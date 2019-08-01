%=====================================================================
% Implementation of Tikhonov regularized denoising/deconvolution
%=====================================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Filters"}

import "gaussian_filter.q"

%
% The main function - for testing the code
%
function [] = main()

    % read data
    img = imread("lena_big.tif")[:,:,1]/255
    
    % add blur
    sigma_blur = 1.5
    img_blurred = gaussian_filter(img, sigma_blur)

    
    % add noise
    sigma = 0.05
    img_noisy = img_blurred + sigma .* randn(size(img))
    
    % params
    lambda1 = 0.5
    lambda2 = 1.5
    
    % denoising
    tic()
    img_den = tikhonov_denoise(img_noisy, lambda1)
    toc()
    % deconvolution
    tic()
    img_dec = tikhonov_deconv(img_noisy, sigma_blur, lambda2)
    toc()
   
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    f=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    g=imshow(img_den,[0,1])
    title(sprintf("Tikhonov - psnr=%f dB", psnr(img_den,img)))
    h=imshow(img_dec,[0,1])
    title(sprintf("Tikhonov deconvolution - psnr=%f dB", psnr(img_dec,img)))
    f.connect(g)
    g.connect(h)

end

function x_est = tikhonov_denoise(y, lambda, num_iter=50)
    x_est = tikhonov_denoise2d(y, lambda, num_iter)
end

function x_est = tikhonov_denoise2d(y, lambda=1, num_iter=50)
    b = y
    x_est = y
    r = b - compute_A2d(x_est, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        Ap = compute_A2d(p, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function x_est = tikhonov_denoise3d(y, lambda=1, num_iter=50)
    b = y
    x_est = y
    x_prev = y
    max_diff = 0.1
    r = b - compute_A3d(x_est, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        x_prev = x_est
        Ap = compute_A3d(p, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function [] = __kernel__ imfilter2d(x : cube'mirror, y : cube'unchecked, h : mat'unchecked'const, n : int, pos : vec3)
    sum = 0.
    for i=0..2*n
    for j=0..2*n
        sum = sum + x[pos + [i-n,j-n,0]] * h[i,j]
    end
    end
    y[pos] = sum
end

function [] = __kernel__ imfilter3d(x : cube'mirror, y : cube'unchecked, h : cube'unchecked'const, n : int, pos : vec3)
    sum = 0.
    for i=0..2*n
    for j=0..2*n
    for k=0..2*n
        sum = sum + x[pos + [i-n,j-n,k-n]] * h[i,j,k]
    end
    end
    end
    y[pos] = sum
end

function Ax = compute_A2d(x, lambda)
    L = [[0,-1,0],[-1,4,-1],[0,-1,0]]
    tmp = zeros(size(x))
    Ax = zeros(size(x))
    parallel_do(size(x), x, tmp, L, 1, imfilter2d)
    parallel_do(size(x), tmp, Ax, L, 1, imfilter2d)
    Ax = x + lambda*Ax
end

function Ax = compute_A3d(x, lambda)
    L = zeros([3,3,3])
    L[1,1,1] = 6; L[0,1,1] = L[2,1,1] = L[1,0,1] = L[1,2,1] = L[1,1,0] = L[1,1,2] = -1
    tmp = zeros(size(x))
    Ax = zeros(size(x))
    parallel_do(size(x), x, tmp, L, 1, imfilter3d)
    parallel_do(size(x), tmp, Ax, L, 1, imfilter3d)
    Ax = x + lambda*Ax
end

function x_est = tikhonov_deconv(y, sigma_blur, lambda, num_iter=10)
    x_est = tikhonov_deconv2d(y, sigma_blur, lambda, num_iter)
end

function x_est = tikhonov_deconv2d(y, sigma_blur, lambda, num_iter=10)
    b = y
    x_est = y
    x_prev = y
    max_diff = 0.1
    r = b - compute_A_dec2d(x_est, sigma_blur, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        x_prev = x_est
        Ap = compute_A_dec2d(p, sigma_blur, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function x_est = tikhonov_deconv3d(y, sigma_blur, lambda, num_iter=10)
    b = y
    x_est = y
    x_prev = y
    max_diff = 0.1
    r = b - compute_A_dec3d(x_est, sigma_blur, lambda)
    p = r
    rsold = inprod(r,r)
    
    for i=1..num_iter
        x_prev = x_est
        Ap = compute_A_dec3d(p, sigma_blur, lambda)
        alpha = rsold / inprod(p,Ap)
        x_est = x_est + alpha*p
        r = r - alpha*Ap
        rsnew = inprod(r,r)
        p = r + (rsnew/rsold)*p
        rsold = rsnew
    end
end

function Ax = compute_A_dec2d(x:mat, sigma_blur, lambda)
    L = [[0,-1,0],[-1,4,-1],[0,-1,0]]
    tmp = zeros(size(x))
    Ax = zeros(size(x))
    Hx = gaussian_filter2d(x, sigma_blur)
    parallel_do(size(x), x, tmp, L, 1, imfilter2d)
    parallel_do(size(x), tmp, Ax, L, 1, imfilter2d)
    Ax = Hx + lambda*Ax
end

function Ax = compute_A_dec3d(x:cube, sigma_blur, lambda)
    L = zeros([3,3,3])
    L[1,1,1] = 6; L[0,1,1] = L[2,1,1] = L[1,0,1] = L[1,2,1] = L[1,1,0] = L[1,1,2] = -1
    tmp = zeros(size(x))
    Ax = zeros(size(x))
    Hx = gaussian_filter3d(x, sigma_blur)
    parallel_do(size(x), x, tmp, L, 1, imfilter3d)
    parallel_do(size(x), tmp, Ax, L, 1, imfilter3d)
    Ax = Hx + lambda*Ax
end

function r = inprod(v,w)
    r = sum(v.*w)
end