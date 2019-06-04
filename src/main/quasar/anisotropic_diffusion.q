%==================================================
% Implementation of anisotropic diffusion denoising
%==================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration"}

import "estimate_noise.q"

% Function: main
% 
% Testing function
% 
% Usage:
%   : function [] = main()
function [] = main()

    % read data
    img = imread("lena_big.tif")[:,:,0]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % estimate noise level
    sigma_est = estimate_noise_liu(img_noisy)
    
    % set parameters
    niter = 5
    dt = 0.2
    k = 2.90186309814453*sigma_est^2 + 1.53053665161133*sigma_est + 0.00475215911865234
    diff_type = "exp" % either "exp" or "quad"
    
    % denoising
    tic()
    img_den = denoise_anisotropic_diffusion(img_noisy,niter,dt,k,diff_type)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Anisotropic diffusion - psnr=%f dB", psnr(img_den,img)))
    fig0.connect(fig1)
    
end

% Function: anisotropic_diffusion
% 
% Anisotropic diffusion denoising
% 
% Usage:
%   : function [img_den : mat] = denoise_anisotropic_diffusion(img : mat, niter : int = 5, dt : scalar = 0.2, k : scalar = 40, fmode : string'const = "exp")
% 
% Parameters:
% img - noisy input image
% niter - number of diffusion iterations
% dt - time step
% k - damping parameter for diffusion function
% diff_type - type of diffusion function ("exp" or "quad")
% 
% Returns:
% img_den - denoised image
function img_den:mat = denoise_anisotropic_diffusion(img:mat,niter:int=5,dt:scalar=0.2,k:scalar=40,diff_type:string="exp")
    % initialization
    img_den = copy(img)
    dx = 1
    dy = 1
    dd = sqrt(2)
    g_n = zeros(size(img))
    g_e = zeros(size(img))
    g_s = zeros(size(img))
    g_w = zeros(size(img))
    g_ne = zeros(size(img))
    g_se = zeros(size(img))
    g_sw = zeros(size(img))
    g_nw = zeros(size(img))
    
    % kernels
    function [] = __kernel__ gradN(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]-1,pos[1]] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradE(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0],pos[1]+1] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradS(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]+1,pos[1]] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradW(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0],pos[1]-1] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradNE(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]-1,pos[1]+1] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradSE(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]+1,pos[1]+1] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradSW(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]+1,pos[1]-1] - x[pos[0],pos[1]]
    end
    function [] = __kernel__ gradNW(g:mat,x:mat,pos:ivec2)
        g[pos[0],pos[1]] = x[pos[0]-1,pos[1]-1] - x[pos[0],pos[1]]
    end
    
    % set diffusion function
    match diff_type with
    | "exp" ->  diff = x -> exp(-(x./k).^2)    % exponential difference function (default)
    | "quad" -> diff = x -> 1./(1+(x./k).^2)   % quadratic difference function
    | _ ->      diff = x -> exp(-(x./k).^2)
    end
    
    % start iterating
    for n=0..niter-1
        % compute gradients
        parallel_do(size(img),g_n,img_den,gradN)
        parallel_do(size(img),g_e,img_den,gradE)
        parallel_do(size(img),g_s,img_den,gradS)
        parallel_do(size(img),g_w,img_den,gradW)
        parallel_do(size(img),g_ne,img_den,gradNE)
        parallel_do(size(img),g_se,img_den,gradSE)
        parallel_do(size(img),g_sw,img_den,gradSW)
        parallel_do(size(img),g_nw,img_den,gradNW)
        
        % compute diffusion function
        cN = diff(g_n)
        cE = diff(g_e)
        cS = diff(g_s)
        cW = diff(g_w)
        cNE = diff(g_ne)
        cSE = diff(g_se)
        cSW = diff(g_sw)
        cNW = diff(g_nw)
        
        % diffusion update
        img_den += dt*( (1/(dy^2))*cN.*g_n + (1/(dy^2))*cS.*g_s + _
                        (1/(dx^2))*cW.*g_w + (1/(dx^2))*cE.*g_e + _
                        (1/(dd^2))*cNE.*g_ne + (1/(dd^2))*cSE.*g_se + _
                        (1/(dd^2))*cSW.*g_sw + (1/(dd^2))*cNW.*g_nw )
    end
end