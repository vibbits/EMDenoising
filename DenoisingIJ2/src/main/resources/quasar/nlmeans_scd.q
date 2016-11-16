% nlmeans_scd.q
% Image deconvolution/denoising for correlated, signal-dependent noise using a non-local prior
% 
% J. Roels, J. Aelterman, J. De Vylder, H.Q. Luong, Y. Saeys, W. Philips, 
% "Bayesian deconvolution of scanning electron microscopy images using point-spread function estimation and non-local regularization",  
% EMBC 2016, Proceedings: 443-447
% 
% Author: Joris Roels
% 
%   deconv_nlmeans_sc:              Image deconvolution/denoising for correlated, signal-dependent noise using a non-local prior
%   deconv_nlmeans_sc_opt:          Image deconvolution/denoising for correlated, signal-dependent noise using a non-local prior (optimized)

import "nlmeans_sc.q"

% Image deconvolution/denoising for correlated, signal-dependent noise using a Non-local means prior
function x_est : mat = deconv_nlmeans_sc( _
    y : mat, _
    H : mat, _
    lambda : scalar, _
    num_iter : int, _
    search_wnd : int, _
    half_block_size : int, _
    h : scalar, _
    sigma_0 : scalar, _
    alpha : scalar, _
    corr_filter_inv : mat)
    
    energy = x -> sum(x.^2)
    inprod = (x,y) -> sum(x.*y)
    norm2 = x -> inprod(x,x)
    
    H_T = flip_mat(H, ceil(size(H)/2)-1)
    % steepest descent
    x_est = y
    for iter=0..num_iter-1
        [NLMS_x_est_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum_sc(x_est, y, search_wnd, half_block_size, h, sigma_0, alpha, corr_filter_inv)
        grad = 2*(imfilter(imfilter(x_est,H,ceil(size(H)/2)-1,"mirror"),H_T,ceil(size(H_T)/2)-1,"mirror") - imfilter(y,H_T,ceil(size(H_T)/2)-1,"mirror")) + _
               2*lambda*(NLMS_weights_cum.*x_est - NLMS_x_est_cum)
        H_grad = imfilter(grad,H,ceil(size(H)/2)-1,"mirror");
        [NLMS_grad_cum, NLMS_weights_cum] = denoise_nlmeans_cumsum_sc(grad, y, search_wnd, half_block_size, h, sigma_0, alpha, corr_filter_inv)
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

% Generates gaussian filter of size N and standard deviation sigma
function [h:mat] = fgaussian(N:int, sigma:scalar)
    center = (N-1)/2
    n_min = -floor(center)
    n_max = ceil(center)
    hg = zeros(N,N)
    s = 0
    for i=n_min..n_max
        for j=n_min..n_max
            hg[i-n_min,j-n_min] = exp(-((i-(n_max-center))^2+(j-(n_max-center))^2)/(2*sigma^2))
            s = s + hg[i-n_min,j-n_min]
        end
    end
    h = hg ./ s
end

function [] = main()

    f_size = 15
    f_sigma = 1
    blur_kernel = fgaussian(f_size, f_sigma)

    sigma_0 = 20
    alpha = 0.05
    half_search_size = 5
    half_block_size = 4
    h = 13.5
    
    num_iter = 25
    lambda = 0.3
    
    img = imread("lena_big.tif")
    img = img[:,:,0]
    
    img_blurred = imfilter(img,blur_kernel,ceil(size(blur_kernel)/2)-1,"mirror")
    
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
    img_blurred_noisy = img_blurred+sigma.*corr_noise
    
    print "Image deconvolution using non-local prior"
    tic()
    img_deconv_sc = deconv_nlmeans_sc(img_blurred_noisy, blur_kernel, lambda, num_iter, half_search_size, half_block_size, h, sigma_0, alpha, em_corr_filter_inv)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig0=imshow(img_blurred_noisy)
    title(sprintf("Input image - psnr=%f dB", psnr(img_blurred_noisy,img)))
    fig1=imshow(img_deconv_sc)
    title(sprintf("Deconvolution with non-local prior - psnr=%f dB", psnr(img_deconv_sc,img)))

    fig0.connect(fig1)
end
