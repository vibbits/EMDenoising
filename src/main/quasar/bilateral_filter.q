%====================================================
% Implementation of the Bilateral filter for images
% using the fast histogram based method: 
%   Ben Weiss. "Fast median and bilateral filtering" 
%   in ACM SIGGRAPH 2006 Papers (SIGGRAPH '06)
%====================================================
{!author name="Bart Goossens"}
{!doc category="Image Processing/Filters"}

import "estimate_noise.q"

% Function: main
% 
% Testing function
% 
% Usage:
%   : function [] = main()
function [] = main()
    
    % load image
    img = imread("lena_big.tif")[:,:,0]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % estimate noise level
    sigma_est = estimate_noise_liu(img_noisy)
    
    % set parameters
    wnd_size = 6
    damping_param = 33.7677001953125*sigma_est^2 - 20.3271179199219*sigma_est - 0.0491275787353516
    
    % denoising
    tic()
    img_den = zeros(size(img_noisy))
    bilateral_filter_denoise(img_noisy.*255, img_den, wnd_size, damping_param)
    img_den = img_den./255
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Bilateral filter - psnr=%f dB", psnr(img_den,img)))
    fig0.connect(fig1)
    
end

% Function: bilateral_filter
%
% Bilateral filter for images - using the fast histogram based method
%
% :function [] = bilateral_filter_denoise(x : cube, y : cube, r : int, h : scalar)
%
% Parameters:
% x - the input image
% y - the output image (allocated to have the same size as the input image)
% r - the radius of the bilateral filter (r = 4 corresponds to a 9x9 window)
% h - bandwidth parameter of the bilateral filter
% Notes:
% * the spatial distance term is currently being ignored for efficiency reasons
%
function [] = bilateral_filter_denoise(x : cube, y : cube, r : int, h : scalar)

    % add_hist_row : adds one single row to the histogram
    function [] = __device__ add_hist_row(x : cube, hist_im : cube[int]'unchecked, row : int, r : int, N : int, sgn : int, blkpos : int, blkdim : int)
        Nhalf = int(N / 2)
        nblocks = int((size(x,1) + blkdim - 1) / blkdim)

        for blkidx = 0..nblocks-1
            p = blkidx * blkdim + blkpos
            n = mod(p, N)
            p0 = p - n - r

            if n < Nhalf
                for k = -Nhalf+n..-1
                    x1 = int(x[row, p0 + k, 0..2]/4)
                    x2 = int(x[row, p0 + 2*r+1 + k, 0..2]/4)
                    hist_im[x1[0], p, 0] += sgn
                    hist_im[x1[1], p, 1] += sgn
                    hist_im[x1[2], p, 2] += sgn
                    hist_im[x2[0], p, 0] -= sgn
                    hist_im[x2[1], p, 1] -= sgn
                    hist_im[x2[2], p, 2] -= sgn
                end
            elseif n == Nhalf
                % this is the most labour-intensive loop
                for k = 0..2*r
                    x1 = int(x[row, p0 + k, 0..2]/4)
                    hist_im[x1[0], p, 0] += sgn
                    hist_im[x1[1], p, 1] += sgn
                    hist_im[x1[2], p, 2] += sgn
                end
            else
                for k = 0..n-Nhalf-1
                    x1 = int(x[row, p0 + k, 0..2]/4)
                    x2 = int(x[row, p0 + 2*r+1 + k, 0..2]/4)
                    hist_im[x1[0], p, 0] -= sgn
                    hist_im[x1[1], p, 1] -= sgn
                    hist_im[x1[2], p, 2] -= sgn
                    hist_im[x2[0], p, 0] += sgn
                    hist_im[x2[1], p, 1] += sgn
                    hist_im[x2[2], p, 2] += sgn
                end
            endif
        end
    end

    % compute_filter_output : computes the bilateral filter output, based on the local histograms
    function [y : vec3] = __device__ compute_filter_output(hist_im : cube[int]'unchecked, w : vec'unchecked, input : ivec3, p : int, N : int)
        Nhalf = int(N/2)
        n = mod(p, N)
        p0 = p - n

        y = [0.0, 0.0, 0.0]
        weight_sum = [0.0, 0.0, 0.0]
        bins = size(hist_im,0)
        for k=1..bins-1
            if n == Nhalf
                val = hist_im[k, p, 0..2]
            else
                val = hist_im[k, p, 0..2] + hist_im[k, p0 + Nhalf, 0..2]
            endif
            weight = [w[input[0] - k + bins] * val[0], _
                      w[input[1] - k + bins] * val[1], _
                      w[input[2] - k + bins] * val[2]]
            y += weight .* [k, k, k]
            weight_sum += weight
        end

        % the separable histogram step
        y = [y[0] * weight_sum[1] * weight_sum[2], _
             weight_sum[0] * y[1] * weight_sum[2], _
             weight_sum[0] * weight_sum[1] * y[2]]
        weight_sum = weight_sum[0] * weight_sum[1] * weight_sum[2] * [1.0,1.0,1.0]
        y ./= weight_sum
    end

    % ARGUMENTS:
    % x : input image
    % y : median filtered output image
    % hist_im : temporary memory for the histograms (one per column of the input image)
    % w : weighting function [-256..255] - 512 entries
    % r : the radius of the bilateral filter window (r = 4 corresponds to a 9x9 window)
    % N : the number of columns processed simultaneously
    % blkpos : the current position within the block
    function [] = __kernel__ bilateral_filter_kernel(x : cube'unchecked, y : cube'unchecked, hist_im : cube[int], w : vec, r : int, N : int, blkpos : int, blkdim : int)

        nblocks = int((size(x,1) + blkdim - 1) / blkdim)

        % Initialization - add the first r rows
        for m = -r..r-1
            add_hist_row(x, hist_im, m, r, N, 1, blkpos, blkdim)
        end

        syncthreads

        for m = 0..size(x,0)-1
            % Running phase - add row (r), subtract row (r-1)
            add_hist_row(x, hist_im, m+r, r, N, 1, blkpos, blkdim)
            add_hist_row(x, hist_im, m-r-1, r, N, -1, blkpos, blkdim)
            syncthreads

            % At this time, we have all local histograms at row m at our disposal, so we can
            % directly proceed by computing the median
            for blkidx = 0..nblocks-1
                p = blkidx * blkdim + blkpos
                if p < size(y, 1)
                    y[m, p, 0..2] = compute_filter_output(hist_im, w, int(x[m, p, 0..2]/4), p, N) * 4.0
                endif
            end
        end
    end

    nextpow2 = x -> 2^ceil(log2(x))
    N = floor(sqrt(r)/2)*2+1
    bins = 64
    w = exp(-(10^h)/(r+1)^2 * (-bins..bins-1).^2/(bins/256)^2)  % weighting function

    % Compute the block size of the filter
    blk_size = max_block_size(bilateral_filter_kernel, [1, size(x,1)])

    % First - make sure the input is between 0 and 255
    x = saturate(x/255)*255    

    % Apply the filter...
    hist_im = cube[int](bins, size(x,1)+nextpow2(N), 3)
    parallel_do([blk_size,blk_size],x,y,hist_im,w,r,N,bilateral_filter_kernel)
end
