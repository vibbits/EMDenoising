%==================================================================================================
% Image denoising/deconvolution for white or correlated noise based on the non-local means filter
% Denoising based on: 
%   J. Roels, J. Aelterman, J. De Vylder, H.Q. Luong, Y. Saeys, S. Lippens, W. Philips, 
%   "Noise Analysis and Removal in 3D Electron Microscopy", in Lecture Notes in 
%   Computer Science 8888 (Advances in Visual Computing), pp. 31-40, Las Vegas, USA, December 8-10, 2014.
% Deconvolution based on: 
%   J. Roels, J. Aelterman, J. De Vylder, H.Q. Luong, Y. Saeys, W. Philips, 
%   "Bayesian deconvolution of scanning electron microscopy images using point-spread function estimation and non-local regularization",  
%   EMBC 2016, Proceedings: 443-447
% Implementation based on the non-local means implementation of Bart Goossens
%==================================================================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration"}

import "imfilter.q"
import "estimate_noise.q"
import "utils.q"

% Function: main
% 
% Test function
% 
% Usage:
%   : function [] = main()
function [] = main()

    % read data
    img = imread("lena_big.tif")[:,:,0]/255
    
    % add blur
    f_size = 15
    f_sigma = 1
    blur_kernel = fgaussian(f_size, f_sigma)
    img_blurred = imfilter(img,blur_kernel,ceil(size(blur_kernel)/2)-1,"mirror")
    
    % add noise
    sigma = 0.05
    em_corr_filter = transpose(csvread("correlation_filters.csv")[:,10])
    em_corr_filter_inv = transpose(csvread("inverse_correlation_filters.csv")[:,10])
    noise = randn(size(img))
    corr_noise = imfilter(noise, em_corr_filter, [(size(em_corr_filter,0)-1)/2,(size(em_corr_filter,1)-1)/2], "mirror")
    corr_noise = corr_noise ./ std(corr_noise) .* sigma
    img_noisy = img_blurred + corr_noise
    
    % estimate noise level
    sigma_est = estimate_noise_liu(img_noisy) 
    
    % set parameters
    half_search_size = 5
    half_block_size = 4
    h = 3.14044952392578*sigma_est^2 + 1.86890029907227*sigma_est + 0.0483760833740234
    lambda = 0.6 % automatic lambda estimation suboptimal, manual choice
    h_c = 1.79779577255249*sigma_est + 0.0488053560256958
    lambda_c = 0.01 % automatic lambda estimation suboptimal, manual choice
    num_iter = 20
    
    % non-local denoising
    tic()
    img_den = denoise_nlmeans(img_noisy, half_search_size, half_block_size, h)
    toc()
    % non-local denoising (correlated)
    tic()
    img_den_c = denoise_nlmeans_c(img_noisy, half_search_size, half_block_size, h_c, em_corr_filter_inv)
    toc()
    % non-local deconvolution
    tic()
    img_dec = deconv_nlmeans(img_noisy,blur_kernel,lambda,num_iter,half_search_size,half_block_size,h)
    toc()
    % non-local deconvolution (correlated)
    tic()
    img_dec_c = deconv_nlmeans_c(img_noisy,blur_kernel,lambda_c,num_iter,half_search_size,half_block_size,h_c,em_corr_filter_inv)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Non-local means - psnr=%f dB", psnr(img_den,img)))
    fig2=imshow(img_den_c,[0,1])
    title(sprintf("Non-local means (correlated adjustment) - psnr=%f dB", psnr(img_den_c,img)))
    fig3=imshow(img_dec,[0,1])
    title(sprintf("Non-local means deconvolution - psnr=%f dB", psnr(img_dec,img)))
    fig4=imshow(img_dec_c,[0,1])
    title(sprintf("Non-local means deconvolution (correlated adjustment) - psnr=%f dB", psnr(img_dec_c,img)))
    fig0.connect(fig1)
    fig1.connect(fig2)
    fig2.connect(fig3)
    fig3.connect(fig4)
    
end

