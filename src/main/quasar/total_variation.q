%========================================================
% Implementation of total variation regularized denoising
%========================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Filters"}

import "imfilter.q"
import "utils.q"

%
% The main function - for testing the code
%
function [] = main()

    % read data
    img = imread("lena_big.tif")[:,:,1]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % estimate noise level
%    sigma_est = estimate_noise_liu(img_noisy)
    sigma_est = sigma
    
    % params
    lambda = 0.05
    
    % denoising
    tic()
    img_den = total_variation_denoise(img_noisy, lambda)
    toc()
   
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    imshow(img_noisy,[])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    imshow(img_den,[0,1])
    title(sprintf("TV - psnr=%f dB", psnr(img_den,img)))

end

function g = grad_tv(x : cube)
    d = 1e-10
    dxh_fw = imfilter(x,[-1,1,0],[0,1],"mirror")
    dxv_fw = imfilter(x,transpose([-1,1,0]),[1,0],"mirror")
    dxh_bw = imfilter(x,[0,-1,1],[0,1],"mirror")
    dxv_bw = imfilter(x,transpose([0,-1,1]),[1,0],"mirror")
    g = ((dxh_fw+dxv_fw)./max(sqrt(dxh_fw.^2 + dxv_fw.^2),d)) - 
        ((dxh_bw+dxv_bw)./max(sqrt(dxh_bw.^2 + dxv_bw.^2),d))
end

function x_est = total_variation_denoise(y : cube, lambda : scalar, num_iter : int = 100, alpha : scalar = 0.01)
    x_est = y
    for i=1..num_iter
        x_est = x_est - alpha*((x_est - y) + lambda*grad_tv(x_est))
    end
end

