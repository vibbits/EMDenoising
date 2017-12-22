%=====================================================================
% Implementation of Tikhonov regularized denoising/deconvolution
%=====================================================================
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
    
    % add blur
    f_sigma = 1.5
    blur_kernel = fgaussian(size(img,0), f_sigma)
    img_blurred = real(fftshift2(ifft2(fft2(img).*fft2(blur_kernel))))
    
    % add noise
    sigma = 0.05
    img_noisy = img_blurred + sigma .* randn(size(img))
    
    % estimate noise level
%    sigma_est = estimate_noise_liu(img_noisy)
    sigma_est = sigma
    
    % params
    lambda1 = 0.5
    lambda2 = 1
    
    % denoising
    tic()
    img_den = tikhonov_denoise(img_noisy, lambda1)
    toc()
    % deconvolution
    tic()
    img_dec = tikhonov_denoise_dec(img_noisy, blur_kernel, lambda2)
    toc()
   
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    imshow(img_den,[0,1])
    title(sprintf("Tikhonov - psnr=%f dB", psnr(img_den,img)))
    imshow(img_dec,[0,1])
    title(sprintf("Tikhonov deconvolution - psnr=%f dB", psnr(img_dec,img)))

end

function x_est = tikhonov_denoise(y, lambda, num_iter=50)
    b = y
    x_est = y
    x_prev = y
    max_diff = 0.1
    r = b - compute_A(x_est, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        x_prev = x_est
        Ap = compute_A(p, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function Ax = compute_A(x, lambda)
    L = [[0,-1,0],[-1,4,-1],[0,-1,0]]
    Ax = x + lambda*imfilter(imfilter(x,L,[1,1],"mirror"),L,[1,1],"mirror")
end

function x_est = tikhonov_denoise_dec(y, H, lambda, num_iter=50)
    b = y
    x_est = y
    x_prev = y
    max_diff = 0.1
    r = b - compute_A_dec(x_est, H, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        x_prev = x_est
        Ap = compute_A_dec(p, H, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function Ax = compute_A_dec(x, H, lambda)
    L = [[0,-1,0],[-1,4,-1],[0,-1,0]]
    Ax = real(fftshift2(ifft2(fft2(x).*fft2(H)))) + 
         lambda*imfilter(imfilter(x,L,[1,1],"mirror"),L,[1,1],"mirror")
end

function r = inprod(v,w)
    r = sum(v.*w)
end
