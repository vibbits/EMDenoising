%==================================================
% Implementation of anisotropic diffusion denoising
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

    % read data
    img = imread("lena_big.tif")[:,:,0]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % set parameters
    niter = 5
    dt = 0.2
    k = 2.90186309814453*sigma^2 + 1.53053665161133*sigma + 0.00475215911865234
    
    % denoising
    tic()
    img_den = denoise_anisotropic_diffusion(img_noisy,niter,dt,k)
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

function [] = __kernel__ diff2d(x : cube, y : cube, K : scalar, alpha : scalar, deltat : scalar, pos : ivec3)

    deltaIxu = x[pos[0], pos[1]+1, pos[2]] - x[pos[0], pos[1], pos[2]]
    deltaIxl = x[pos[0], pos[1], pos[2]]   - x[pos[0], pos[1]-1, pos[2]]
    
    deltaIyu = x[pos[0]+1, pos[1], pos[2]] - x[pos[0], pos[1], pos[2]]
    deltaIyl = x[pos[0], pos[1], pos[2]]   - x[pos[0]-1, pos[1], pos[2]]

    cxu = 1.0 ./ (1.0 + pow(abs(deltaIxu) / K, 1 + alpha))
    cxl = 1.0 ./ (1.0 + pow(abs(deltaIxl) / K, 1 + alpha))
    
    cyu = 1.0 ./ (1.0 + pow(abs(deltaIyu) / K, 1 + alpha))
    cyl = 1.0 ./ (1.0 + pow(abs(deltaIyl) / K, 1 + alpha))
    
    deltaI = (cxu .* deltaIxu - cxl .* deltaIxl) + (cyu .* deltaIyu - cyl .* deltaIyl)
    
    y[pos[0], pos[1], pos[2]] = x[pos[0], pos[1], pos[2]] + deltat * deltaI
end

function [] = __kernel__ diff3d(x : cube, y : cube, K : scalar, alpha : scalar, deltat : scalar, pos : ivec3)

    deltaIxu = x[pos[0], pos[1]+1, pos[2]] - x[pos[0], pos[1], pos[2]]
    deltaIxl = x[pos[0], pos[1], pos[2]]   - x[pos[0], pos[1]-1, pos[2]]
    
    deltaIyu = x[pos[0]+1, pos[1], pos[2]] - x[pos[0], pos[1], pos[2]]
    deltaIyl = x[pos[0], pos[1], pos[2]]   - x[pos[0]-1, pos[1], pos[2]]
    
    deltaIzu = x[pos[0], pos[1], pos[2]+1] - x[pos[0], pos[1], pos[2]]
    deltaIzl = x[pos[0], pos[1], pos[2]]   - x[pos[0], pos[1], pos[2]-1]

    cxu = 1.0 ./ (1.0 + pow(abs(deltaIxu) / K, 1 + alpha))
    cxl = 1.0 ./ (1.0 + pow(abs(deltaIxl) / K, 1 + alpha))
    
    cyu = 1.0 ./ (1.0 + pow(abs(deltaIyu) / K, 1 + alpha))
    cyl = 1.0 ./ (1.0 + pow(abs(deltaIyl) / K, 1 + alpha))
    
    czu = 1.0 ./ (1.0 + pow(abs(deltaIzu) / K, 1 + alpha))
    czl = 1.0 ./ (1.0 + pow(abs(deltaIzl) / K, 1 + alpha))
    
    deltaI = (cxu .* deltaIxu - cxl .* deltaIxl) + (cyu .* deltaIyu - cyl .* deltaIyl) + (czu .* deltaIzu - czl .* deltaIzl)
    
    y[pos[0], pos[1], pos[2]] = x[pos[0], pos[1], pos[2]] + deltat * deltaI
end

% Function: denoise_anisotropic_diffusion2d
% 
% 2D Anisotropic diffusion denoising
% 
% Usage:
%   : function [img_den : mat] = denoise_anisotropic_diffusion2d(img : mat, niter : int = 5, dt : scalar = 0.2, k : scalar = 40, alpha : scalar = 1)
% 
% Parameters:
% img - noisy input image
% niter - number of diffusion iterations
% dt - time step
% k - damping parameter for diffusion function
% 
% Returns:
% img_den - denoised image
function y:cube = denoise_anisotropic_diffusion2d(x:cube,num_iter:int=50,dt:scalar=0.2,k:scalar=40,alpha:scalar=1)
    
    y = copy(x)
    tmp = zeros(size(x))
    
    for iter = 1..num_iter
        parallel_do(size(x), y, tmp, k, alpha, dt, diff2d)
        y = copy(tmp)
    end
    
end

% Function: denoise_anisotropic_diffusion3d
% 
% 3D Anisotropic diffusion denoising
% 
% Usage:
%   : function [img_den : mat] = denoise_anisotropic_diffusion3d(img : mat, niter : int = 5, dt : scalar = 0.2, k : scalar = 40, alpha : scalar = 1)
% 
% Parameters:
% img - noisy input image
% niter - number of diffusion iterations
% dt - time step
% k - damping parameter for diffusion function
% 
% Returns:
% img_den - denoised image
function y:cube = denoise_anisotropic_diffusion3d(x:cube,num_iter:int=50,dt:scalar=0.2,k:scalar=40,alpha:scalar=1)
    
    y = copy(x)
    tmp = zeros(size(x))
    
    for iter = 1..num_iter
        parallel_do(size(x), y, tmp, k, alpha, dt, diff3d)
        y = copy(tmp)
    end
    
end

% Function: denoise_anisotropic_diffusion
% 
% Anisotropic diffusion denoising
% 
% Usage:
%   : function [img_den : mat] = denoise_anisotropic_diffusion(img : mat, niter : int = 5, dt : scalar = 0.2, k : scalar = 40, alpha : scalar = 1)
% 
% Parameters:
% img - noisy input image
% niter - number of diffusion iterations
% dt - time step
% k - damping parameter for diffusion function
% 
% Returns:
% img_den - denoised image
function y:cube = denoise_anisotropic_diffusion(x:cube,num_iter:int=50,dt:scalar=0.2,k:scalar=40,alpha:scalar=1)
    
    y = denoise_anisotropic_diffusion2d(x,num_iter,dt,k,alpha)
    
end