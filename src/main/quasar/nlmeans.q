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

import "gaussian_filter.q"
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
    sigma_blur = 1
    img_blurred = gaussian_filter(img, sigma_blur)
    
    % add noise
    sigma = 0.05
    img_noisy = img_blurred + sigma*randn(size(img))
    
    % set parameters
    half_search_size = 5
    half_block_size = 4
    h = 3.14044952392578*sigma^2 + 1.86890029907227*sigma + 0.0483760833740234
    lambda = 0.3 % automatic lambda estimation suboptimal, manual choice
    num_iter = 20
    
    % non-local denoising
    tic()
    img_den = denoise_nlmeans(img_noisy, half_search_size, half_block_size, h)
    toc()
    % non-local deconvolution
    tic()
    img_dec = deconv_nlmeans(img_noisy,sigma_blur,lambda,num_iter,half_search_size,half_block_size, h)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Non-local means - psnr=%f dB", psnr(img_den,img)))
    fig2=imshow(img_dec,[0,1])
    title(sprintf("Non-local means deconvolution - psnr=%f dB", psnr(img_dec,img)))
    fig0.connect(fig1)
    fig1.connect(fig2)
    
end

% Image deconvolution/denoising for correlated noise using a Non-local means prior
function x_est : mat = deconv_nlmeans( _
    y : mat, _
    sigma_blur : scalar, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar)
    
    x_est = deconv_nlmeans2d(y, sigma_blur, lambda, num_iter, search_wnd, half_block_size, h)
    
end

