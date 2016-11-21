% nlmeans_sc.q
% Image denoising for correlated, signal-dependent noise using a Non-local means-based filter
% 
% J. Roels, J. Aelterman, J. De Vylder, H.Q. Luong, Y. Saeys, S. Lippens, W. Philips, 
% "Noise Analysis and Removal in 3D Electron Microscopy", in Lecture Notes in 
% Computer Science 8888 (Advances in Visual Computing), pp. 31-40, Las Vegas, 
% USA, December 8-10, 2014.
% 
% Author: Joris Roels
% Based on the Non-local means implementation of Bart Goossens

import "imfilter.q"

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
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_sc( _
    img_noisy, _
    img_orig, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    sigma_0 : scalar, _
    alpha : scalar, _
    corr_filter_inv: mat)

    % prewhitened image
    img_prewhit = imfilter(img_orig, corr_filter_inv, [(size(corr_filter_inv,0)-1)/2,(size(corr_filter_inv,1)-1)/2], "mirror");
    img_prewhit = img_prewhit ./ sqrt(alpha.*img_orig+sigma_0)
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
%   sigma_0: base standard deviation of the noise (standard deviation in absence of signal)
%   alpha: signal dependency parameter
%   corr_filter_inv: inverse impulse response of noise correlation kernel
function img_est = denoise_nlmeans_sc( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    sigma_0 : scalar, _
    alpha : scalar, _
    corr_filter_inv: mat)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_sc(img_noisy, _
                                                          img_noisy, _
                                                          half_search_size : int, _
                                                          half_block_size : int, _
                                                          h : scalar, _
                                                          sigma_0 : scalar, _
                                                          alpha : scalar, _
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
function [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_s( _
    img_noisy, _
    img_orig, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    sigma_0 : scalar, _
    alpha : scalar)

    % prewhitened image
    img_prewhit = img_orig ./ sqrt(alpha.*img_orig+sigma_0)
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
%   sigma_0: base standard deviation of the noise (standard deviation in absence of signal)
%   alpha: signal dependency parameter
%   corr_filter_inv: inverse impulse response of noise correlation kernel
function img_est = denoise_nlmeans_s( _
    img_noisy, _
    half_search_size : int, _
    half_block_size : int, _
    h : scalar, _
    sigma_0 : scalar, _
    alpha : scalar)

    [accum_mtx, accum_weight] = denoise_nlmeans_cumsum_s(img_noisy, _
                                                          img_noisy, _
                                                          half_search_size : int, _
                                                          half_block_size : int, _
                                                          h : scalar, _
                                                          sigma_0 : scalar, _
                                                          alpha : scalar)
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
% %   sigma_0: base standard deviation of the noise (standard deviation in absence of signal)
% %   alpha: signal dependency parameter
% %   corr_filter_inv: inverse impulse response of noise correlation kernel
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

% Image denoising for correlated, signal-dependent noise using a Non-local means-based filter
%   img_noisy: the image to be denoised
%   img_orig: the image of which the weights will be based upon
%   half_search_size: half the size of the search space where patches should be compared
%   half_block_size: half the size of the patches to compare
%   h: parameter to control the weighting function
% %   sigma_0: base standard deviation of the noise (standard deviation in absence of signal)
% %   alpha: signal dependency parameter
% %   corr_filter_inv: inverse impulse response of noise correlation kernel
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
function [img_ext:mat] = bound_extension(img:mat, y_ext:int, x_ext:int, type_ext:string)
    [Ny,Nx] = size(img)
    img_ext = zeros(Ny+2*y_ext,Nx+2*x_ext)
    img_ext[y_ext..Ny+y_ext-1,x_ext..Nx+x_ext-1] = img
    
    if type_ext == "mirror"
        img_ext[0..y_ext-1,:] = img_ext[2*y_ext-1..-1..y_ext,:]
        img_ext[:,0..x_ext-1] = img_ext[:,2*x_ext-1..-1..x_ext]
        img_ext[Ny+y_ext..Ny+2*y_ext-1,:] = img_ext[Ny+y_ext-1..-1..Ny,:]
        img_ext[:,Nx+x_ext..Nx+2*x_ext-1] = img_ext[:,Nx+x_ext-1..-1..Nx]
        img_ext[0..y_ext-1,0..x_ext] = img_ext[2*y_ext-1..-1..y_ext,2*x_ext-1..-1..x_ext]
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

% main function
function [] = main()
    
    sigma_0 = 20
    alpha = 0.05
    half_search_size = 5
    half_block_size = 4
    h1 = 60
    h2 = 10
    h3 = 13.5
    
    img = imread("lena_big.tif") 
    img = img[:,:,0]
    
    em_corr_filter = [ 0.000731065652886,_
                       0.000065153641174,_
                       0.000326740756258,_
                       0.000270675041410,_
                      -0.000270801448761,_
                      -0.001122833642968,_
                      -0.004546635809409,_
                      -0.012741935522000,_
                      -0.033561792758809,_
                      -0.071377870761527,_
                      -0.113814349008347,_
                       0.122897508112658,_
                       0.629735356970885,_
                       0.122897508112658,_
                      -0.113814349008347,_
                      -0.071377870761527,_
                      -0.033561792758809,_
                      -0.012741935522000,_
                      -0.004546635809409,_
                      -0.001122833642968,_
                      -0.000270801448761,_
                       0.000270675041410,_
                       0.000326740756258,_
                       0.000065153641174,_
                       0.000731065652886]
    
    em_corr_filter_inv =  [0.003548810180648,_
                           0.006457459824059,_
                           0.007150416544695,_
                           0.010395250498662,_
                           0.018758809056068,_
                           0.021360913009926,_
                           0.045297563880590,_
                           0.039260499682212,_
                           0.123410138059489,_
                           0.022063139838911,_
                           0.443138357376189,_
                          -0.479376389377209,_
                           1.955721404909547,_
                          -0.479376389377209,_
                           0.443138357376189,_
                           0.022063139838911,_
                           0.123410138059488,_
                           0.039260499682212,_
                           0.045297563880590,_
                           0.021360913009926,_
                           0.018758809056068,_
                           0.010395250498662,_
                           0.007150416544695,_
                           0.006457459824060,_
                           0.003548810180647]
    
    noise = randn(size(img,0),size(img,1))
    corr_noise = imfilter(noise, em_corr_filter, [(size(em_corr_filter,0)-1)/2,(size(em_corr_filter,1)-1)/2], "mirror")
    corr_noise = corr_noise ./ std(corr_noise)
    sigma = sqrt((sigma_0*sigma_0).*ones(size(img,0),size(img,1)) + alpha.*img)
    img_noisy = img+sigma.*corr_noise
    
    print "Non-vector based NLMeans filter (correlation and signal dependency adjustment adjustment)"
    tic()
    img_est1 = denoise_nlmeans(img_noisy, half_search_size, half_block_size, h1)
    toc()
    tic()
    img_est2 = denoise_nlmeans_s(img_noisy, half_search_size, half_block_size, h2, sigma_0, alpha)
    toc()
    tic()
    img_est3 = denoise_nlmeans_sc(img_noisy, half_search_size, half_block_size, h3, sigma_0, alpha, em_corr_filter_inv)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig1=imshow(img_noisy)
    title(sprintf("Noisy image - psnr=%f dB", psnr(img_noisy,img)))
    fig2=imshow(img_est1)
    title(sprintf("NLMS - psnr=%f dB", psnr(img_est1,img)))
    fig3=imshow(img_est2)
    title(sprintf("NLMS-S - psnr=%f dB", psnr(img_est2,img)))
    fig4=imshow(img_est3)
    title(sprintf("NLMS-SC - psnr=%f dB", psnr(img_est3,img)))

    fig1.connect(fig2)
    fig2.connect(fig3)
    fig3.connect(fig4)
    
end