% Image deconvolution/denoising for correlated noise using a Non-local means prior
function x_est : mat = deconv_nlmeans_c( _
    y : mat, _
    H : mat, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar, _
    corr_filter_inv : mat)
    
    inprod = (x,y) -> sum(x.*y)
    norm2 = x -> inprod(x,x)
    
    H_T = flip_mat(H, ceil(size(H)/2)-1)
    % steepest descent
    x_est = y
    for iter=0..num_iter-1
        [NLMS_x_est_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum_c(x_est, y, search_wnd, half_block_size, h, corr_filter_inv)
        grad = 2*(imfilter(imfilter(x_est,H,ceil(size(H)/2)-1,"mirror"),H_T,ceil(size(H_T)/2)-1,"mirror") - imfilter(y,H_T,ceil(size(H_T)/2)-1,"mirror")) + _
               2*lambda*(NLMS_weights_cum.*x_est - NLMS_x_est_cum)
        H_grad = imfilter(grad,H,ceil(size(H)/2)-1,"mirror");
        [NLMS_grad_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum_c(grad, y, search_wnd, half_block_size, h, corr_filter_inv)
        temp = NLMS_weights_cum.*grad - NLMS_grad_cum
        alpha_step = (2*lambda*inprod(x_est,temp) - inprod(y-imfilter(x_est,H,ceil(size(H)/2)-1,"mirror"),H_grad)) / _
                      (2*lambda*inprod(grad,temp) + (norm2(H_grad)))
        x_est = x_est - alpha_step*grad
    end
    
end

% Image deconvolution/denoising for correlated noise using a Non-local means prior
function x_est : mat = deconv_nlmeans( _
    y : mat, _
    H : mat, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar)
    
    inprod = (x,y) -> sum(x.*y)
    norm2 = x -> inprod(x,x)
    
    H_T = flip_mat(H, ceil(size(H)/2)-1)
    % steepest descent
    x_est = y
    for iter=0..num_iter-1
        [NLMS_x_est_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum(x_est, y, search_wnd, half_block_size, h)
        grad = 2*(imfilter(imfilter(x_est,H,ceil(size(H)/2)-1,"mirror"),H_T,ceil(size(H_T)/2)-1,"mirror") - imfilter(y,H_T,ceil(size(H_T)/2)-1,"mirror")) + _
               2*lambda*(NLMS_weights_cum.*x_est - NLMS_x_est_cum)
        H_grad = imfilter(grad,H,ceil(size(H)/2)-1,"mirror");
        [NLMS_grad_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum(grad, y, search_wnd, half_block_size, h)
        temp = NLMS_weights_cum.*grad - NLMS_grad_cum
        alpha_step = (2*lambda*inprod(x_est,temp) - inprod(y-imfilter(x_est,H,ceil(size(H)/2)-1,"mirror"),H_grad)) / _
                     (2*lambda*inprod(grad,temp) + (norm2(H_grad)))
        x_est = x_est - alpha_step*grad
    end
    
end

% Mirrors a matrix around a center position
function [B:mat] = flip_mat(A:mat, c:ivec2)
    function [] = __kernel__ flip_kernel(x:mat, y:mat, c:ivec2, pos:ivec2)
        y[pos] = x[2*c-pos]
    end
    B = zeros(size(A))
    parallel_do(size(B),A,B,c,flip_kernel)
end

% Image denoising for correlated, signal-dependent noise using a Non-local means-based filter
% The output of the function is a cummulated signal and weight matrix (respectively accum_mtx 
% and accum_weight), the eventual denoised image is obtained by computing accum_mtx./accum_weight
%   img_noisy: the image to be denoised
%   img_orig: the image of which the weights will be based upon
%   half_search_size: half the size of the search space where patches should be compared
%   half_block_size: half the size of the patches to compare
%   h: parameter to control the weighting function
%   corr_filter_inv: inverse impulse response of noise correlation kernel
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_c( _
    img_noisy, _
    img_orig, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    corr_filter_inv: mat)

    % prewhitened image
    img_prewhit = imfilter(img_orig, corr_filter_inv, [(size(corr_filter_inv,0)-1)/2,(size(corr_filter_inv,1)-1)/2], "mirror")
    img_prewhit = bound_extension(img_prewhit, half_block_size, half_block_size, "mirror")
    
    % Extend images to avoid boundary artifacts
    img_noisy = bound_extension(img_noisy, half_block_size, half_block_size, "mirror")
    img_orig = bound_extension(img_orig, half_block_size, half_block_size, "mirror")

    [rows,cols] = size(img_noisy)
    Bs = (2*half_block_size+1)^2

    % computation of the square difference
    function y = square_diff(x, d : vec)
        function [] = __kernel__ process_gray(x : mat'circular, y : mat'unchecked, d : ivec2, pos : ivec2)
            diff = x[pos] - x[pos + d]
            y[pos] = diff * diff
        end

        y = zeros(size(x,0),size(x,1))
        parallel_do(size(y),x,y,d,process_gray)
    end

    % A cyclic mean filter - (without normalization)
    function [] = moving_average_filter(img_in, img_out, half_block_size)
        function [] = __kernel__ mean_filter_hor_cyclic(x : mat'circular, y : mat'unchecked, n : int, pos : ivec2)
            sum = 0.
            for i=pos[1]-n..pos[1]+n
                sum = sum + x[pos[0],i]
            end
            y[pos] = sum
        end

        function [] = __kernel__ mean_filter_ver_cyclic(x : mat'circular, y : mat'unchecked, n : int, pos : ivec2)
            sum = 0.
            for i=pos[0]-n..pos[0]+n
                sum = sum + x[i,pos[1]]
            end
            y[pos] = sum
        end

        img_tmp = zeros(size(img_in))
        parallel_do (size(img_out), img_in, img_tmp, half_block_size, mean_filter_hor_cyclic)
        parallel_do (size(img_out), img_tmp, img_out, half_block_size, mean_filter_ver_cyclic)
    end

    % weighting and accumulation step
    function [] = weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, d)

        function [] = __kernel__ process_gray(accum_mtx : mat'unchecked, accum_weight : mat'unchecked, _
                                              img_noisy : mat'circular, ssd : mat'circular, h : scalar, d : ivec2, pos : ivec2)
            dpos = pos + d
            % weight1 = (sqrt(ssd[pos]/Bs) <= h) * (1-(ssd[pos]/Bs))/(h*h)                                                                  % TUKEY
            % weight1 = 1/max(1,sqrt(ssd[pos]/Bs)/h)                                                                                        % HUBER
            % weight1 = (ssd[pos]==0) .* 1 + (ssd[pos]!=0 && sqrt(ssd[pos]/Bs)<=h) * sin(pi*sqrt(ssd[pos]/Bs)/h)/(sqrt(ssd[pos]/Bs)/h)      % ANDREWS
            % weight1 = 1/(1+(ssd[pos]/Bs)/(h*h))                                                                                           % CAUCHY
            % weight1 = 1/((1+(ssd[pos]/Bs)/(h*h))*(1+(ssd[pos]/Bs)/(h*h)))                                                                 % GEMANMCCLURE
            % weight1 = exp(-(ssd[pos]/Bs)/(h*h))                                                                                           % LECLERCQ
            % weight1 = (sqrt(ssd[pos]/Bs) < h) * (1 - ((ssd[pos]/Bs)/(h*h)))                                                               % BISQUARE
            weight1 = (sqrt(ssd[pos]/Bs) < h) * (1 - ((ssd[pos]/Bs)/(h*h)))^8                                                               % MODIFIED BISQUARE
            % weight1 = (ssd[pos]==0) + (ssd[pos]!=0)*tanh(sqrt(ssd[pos]/Bs)/h)/(sqrt(ssd[pos]/Bs)/h)                                       % LOGISTIC
            % weight1 = (sqrt(ssd[pos]/Bs)<h)                                                                                               % TALWAR
            % weight1 = (sqrt(ssd[pos]/Bs)<h)/(h*h) + (sqrt(ssd[pos]/Bs)>=h)/(ssd[pos]/Bs)                                                  % BLUE
            a = weight1 * img_noisy[dpos]
            w = weight1

            dpos = pos - d
            % weight2 = (sqrt(ssd[dpos]/Bs) <= h) * (1-(ssd[dpos]/Bs))/(h*h)                                                                % TUKEY
            % weight2 = 1/max(1,sqrt(ssd[dpos]/Bs)/h)                                                                                       % HUBER
            % weight2 = (ssd[dpos]==0) .* 1 + (ssd[dpos]!=0 && sqrt(ssd[dpos]/Bs)<=h) * sin(pi*sqrt(ssd[dpos]/Bs)/h)/(sqrt(ssd[dpos]/Bs)/h) % ANDREWS
            % weight2 = 1/(1+(ssd[dpos]/Bs)/(h*h))                                                                                          % CAUCHY
            % weight2 = 1/((1+(ssd[dpos]/Bs)/(h*h))*(1+(ssd[dpos]/Bs)/(h*h)))                                                               % GEMANMCCLURE
            % weight2 = exp(-(ssd[dpos]/Bs)/(h*h))                                                                                          % LECLERCQ
            % weight2 = (sqrt(ssd[dpos]/Bs) < h) * (1 - ((ssd[dpos]/Bs)/(h*h)))                                                             % BISQUARE
            weight2 = (sqrt(ssd[dpos]/Bs) < h) * (1 - ((ssd[dpos]/Bs)/(h*h)))^8                                                             % MODIFIED BISQUARE
            % weight2 = (ssd[dpos]==0) + (ssd[dpos]!=0)*tanh(sqrt(ssd[dpos]/Bs)/h)/(sqrt(ssd[dpos]/Bs)/h)                                   % LOGISTIC
            % weight2 = (sqrt(ssd[dpos]/Bs)<h)                                                                                              % TALWAR
            % weight2 = (sqrt(ssd[dpos]/Bs)<h)/(h*h) + (sqrt(ssd[dpos]/Bs)>=h)/(ssd[dpos]/Bs)                                               % BLUE
            a += weight2 * img_noisy[dpos]
            w += weight2

            accum_mtx[pos] += a
            accum_weight[pos] += w
        
        end

        parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,ssd,h, d, process_gray)
    end

    % weighting and accumulation step partially
    function [] = weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)

        function [] = __kernel__ process_gray(accum_mtx : mat'unchecked, accum_weight : mat'unchecked, _
                                              img_noisy : mat'circular, h : scalar, pos : ivec2)
            weight = 0.2 * (sqrt(0) < h) * (1 - ((0/Bs)/(h*h)))^8
            % weight = 0.01 * exp(h * 0)
            a = weight * img_noisy[pos]
            w = weight

            accum_mtx[pos] += a
            accum_weight[pos] += w
        end

        parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,h, process_gray)
    end

    accum_mtx = zeros(rows,cols)
    accum_weight = zeros(rows,cols)
    ssd = zeros(rows,cols)

    for md = -half_search_size..half_search_size
        for nd = -half_search_size..half_search_size
            if (md > 0) || (md == 0 && nd > 0)
                square_diff_img = square_diff(img_prewhit, [md,nd])
                moving_average_filter(square_diff_img, ssd, half_block_size)
                weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, [md,nd])
            elseif md == 0 && nd == 0
                weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)
            endif
        end
    end
    
    % crop images
    img_noisy = img_noisy[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]
    accum_mtx = accum_mtx[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]
    accum_weight = accum_weight[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]

