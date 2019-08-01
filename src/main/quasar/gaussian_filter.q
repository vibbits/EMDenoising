%==================================================
% Illustration of the gaussian filter
%==================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration"}

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
    
    % set parameters
    sigma_filter = 8.06167602539063*sigma + 0.534878730773926
    
    % denoising
    tic()
    img_den = gaussian_filter(img_noisy, sigma_filter)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Gaussian filter - psnr=%f dB", psnr(img_den,img)))
    fig0.connect(fig1)

end

% Function: gaussian_filter
% 
% Applies a Gaussian filter to the image
% 
% Usage:
%   : function [y] = gaussian_filter(x, sigma = 1)
% 
% Parameters:
% x - noisy input image
% sigma - standard deviation of the gaussian
% 
% Returns:
% y - denoised image
function y = gaussian_filter(x, sigma=1)

    y = gaussian_filter2d(x, sigma)
    
end

% Function: gaussian_filter2d
% 
% Applies a 2D Gaussian filter to the image
% 
% Usage:
%   : function [y] = gaussian_filter2d(x, sigma = 1)
% 
% Parameters:
% x - noisy input image
% sigma - standard deviation of the gaussian
% 
% Returns:
% y - denoised image
function y = gaussian_filter2d(x : mat, sigma=1)

    function [] = __kernel__ gaussian_filter_x(x : cube'mirror, y : cube'unchecked, fc : vec'unchecked'const, n : int, pos : vec3)
        sum = 0.
        for i=0..2*n
            sum = sum + x[pos + [0,i-n,0]] * fc[i]
        end
        y[pos] = sum
    end

    function [] = __kernel__ gaussian_filter_y(x : cube'mirror, y : cube'unchecked, fc : vec'unchecked'const, n : int, pos : vec3)
        sum = 0.
        for i=0..2*n
            sum = sum + x[pos + [i-n,0,0]] * fc[i]
        end
        y[pos] = sum
    end

    % computation of the filter coefficients
    n = 4
    fc = exp(-0.5*(-n..n).^2/sigma)
    fc = fc / sum(fc)

    y = zeros(size(x))
    tmp = zeros(size(x))
    parallel_do (size(y), x, tmp, fc, n, gaussian_filter_x)
    parallel_do (size(y), tmp, y, fc, n, gaussian_filter_y)
end

% Function: gaussian_filter3d
% 
% Applies a 3D Gaussian filter to the image
% 
% Usage:
%   : function [y] = gaussian_filter3d(x, sigma = 1)
% 
% Parameters:
% x - noisy input image
% sigma - standard deviation of the gaussian
% 
% Returns:
% y - denoised image
function y = gaussian_filter3d(x : cube, sigma=1)

    function [] = __kernel__ gaussian_filter_x(x : cube'mirror, y : cube'unchecked, fc : vec'unchecked'const, n : int, pos : vec3)
        sum = 0.
        for i=0..2*n
            sum = sum + x[pos + [0,i-n,0]] * fc[i]
        end
        y[pos] = sum
    end

    function [] = __kernel__ gaussian_filter_y(x : cube'mirror, y : cube'unchecked, fc : vec'unchecked'const, n : int, pos : vec3)
        sum = 0.
        for i=0..2*n
            sum = sum + x[pos + [i-n,0,0]] * fc[i]
        end
        y[pos] = sum
    end

    function [] = __kernel__ gaussian_filter_z(x : cube'mirror, y : cube'unchecked, fc : vec'unchecked'const, n : int, pos : vec3)
        sum = 0.
        for i=0..2*n
            sum = sum + x[pos + [0,0,i-n]] * fc[i]
        end
        y[pos] = sum
    end

    % computation of the filter coefficients
    n = 4
    fc = exp(-0.5*(-n..n).^2/sigma)
    fc = fc / sum(fc)

    y = zeros(size(x))
    tmp1 = zeros(size(x))
    tmp2 = zeros(size(x))
    parallel_do (size(y), x, tmp1, fc, n, gaussian_filter_x)
    parallel_do (size(y), tmp1, tmp2, fc, n, gaussian_filter_y)
    parallel_do (size(y), tmp2, y, fc, n, gaussian_filter_z)
end