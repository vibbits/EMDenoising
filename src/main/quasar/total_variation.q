%========================================================
% Implementation of total variation regularized denoising
%========================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Filters"}

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

function g = grad_tv(img : mat)
    g = grad_tv2d(img)
end

function g = grad_tv2d(img : mat)
    d = 1e-10
    [x,y] = size(img)
    img = bound_extension2d(img, 1, 1)
    dxh_fw = img[1..x,1..y] - img[0..x-1,1..y]
    dxv_fw = img[1..x,1..y] - img[1..x,0..y-1]
    dxh_bw = img[2..x+1,1..y] - img[1..x,1..y]
    dxv_bw = img[1..x,2..y+1] - img[1..x,1..y]
    g = ((dxh_fw+dxv_fw)./max(sqrt(dxh_fw.^2 + dxv_fw.^2),d)) - 
        ((dxh_bw+dxv_bw)./max(sqrt(dxh_bw.^2 + dxv_bw.^2),d))
end

function g = grad_tv3d(img : cube)
    d = 1e-10
    [x,y,z] = size(img)
    img = bound_extension3d(img, 1, 1, 1)
    dxh_fw = img[1..x,1..y,1..z] - img[0..x-1,1..y,1..z]
    dxh_bw = img[2..x+1,1..y,1..z] - img[1..x,1..y,1..z]
    dxv_fw = img[1..x,1..y,1..z] - img[1..x,0..y-1,1..z]
    dxv_bw = img[1..x,2..y+1,1..z] - img[1..x,1..y,1..z]
    dxd_fw = img[1..x,1..y,1..z] - img[1..x,1..y,0..z-1]
    dxd_bw = img[1..x,1..y,1..z] - img[1..x,1..y,2..z+1]
    g = ((dxh_fw+dxv_fw+dxd_fw)./max(sqrt(dxh_fw.^2 + dxv_fw.^2 + dxd_fw.^2),d)) - 
        ((dxh_bw+dxv_bw+dxd_bw)./max(sqrt(dxh_bw.^2 + dxv_bw.^2 + dxd_bw.^2),d))
end

function x_est = total_variation_denoise(y : mat, lambda : scalar, num_iter : int = 100, alpha : scalar = 0.01)
    x_est = total_variation_denoise2d(y, lambda, num_iter, alpha)
end

function x_est = total_variation_denoise2d(y : mat, lambda : scalar, num_iter : int = 100, alpha : scalar = 0.01)
    x_est = y
    for i=1..num_iter
        x_est = x_est - alpha*((x_est - y) + lambda*grad_tv2d(x_est))
    end
end

function x_est = total_variation_denoise3d(y : cube, lambda : scalar, num_iter : int = 100, alpha : scalar = 0.01)
    x_est = y
    for i=1..num_iter
        x_est = x_est - alpha*((x_est - y) + lambda*grad_tv3d(x_est))
    end
end