end

% Image denoising for correlated, signal-dependent noise using a Non-local means-based filter
%   img_noisy: the image to be denoised
%   img_orig: the image of which the weights will be based upon
%   half_search_size: half the size of the search space where patches should be compared
%   half_block_size: half the size of the patches to compare
%   h: parameter to control the weighting function
%   corr_filter_inv: inverse impulse response of noise correlation kernel
function img_est = denoise_nlmeans_c( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    corr_filter_inv: mat)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_c(img_noisy, _
                                                          img_noisy, _
                                                          half_search_size : int, _
                                                          half_block_size : int, _
                                                          h : scalar, _
                                                          corr_filter_inv: mat)
    img_est = accum_mtx./accum_weight
    
end

% Image denoising for correlated, signal-dependent noise using a Non-local means-based filter
% The output of the function is a cummulated signal and weight matrix (respectively accum_mtx 
% and accum_weight), the eventual denoised image is obtained by computing accum_mtx./accum_weight
%   img_noisy: the image to be denoised
%   img_orig: the image of which the weights will be based upon
%   half_search_size: half the size of the search space where patches should be compared
%   half_block_size: half the size of the patches to compare
%   h: parameter to control the weighting function
%   sigma_0: base standard deviation of the noise (standard deviation in absence of signal)
%   alpha: signal dependency parameter
%   corr_filter_inv: inverse impulse response of noise correlation kernel
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum( _
    img_noisy, _
    img_orig, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)
    
    % Extend images to avoid boundary artifacts
    img_noisy = bound_extension(img_noisy, half_block_size, half_block_size, "mirror")
    img_orig = bound_extension(img_orig, half_block_size, half_block_size, "mirror")
    
    [rows,cols] = size(img_noisy)
    Bs = (2*half_block_size+1)^2

    % computation of the square difference
    function y = square_diff(x, d : vec)
        function [] = __kernel__ process_gray(x : mat'circular, y : mat'unchecked, d : ivec2, pos : ivec2)
            diff = x[pos] - x[pos + d]
            y[pos] = diff * diff
        end

        y = zeros(size(x,0),size(x,1))
        parallel_do(size(y),x,y,d,process_gray)
    end

    % A cyclic mean filter - (without normalization)
    function [] = moving_average_filter(img_in, img_out, half_block_size)
        function [] = __kernel__ mean_filter_hor_cyclic(x : mat'circular, y : mat'unchecked, n : int, pos : ivec2)
            sum = 0.
            for i=pos[1]-n..pos[1]+n
                sum = sum + x[pos[0],i]
            end
            y[pos] = sum
        end

        function [] = __kernel__ mean_filter_ver_cyclic(x : mat'circular, y : mat'unchecked, n : int, pos : ivec2)
            sum = 0.
            for i=pos[0]-n..pos[0]+n
                sum = sum + x[i,pos[1]]
            end
            y[pos] = sum
        end

        img_tmp = zeros(size(img_in))
        parallel_do (size(img_out), img_in, img_tmp, half_block_size, mean_filter_hor_cyclic)
        parallel_do (size(img_out), img_tmp, img_out, half_block_size, mean_filter_ver_cyclic)
    end

    % weighting and accumulation step
    function [] = weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, d)

        function [] = __kernel__ process_gray(accum_mtx : mat'unchecked, accum_weight : mat'unchecked, _
                                              img_noisy : mat'circular, ssd : mat'circular, h : scalar, d : ivec2, pos : ivec2)
            dpos = pos + d
            % weight1 = (sqrt(ssd[pos]/Bs) <= h) * (1-(ssd[pos]/Bs))/(h*h)                                                                  % TUKEY
            % weight1 = 1/max(1,sqrt(ssd[pos]/Bs)/h)                                                                                        % HUBER
            % weight1 = (ssd[pos]==0) .* 1 + (ssd[pos]!=0 && sqrt(ssd[pos]/Bs)<=h) * sin(pi*sqrt(ssd[pos]/Bs)/h)/(sqrt(ssd[pos]/Bs)/h)      % ANDREWS
            % weight1 = 1/(1+(ssd[pos]/Bs)/(h*h))                                                                                           % CAUCHY
            % weight1 = 1/((1+(ssd[pos]/Bs)/(h*h))*(1+(ssd[pos]/Bs)/(h*h)))                                                                 % GEMANMCCLURE
            % weight1 = exp(-(ssd[pos]/Bs)/(h*h))                                                                                           % LECLERCQ
            % weight1 = (sqrt(ssd[pos]/Bs) < h) * (1 - ((ssd[pos]/Bs)/(h*h)))                                                               % BISQUARE
            weight1 = (sqrt(ssd[pos]/Bs) < h) * (1 - ((ssd[pos]/Bs)/(h*h)))^8                                                               % MODIFIED BISQUARE
            % weight1 = (ssd[pos]==0) + (ssd[pos]!=0)*tanh(sqrt(ssd[pos]/Bs)/h)/(sqrt(ssd[pos]/Bs)/h)                                       % LOGISTIC
            % weight1 = (sqrt(ssd[pos]/Bs)<h)                                                                                               % TALWAR
            % weight1 = (sqrt(ssd[pos]/Bs)<h)/(h*h) + (sqrt(ssd[pos]/Bs)>=h)/(ssd[pos]/Bs)                                                  % BLUE
            a = weight1 * img_noisy[dpos]
            w = weight1

            dpos = pos - d
            % weight2 = (sqrt(ssd[dpos]/Bs) <= h) * (1-(ssd[dpos]/Bs))/(h*h)                                                                % TUKEY
            % weight2 = 1/max(1,sqrt(ssd[dpos]/Bs)/h)                                                                                       % HUBER
            % weight2 = (ssd[dpos]==0) .* 1 + (ssd[dpos]!=0 && sqrt(ssd[dpos]/Bs)<=h) * sin(pi*sqrt(ssd[dpos]/Bs)/h)/(sqrt(ssd[dpos]/Bs)/h) % ANDREWS
            % weight2 = 1/(1+(ssd[dpos]/Bs)/(h*h))                                                                                          % CAUCHY
            % weight2 = 1/((1+(ssd[dpos]/Bs)/(h*h))*(1+(ssd[dpos]/Bs)/(h*h)))                                                               % GEMANMCCLURE
            % weight2 = exp(-(ssd[dpos]/Bs)/(h*h))                                                                                          % LECLERCQ
            % weight2 = (sqrt(ssd[dpos]/Bs) < h) * (1 - ((ssd[dpos]/Bs)/(h*h)))                                                             % BISQUARE
            weight2 = (sqrt(ssd[dpos]/Bs) < h) * (1 - ((ssd[dpos]/Bs)/(h*h)))^8                                                             % MODIFIED BISQUARE
            % weight2 = (ssd[dpos]==0) + (ssd[dpos]!=0)*tanh(sqrt(ssd[dpos]/Bs)/h)/(sqrt(ssd[dpos]/Bs)/h)                                   % LOGISTIC
            % weight2 = (sqrt(ssd[dpos]/Bs)<h)                                                                                              % TALWAR
            % weight2 = (sqrt(ssd[dpos]/Bs)<h)/(h*h) + (sqrt(ssd[dpos]/Bs)>=h)/(ssd[dpos]/Bs)                                               % BLUE
            a += weight2 * img_noisy[dpos]
            w += weight2

            accum_mtx[pos] += a
            accum_weight[pos] += w
        
        end

        parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,ssd,h, d, process_gray)
    end

    % weighting and accumulation step partially
    function [] = weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)

        function [] = __kernel__ process_gray(accum_mtx : mat'unchecked, accum_weight : mat'unchecked, _
                                              img_noisy : mat'circular, h : scalar, pos : ivec2)
            weight = 0.2 * (sqrt(0) < h) * (1 - ((0/Bs)/(h*h)))^8
            % weight = 0.01 * exp(h * 0)
            a = weight * img_noisy[pos]
            w = weight

            accum_mtx[pos] += a
            accum_weight[pos] += w
        end

        parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,h, process_gray)
    end
    
    
    accum_mtx = zeros(rows,cols)
    accum_weight = zeros(rows,cols)
    ssd = zeros(rows,cols)

    for md = -half_search_size..half_search_size
        for nd = -half_search_size..half_search_size
            if (md > 0) || (md == 0 && nd > 0)
                square_diff_img = square_diff(img_orig, [md,nd])
                moving_average_filter(square_diff_img, ssd, half_block_size)
                weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, [md,nd])
            elseif md == 0 && nd == 0
                weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)
            endif
        end
    end
    
    % crop images
    img_noisy = img_noisy[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]
    accum_mtx = accum_mtx[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]
    accum_weight = accum_weight[half_block_size..rows-half_block_size-1,half_block_size..cols-half_block_size-1]

