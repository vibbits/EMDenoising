%==============================================================================
% NLMeansDenoising.q
% NLMeans implementation for the CPU / GPU (white noise only)
% 2011-2012 Bart Goossens
%==============================================================================
import "system.q"
import "linalg.q"

{!author name="Bart Goossens"}
{!doc category="Image Processing/Restoration"}

% Section: Overview
%
% Demonstration of image denoising for additive white Gaussian noise using
% the Non-local means filter, according to:
%
% * B. Goossens, H.Q. Luong, A. Pizurica, W. Philips, "An improved non-local 
% means algorithm for image denoising," in 2008 International Workshop on Local 
% and Non-Local Approximation in Image Processing (LNLA2008), Lausanne, 
% Switzerland, Aug. 25-29 .
% 

% Function: denoise_nlmeans
%
% Denoising of images using non-local means (assuming additive white Gaussian
% input noise)
%
% :function img_den = denoise_nlmeans(img_noisy : cube, sigma : scalar, search_wnd : int, _
%    half_block_size : ivec2, vector_based_filter = 1, klt_postprocessing = 1)
%
% Parameters:
% img_noisy       - noisy input image. This can either be an RGB image or a
%                     grayscale image
% sigma           - estimated noise standard deviation
% search_wnd      - size of the search window used for block matching
% half_block_size - half the size of the local patch
% vector_based_filter - use the vector-based filter (which is slower, but
%                     gives a better end result)
% klt_postprocessing - apply KLT postprocessing to remove the remaining noise
%
function img_den = denoise_nlmeans( _
    img_noisy : cube, _
    sigma : scalar, _
    search_wnd : int, _
    half_block_size : ivec2, _
    vector_based_filter : int = 1, _
    klt_postprocessing : int = 1)
    
    printf("From Quasar: denoise_nlmeans: sigma=%f search_wnd=%d half_block_size=[%d,%d] vector_based_filter=%d klt_postprocessing=%d\n", sigma, search_wnd, half_block_size[0], half_block_size[1], vector_based_filter, klt_postprocessing)
    % printf("Writing input image\n")
    % imwrite("E:\\\brol_fromquasar_nlsmean_input.tif", img_noisy)

    img_noisy_size = size(img_noisy, 0..2)
    printf("Input image: %d x %d x %d\n", img_noisy_size[0], img_noisy_size[1], img_noisy_size[2])
    assert(img_noisy_size[2] == 1)   % only grayscale images are supported

    % bandwidth parameter
    h = -0.045 / (sigma^2 * size(img_noisy,2))

    [rows,cols,C] = size(img_noisy,0..2)

    % prewhitened image
    img_prewhit = img_noisy

    % computation of the square difference
    function y = square_diff(x, d : vec)
        function [] = __kernel__ process_gray(x : mat'circular, y : mat'unchecked, d : ivec2, pos : ivec2)
            diff = x[pos] - x[pos + d]
            y[pos] = diff * diff
        end
        function [] = __kernel__ process_rgb(x : cube'circular, y : mat'unchecked, d : ivec2, pos : ivec2)
            diff = x[pos[0],pos[1],0..2] - x[pos[0]+d[0],pos[1]+d[1],0..2]
            y[pos] = dotprod(diff, diff)
        end


        y = zeros(size(x,0),size(x,1))
        if size(x,2)==1
            parallel_do(size(y),x,y,d,process_gray)
        else
            parallel_do(size(y),x,y,d[0..1],process_rgb)
        endif
    end

    % computation of the running average based on the integral image representation
    function y = running_average(x, half_block_size)
        function [] = __kernel__ process(x : mat, y : mat'unchecked, d : vec2, pos : vec2)
            y[pos] = x[pos + d] + x[pos - d] - (x[pos + [-d[0],d[1]]] + x[pos + [d[0],-d[1]]]) 
        end
        y = zeros(size(x))
        parallel_do(size(x),x,y,half_block_size,process)    
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
        parallel_do (size(img_out), img_in, img_tmp, half_block_size[1], mean_filter_hor_cyclic)
        parallel_do (size(img_out), img_tmp, img_out, half_block_size[0], mean_filter_ver_cyclic)
    end

    % weighting and accumulation step
    function [] = weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, d)
        function [] = __kernel__ process_rgb(accum_mtx : cube'unchecked, accum_weight : mat'unchecked, _
                                             img_noisy : cube'circular, ssd : mat'circular, h : scalar, d : ivec2, pos : ivec2)
            dpos = pos + d
            weight1 = exp(h * ssd[pos])
            a = weight1 * img_noisy[dpos[0],dpos[1],0..2]
            w = weight1

            dpos = pos - d
            weight2 = exp(h * ssd[dpos])
            a += weight2 * img_noisy[dpos[0],dpos[1],0..2]
            w += weight2

            accum_mtx[pos[0],pos[1],0..2] = accum_mtx[pos[0],pos[1],0..2] + a
            accum_weight[pos] += w    
        end

        function [] = __kernel__ process_gray(accum_mtx : mat'unchecked, accum_weight : mat'unchecked, _
                                              img_noisy : mat'circular, ssd : mat'circular, h : scalar, d : ivec2, pos : ivec2)
            dpos = pos + d
            weight1 = exp(h * ssd[pos])
            a = weight1 * img_noisy[dpos]
            w = weight1

            dpos = pos - d
            weight2 = exp(h * ssd[dpos])
            a += weight2 * img_noisy[dpos]
            w += weight2

            accum_mtx[pos] += a
            accum_weight[pos] += w
        
        end

        if size(img_noisy,2) == 1 % grayscale image
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,ssd,h, d, process_gray)
        else % rgb images
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,img_noisy,ssd,h, d, process_rgb)
        endif
    end

    % vector-based filter - (0,0) accumulation step
    function [] = vector_weight_and_accumulate1(accum_mtx, accum_weight, accum_sqweights, img_noisy, weight, V)
        function [] = __kernel__ process_rgb(accum_mtx : cube'unchecked, accum_weight : mat'unchecked, _
            accum_sqweights : mat'unchecked, img_noisy : cube'circular, weight : scalar, V : int, pos : ivec2)

            accum_weight[pos] += weight
            accum_sqweights[pos] += weight*weight

            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    % TODO - check for vector-based version
                    %accum_mtx[pos[0],pos[1],3*index+(0..2)] += weight * img_noisy[pos[0]+m,pos[1]+n,0..2]
                    accum_mtx[pos[0],pos[1],3*index+0] += weight * img_noisy[pos[0]+m,pos[1]+n,0]
                    accum_mtx[pos[0],pos[1],3*index+1] += weight * img_noisy[pos[0]+m,pos[1]+n,1]
                    accum_mtx[pos[0],pos[1],3*index+2] += weight * img_noisy[pos[0]+m,pos[1]+n,2]
                end
            end
        end

        function [] = __kernel__ process_gray(accum_mtx : cube'unchecked, accum_weight : mat'unchecked, _
            accum_sqweights : mat'unchecked, img_noisy : mat'circular, weight : scalar, V : int, pos : ivec2)

            accum_weight[pos] += weight
            accum_sqweights[pos] += weight*weight

            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    accum_mtx[pos[0],pos[1],index] += weight * img_noisy[pos[0]+m,pos[1]+n]
                end
            end
        end

        if size(img_noisy,2) == 1 % grayscale image
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,accum_sqweights,img_noisy,weight,V,process_gray)
        else % rgb images
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,accum_sqweights,img_noisy,weight,V,process_rgb)
        endif
    end

    % vector-based weighting and accumulation step
    function [] = vector_weight_and_accumulate2(accum_mtx, accum_weight, accum_sqweights, img_noisy, ssd, h, d, V)
        function [] = __kernel__ process_rgb(accum_mtx : cube'unchecked, accum_weight : mat'unchecked, _
                                             accum_sqweights : mat'unchecked, _
                                             img_noisy : cube'circular, ssd : mat'circular, h : scalar, d : ivec2, V : int, pos : ivec2)
            weight1 = exp(h * ssd[pos])
            weight2 = exp(h * ssd[pos-d])

            accum_sqweights[pos] += weight1 * weight1 + weight2 * weight2
            accum_weight[pos] += weight1 + weight2
        
            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    val = weight1 * img_noisy[pos[0]+d[0]+m,pos[1]+d[1]+n,0..2] + _
                          weight2 * img_noisy[pos[0]-d[0]+m,pos[1]-d[1]+n,0..2]
                    accum_mtx[pos[0],pos[1],3*index+0] += val[0]
                    accum_mtx[pos[0],pos[1],3*index+1] += val[1]
                    accum_mtx[pos[0],pos[1],3*index+2] += val[2]
                end
            end

        end

        function [] = __kernel__ process_gray(accum_mtx : cube'unchecked, accum_weight : mat'unchecked, _
                                              accum_sqweights : mat'unchecked, _
                                              img_noisy : mat'circular, ssd : mat'circular, h : scalar, d : ivec2, V : int, pos : ivec2)
            weight1 = exp(h * ssd[pos])
            weight2 = exp(h * ssd[pos-d])

            accum_sqweights[pos] += weight1 * weight1 + weight2 * weight2
            accum_weight[pos] += weight1 + weight2
        
            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    accum_mtx[pos[0],pos[1],index] += weight1 * img_noisy[pos+d+[m,n]] + _
                                                      weight2 * img_noisy[pos-d+[m,n]]
                end
            end
        end

        if size(img_noisy,2) == 1 % grayscale image
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,accum_sqweights,img_noisy,ssd,h, d, V, process_gray)
        else % rgb images
            parallel_do(size(img_noisy,0..1),accum_mtx,accum_weight,accum_sqweights,img_noisy,ssd,h, d, V, process_rgb)
        endif
    end

    % vector-based aggregation
    function [] = vector_aggregate(accum_mtx, accum_weight, img_den, V)
        function [] = __kernel__ process_rgb(accum_mtx : cube'circular, accum_weight : mat'unchecked, _
                                              img_den : cube'unchecked, V : int, pos : ivec2)
            sum = [0.0, 0.0, 0.0]
            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    sum[0] += accum_mtx[pos[0]-m,pos[1]-n,3*index+0]
                    sum[1] += accum_mtx[pos[0]-m,pos[1]-n,3*index+1]
                    sum[2] += accum_mtx[pos[0]-m,pos[1]-n,3*index+2]
                end
            end
            img_den[pos[0],pos[1],0..2] = sum / accum_weight[pos]
        end


        function [] = __kernel__ process_gray(accum_mtx : cube'circular, accum_weight : mat'unchecked, _
                                              img_den : mat'unchecked, V : int, pos : ivec2)
            sum = 0.0
            for m=-V..V
                for n=-V..V
                    index=(V+m)*(2*V+1)+V+n
                    sum += accum_mtx[pos[0]-m,pos[1]-n,index]
                end
            end
            img_den[pos] = sum / accum_weight[pos]
        end

        if size(img_den,2) == 1 % grayscale image
            parallel_do(size(img_den,0..1),accum_mtx, accum_weight, img_den, V, process_gray)
        else % rgb images
            parallel_do(size(img_den,0..1),accum_mtx, accum_weight, img_den, V, process_rgb)
        endif
    end

    % the non-vector based filter implementation is the simplest, and the fastest too...
    if vector_based_filter == 0

        accum_mtx = zeros(rows,cols,C)
        accum_weight = zeros(rows,cols)
        ssd = zeros(rows,cols)

        for dis_x = -search_wnd..search_wnd
            for dis_y = -search_wnd..search_wnd
                if dis_x == 0 && dis_y == 0
                    accum_mtx = accum_mtx + 0.1 * img_noisy
                    accum_weight = accum_weight + 0.1
                elseif (dis_y == 0 && dis_x > 0) || (dis_y > 0)
                    square_diff_img = square_diff(img_prewhit, [dis_x,dis_y])
                    moving_average_filter(square_diff_img, ssd, half_block_size)
                    weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, [dis_x,dis_y])
                endif
            end
        end

        img_den = accum_mtx ./ repmat(accum_weight,[1,1,C])

    else

        V = 1       % half size of the vector neighborhood

        accum_mtx = zeros(rows,cols,C*(2*V+1)^2) % a little bit more mory is required!
        accum_weight = zeros(rows,cols)
        accum_sqweights = zeros(rows,cols) % accumulator for the squared weights
        ssd = zeros(rows,cols)

        for dis_x = -search_wnd..search_wnd
            for dis_y = -search_wnd..search_wnd
                if dis_x == 0 && dis_y == 0
                    vector_weight_and_accumulate1(accum_mtx, accum_weight, accum_sqweights, img_noisy, 0.1, V)
                elseif (dis_y == 0 && dis_x > 0) || (dis_y > 0)
                    square_diff_img = square_diff(img_prewhit, [dis_x,dis_y])
                    moving_average_filter(square_diff_img, ssd, half_block_size)
                    vector_weight_and_accumulate2(accum_mtx, accum_weight, accum_sqweights, img_noisy, ssd, h, [dis_x,dis_y], V)
                endif
            end
        end

        % apply the locally adaptive KLTb-based postprocessing
        if klt_postprocessing
            B = 32

            % compute the local noise variance in the image
            local_noise_var = sigma^2 * accum_sqweights ./ accum_weight.^2

            % Note - matrix dimensions: B^2 x C
            function [] = __kernel__ wiener_filter_kernel(u : mat'unchecked, nv : mat'unchecked, _
                Sig_Y_diag : mat'unchecked, B : int, offset : ivec2, pos : ivec2)
                m = int(pos[0] / B)
                n = mod(pos[0], B)
                v = nv[offset + [m,n]]
                Sig_X_diag = max(1e-2, Sig_Y_diag[pos[1],pos[1]] - v)
                u[pos] = u[pos] * Sig_X_diag / (Sig_X_diag + v)
            end

            % a possible refinement for the case of correlated noise would be 
            % to also take the noise PSD into account in the KLT denoising. Here
            % we simply assume that the noise PSD is flat after NLMeans denoising, 
            % which is not necessarily true. Although for most types of correlated
            % noise this does not pose too many problems...
            for m = 0..B..rows-1
                for n = 0..B..cols-1
                    block_rm = m..min(m+B,rows)-1
                    block_rn = n..min(n+B,cols)-1
                    CC = size(accum_mtx,2)
                    sample = accum_mtx[block_rm, block_rn, :] ./ repmat(accum_weight[block_rm, block_rn],[1,1,CC])
                    sample = reshape(sample, [numel(block_rm)*numel(block_rn), CC])
                    y_mean = mean(sample,1)*ones(1,CC)
                    Cy = transpose(sample - y_mean) * (sample - y_mean) / (numel(block_rm)*numel(block_rn)-1)
                    [U,Sig_Y_diag,dummy] = svd(Cy)

                    % apply the forward KLT
                    u = (sample - y_mean) * U

                    % apply the Wiener filter
                    parallel_do(size(u), u, local_noise_var, Sig_Y_diag, B, [m, n], wiener_filter_kernel)

                    y = u * transpose(U) + y_mean % backward KLT 
                    y = reshape(y, [numel(block_rm), numel(block_rn), CC])
                    y = y .* repmat(accum_weight[block_rm, block_rn],[1,1,CC]) % unnormalize
                    
                    % update the estimated signal vectors
                    accum_mtx[block_rm, block_rn, :] = y
                end
            end
                
        endif

        % in the vector-based case, we only accumulated the weights for the central pixel
        % so far. With a simple convolution, we can obtain the block-accumulated weights
        moving_average_filter(accum_weight, accum_weight, V*[1,1])

        % average over all shifted estimates
        img_den = zeros(size(img_noisy))
        vector_aggregate(accum_mtx, accum_weight, img_den, V)        
    endif
    
    printf("From Quasar: done calculating nlmeans\n");
    
%    % For debugging:
%    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))
%    printf("denoise_nlmeans - nonsense psnr=%f (denoised vs noisy) dB\n", psnr(img_den, img_noisy))  % nonsense, just want to check if denoised image is really different
%    
%    printf("writing result")
%    imwrite("E:\\workspace\\minimal-ij1-plugin-native-quasar\\src\\main\\quasar\\brol_fromquasar_nlsmean_result.png", img_den, [0, 255])

end
