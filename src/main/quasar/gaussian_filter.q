%==================================================
% Illustration of the gaussian filter
%==================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration"}

import "imfilter.q"
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
    sigma_filter = 8.06167602539063*sigma_est + 0.534878730773926
    
    % denoising
    tic()
    img_den = gaussian_filter(img_noisy, sigma_filter, 0, "mirror")
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