end


% Function: denoise_nlmeans
% 
% Non-local means denoising
% 
% Usage:
%   : function [img_est : mat] = denoise_nlmeans(img_noisy, half_search_size : int, half_block_size : int, h : scalar)
% 
% Parameters:
% img_noisy - noisy image
% half_search_size - half search window
% half_block_size - half block size
% h - damping parameter
% 
% Returns:
% img_est - denoised image
function img_est = denoise_nlmeans( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum(img_noisy, _
                                                          img_noisy, _
                                                          half_search_size : int, _
                                                          half_block_size : int, _
                                                          h : scalar)
    img_est = accum_mtx./accum_weight
    
end

% Extend image boundaries 
%   y_ext: extension along the rows
%   x_ext: extension along the columns
%   type_ext:   'mirror'     mirror extension
%               'mirror_nr': mirror without repeating the last pixel
%               'circular':  fft2-like
%               'zeros':     add zeros         
% Function: bound_extension
% 
% Extend image boundaries 
% 
% Usage:
%   : function [img_ext : mat] = bound_extension(img : mat, y_ext : int, x_ext : int, type_ext : string'const)
% 
% Parameters:
% img - input image
% y_ext - extension along the rows
% x_ext - extension along the columns
% type_ext - mirror extension: 'mirror', mirror without repeating the last pixel: 'mirror_nr', fft2-like: 'circular', add zeros: 'zeros'
% 
% Returns:
% img_ext - extended image
function [img_ext:mat] = bound_extension(img:mat, y_ext:int, x_ext:int, type_ext:string)
    [Ny,Nx] = size(img)
    img_ext = zeros(Ny+2*y_ext,Nx+2*x_ext)
    img_ext[y_ext..Ny+y_ext-1,x_ext..Nx+x_ext-1] = img
    
    if type_ext == "mirror"
        img_ext[0..y_ext-1,:] = img_ext[2*y_ext-1..-1..y_ext,:]
        img_ext[:,0..x_ext-1] = img_ext[:,2*x_ext-1..-1..x_ext]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,:] = img_ext[Ny+y_ext-1..-1..Ny,:]
        img_ext[:,Nx+x_ext..Nx+2*x_ext-1] = img_ext[:,Nx+x_ext-1..-1..Nx]
        img_ext[0..y_ext-1,0..x_ext-1] = img_ext[2*y_ext-1..-1..y_ext,2*x_ext-1..-1..x_ext]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[Ny+y_ext-1..-1..Ny,Nx+x_ext-1..-1..Nx]
        img_ext[0..y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[2*y_ext-1..-1..y_ext,Nx+x_ext-1..-1..Nx]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,0..x_ext-1] = img_ext[Ny+y_ext-1..-1..Ny,2*x_ext-1..-1..x_ext]
    elseif type_ext == "mirror_nr"
        img_ext[0..y_ext-1,:] = img_ext[2*y_ext..-1..y_ext+1,:]
        img_ext[:,0..x_ext-1] = img_ext[:,2*x_ext..-1..x_ext+1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,:] = img_ext[Ny+y_ext-2..-1..Ny-1,:]
        img_ext[:,Nx+x_ext..Nx+2*x_ext-1] = img_ext[:,Nx+x_ext-2..-1..Nx-1]
        img_ext[0..y_ext-1,0..x_ext] = img_ext[2*y_ext..-1..y_ext+1,2*x_ext..-1..x_ext+1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[Ny+y_ext..-1..Ny+1,Nx+x_ext..-1..Nx+1]
        img_ext[0..y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[2*y_ext..-1..y_ext+1,Nx+x_ext..-1..Nx+1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,0..x_ext-1] = img_ext[Ny+y_ext..-1..Ny+1,2*x_ext..-1..x_ext+1]
    elseif type_ext == "circular"
        img_ext[0..y_ext-1,:] = img_ext[Ny..Ny+y_ext-1,:]
        img_ext[:,0..x_ext-1] = img_ext[:,Nx..Nx+x_ext-1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,:] = img_ext[y_ext..2*y_ext-1,:]
        img_ext[:,Nx+x_ext..Nx+2*x_ext-1] = img_ext[:,x_ext..2*x_ext-1]
        img_ext[0..y_ext-1,0..x_ext] = img_ext[Ny..Ny+y_ext-1,Nx..Nx+x_ext-1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[y_ext..2*y_ext-1,x_ext..2*x_ext-1]
        img_ext[0..y_ext-1,Nx+x_ext..Nx+2*x_ext-1] = img_ext[Ny..Ny+y_ext-1,x_ext..2*x_ext-1]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,0..x_ext-1] = img_ext[y_ext..2*y_ext-1,Nx..Nx+x_ext-1]
    endif
end