% 2D Image deconvolution/denoising for correlated noise using a Non-local means prior
function x_est : mat = deconv_nlmeans2d( _
    y : mat, _
    sigma_blur : scalar, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar)
    
    inprod = (x,y) -> sum(x.*y)
    norm2 = x -> inprod(x,x)
    
    % steepest descent
    x_est = y
    for iter=0..num_iter-1
        [NLMS_x_est_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum2d(x_est, y, search_wnd, half_block_size, h)
        grad = 2*(gaussian_filter2d(gaussian_filter2d(x_est, sigma_blur), sigma_blur) - gaussian_filter2d(y, sigma_blur)) + _ 
               2*lambda*(NLMS_weights_cum.*x_est - NLMS_x_est_cum)
        H_grad = gaussian_filter2d(grad, sigma_blur)
        [NLMS_grad_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum2d(grad, y, search_wnd, half_block_size, h)
        temp = NLMS_weights_cum.*grad - NLMS_grad_cum
        alpha_step = (2*lambda*inprod(x_est,temp) - inprod(y-gaussian_filter2d(x_est, sigma_blur),H_grad)) / _
                     (2*lambda*inprod(grad,temp) + (norm2(H_grad)))
        x_est = x_est - alpha_step*grad
    end
    
end

% 3D Image deconvolution/denoising for correlated noise using a Non-local means prior
function x_est : cube = deconv_nlmeans3d( _
    y : cube, _
    sigma_blur : scalar, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar)
    
    inprod = (x,y) -> sum(x.*y)
    norm2 = x -> inprod(x,x)
    
    % steepest descent
    x_est = y
    for iter=0..num_iter-1
        [NLMS_x_est_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum3d(x_est, y, search_wnd, half_block_size, h)
        grad = 2*(gaussian_filter3d(gaussian_filter3d(x_est, sigma_blur), sigma_blur) - gaussian_filter3d(y, sigma_blur)) + _ 
               2*lambda*(NLMS_weights_cum.*x_est - NLMS_x_est_cum)
        H_grad = gaussian_filter3d(grad, sigma_blur)
        [NLMS_grad_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum3d(grad, y, search_wnd, half_block_size, h)
        temp = NLMS_weights_cum.*grad - NLMS_grad_cum
        alpha_step = (2*lambda*inprod(x_est,temp) - inprod(y-gaussian_filter3d(x_est, sigma_blur),H_grad)) / _
                     (2*lambda*inprod(grad,temp) + (norm2(H_grad)))
        x_est = x_est - alpha_step*grad
    end
    
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
    
    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum2d(img_noisy, img_orig, half_search_size, half_block_size, h)

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
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum2d( _
    img_noisy : mat, _
    img_orig : mat, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)
    
    % Extend images to avoid boundary artifacts
    img_noisy = bound_extension2d(img_noisy, half_block_size, half_block_size)
    img_orig = bound_extension2d(img_orig, half_block_size, half_block_size)
    
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
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum3d( _
    img_noisy : cube, _
    img_orig : cube, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)
    
    % Extend images to avoid boundary artifacts
    img_noisy = bound_extension3d(img_noisy, half_block_size, half_block_size, half_block_size)
    img_orig = bound_extension3d(img_orig, half_block_size, half_block_size, half_block_size)
    
    [z,y,x] = size(img_noisy)
    Bs = (2*half_block_size+1)^2

    % computation of the square difference
    function y = square_diff(x, d : vec)
        function [] = __kernel__ process_gray(x : cube'circular, y : cube'unchecked, d : ivec3, pos : ivec3)
            diff = x[pos] - x[pos + d]
            y[pos] = diff * diff
        end

        y = zeros(size(x,0..2))
        parallel_do(size(y),x,y,d,process_gray)
    end

    % A cyclic mean filter - (without normalization)
    function [] = moving_average_filter(img_in, img_out, half_block_size)
        function [] = __kernel__ mean_filter_z_cyclic(x : cube'circular, y : cube'unchecked, n : int, pos : ivec3)
            sum = 0.
            for i=pos[0]-n..pos[0]+n
                sum = sum + x[i,pos[1],pos[2]]
            end
            y[pos] = sum
        end
        
        function [] = __kernel__ mean_filter_y_cyclic(x : cube'circular, y : cube'unchecked, n : int, pos : ivec3)
            sum = 0.
            for i=pos[1]-n..pos[1]+n
                sum = sum + x[pos[0],i,pos[2]]
            end
            y[pos] = sum
        end
        
        function [] = __kernel__ mean_filter_x_cyclic(x : cube'circular, y : cube'unchecked, n : int, pos : ivec3)
            sum = 0.
            for i=pos[2]-n..pos[2]+n
                sum = sum + x[pos[0],pos[1],i]
            end
            y[pos] = sum
        end

        img_tmp1 = zeros(size(img_in))
        img_tmp2 = zeros(size(img_in))
        parallel_do (size(img_out), img_in, img_tmp1, half_block_size, mean_filter_z_cyclic)
        parallel_do (size(img_out), img_tmp1, img_tmp2, half_block_size, mean_filter_y_cyclic)
        parallel_do (size(img_out), img_tmp2, img_out, half_block_size, mean_filter_x_cyclic)
    end

    % weighting and accumulation step
    function [] = weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, d)

        function [] = __kernel__ process_gray(accum_mtx : cube'unchecked, accum_weight : cube'unchecked, _
                                              img_noisy : cube'circular, ssd : cube'circular, h : scalar, d : ivec3, pos : ivec3)
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

        parallel_do(size(img_noisy,0..2),accum_mtx,accum_weight,img_noisy,ssd,h, d, process_gray)
    end

    % weighting and accumulation step partially
    function [] = weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)

        function [] = __kernel__ process_gray(accum_mtx : cube'unchecked, accum_weight : cube'unchecked, _
                                              img_noisy : cube'circular, h : scalar, pos : ivec3)
            weight = 0.2 * (sqrt(0) < h) * (1 - ((0/Bs)/(h*h)))^8
            % weight = 0.01 * exp(h * 0)
            a = weight * img_noisy[pos]
            w = weight

            accum_mtx[pos] += a
            accum_weight[pos] += w
        end

        parallel_do(size(img_noisy,0..2),accum_mtx,accum_weight,img_noisy,h, process_gray)
    end
    
    accum_mtx = zeros(z,y,x)
    accum_weight = zeros(z,y,x)
    ssd = zeros(z,y,x)

    for md = -half_search_size..half_search_size
    for nd = -half_search_size..half_search_size
    for od = -half_search_size..half_search_size
        if (md > 0) || (md == 0 && nd > 0) || (md == 0 && nd == 0 && od > 0)
            square_diff_img = square_diff(img_orig, [md,nd,od])
            moving_average_filter(square_diff_img, ssd, half_block_size)
            weight_and_accumulate(accum_mtx, accum_weight, img_noisy, ssd, h, [md,nd,od])
        elseif md == 0 && nd == 0 && od == 0
            weight_and_accumulate_partial(accum_mtx, accum_weight, img_noisy, h)
        endif
    end
    end
    end
    
    % crop images
    img_noisy = img_noisy[half_block_size..z-half_block_size-1,half_block_size..y-half_block_size-1,half_block_size..x-half_block_size-1]
    accum_mtx = accum_mtx[half_block_size..z-half_block_size-1,half_block_size..y-half_block_size-1,half_block_size..x-half_block_size-1]
    accum_weight = accum_weight[half_block_size..z-half_block_size-1,half_block_size..y-half_block_size-1,half_block_size..x-half_block_size-1]

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

    img_est = denoise_nlmeans2d(img_noisy, half_search_size, half_block_size, h)
    
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
function img_est = denoise_nlmeans2d( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum2d(img_noisy, _
                                                         img_noisy, _
                                                         half_search_size : int, _
                                                         half_block_size : int, _
                                                         h : scalar)
    img_est = accum_mtx./accum_weight
    
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
function img_est = denoise_nlmeans3d( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum3d(img_noisy, _
                                                         img_noisy, _
                                                         half_search_size : int, _
                                                         half_block_size : int, _
                                                         h : scalar)
    img_est = accum_mtx./accum_weight
    
end
