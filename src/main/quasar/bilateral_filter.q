%=====================================================================
% Fast bilateral filter implementation of
%   K. N. Chaudhury, D. Sage, and M. Unser, 
%   "Fast O(1) bilateral filtering using trigonometric range kernels," 
%   IEEE Transactions on Image Processing
%=====================================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Filters"}

import "gaussian_filter.q"

% Function: main
% 
% Testing function
% 
% Usage:
%   : function [] = main()
function [] = main()
    
    % load image
    img = imread("lena_big.tif")[:,:,1]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % set parameters
    s_r = 16 % 50
    s_s = 1.5
    
    % denoising
    tic()
    img_den = bilateral_filter_denoise(img_noisy*255, s_r, s_s, 255)/255
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("Bilateral filter - psnr=%f dB", psnr(img_den,img)))
    fig0.connect(fig1)
    
end

% Function: bilateral_filter2d
% 
% 2D Fast bilateral filter implementation of
%   K. N. Chaudhury, D. Sage, and M. Unser, 
%   "Fast O(1) bilateral filtering using trigonometric range kernels," 
%   IEEE Transactions on Image Processing
% 
% Usage:
%   : function [y : cube] = bilateral_filter2d(x : cube'const, s_r : scalar, s_s : scalar, T : int = 255)
% 
% Parameters:
% x - input image
% s_r - range damping parameter
% s_s - spatial damping parameter
% T - dynamic range
%
% Notes:
% "Small" values of s_r would lead to a large number of iterations N and to numeric accuracy issues
% in the implementation. To avoid this the number of iterations N will be limited to 100. This
% corresponds to a minimum s_r value of (2*T)/pi/sqrt(Nmax) with Nmax=100.
% 
% Returns:
% y - filtered image
function [y:cube] = bilateral_filter2d(x:cube, s_r:scalar=16, s_s:scalar=0.5, T:int=255)

    Nmax = 100

    gamma = pi/(2*T)
    rho = gamma*s_r
    if s_r > 1/(gamma.^2)
        N = Nmax
    else
        N = floor(1/(rho.^2))
    endif
    
    % Avoid values of N larger than Nmax (about 100) because of numeric overflow issues
    % (caused by the 1/2^N and the combinatorial function in the code below).
    N = min(N, Nmax)
        
    v = gamma * x / (rho * sqrt(N))
    num = zeros(size(x))
    den = zeros(size(x))
    for n = 0..N
        h = cos((2*n-N)*v)
        g = x .* h
        d = (1 / 2^N) * comb(N, n) * h
        hh = gaussian_filter2d(h, s_s)
        gg = gaussian_filter2d(g, s_s)
        num = num + d .* gg
        den = den + d .* hh
    end
    y = num./den  
      
end

% Function: bilateral_filter3d
% 
% 3D Fast bilateral filter implementation of
%   K. N. Chaudhury, D. Sage, and M. Unser, 
%   "Fast O(1) bilateral filtering using trigonometric range kernels," 
%   IEEE Transactions on Image Processing
% 
% Usage:
%   : function [y : cube] = bilateral_filter3d(x : cube'const, s_r : scalar, s_s : scalar, T : int = 255)
% 
% Parameters:
% x - input image
% s_r - range damping parameter
% s_s - spatial damping parameter
% T - dynamic range
%
% Notes:
% "Small" values of s_r would lead to a large number of iterations N and to numeric accuracy issues
% in the implementation. To avoid this the number of iterations N will be limited to 100. This
% corresponds to a minimum s_r value of (2*T)/pi/sqrt(Nmax) with Nmax=100.
% 
% Returns:
% y - filtered image
function [y:cube] = bilateral_filter3d(x:cube, s_r:scalar=16, s_s:scalar=0.5, T:int=255)

    Nmax = 100

    gamma = pi/(2*T)
    rho = gamma*s_r
    if s_r > 1/(gamma.^2)
        N = Nmax
    else
        N = floor(1/(rho.^2))
    endif
    
    % Avoid values of N larger than Nmax (about 100) because of numeric overflow issues
    % (caused by the 1/2^N and the combinatorial function in the code below).
    N = min(N, Nmax)
        
    v = gamma * x / (rho * sqrt(N))
    num = zeros(size(x))
    den = zeros(size(x))
    for n = 0..N
        h = cos((2*n-N)*v)
        g = x .* h
        d = (1 / 2^N) * comb(N, n) * h
        hh = gaussian_filter3d(h, s_s)
        gg = gaussian_filter3d(g, s_s)
        num = num + d .* gg
        den = den + d .* hh
    end
    y = num./den    
    
end

% Function: bilateral_filter_denoise
% 
% Fast bilateral filter implementation of
%   K. N. Chaudhury, D. Sage, and M. Unser, 
%   "Fast O(1) bilateral filtering using trigonometric range kernels," 
%   IEEE Transactions on Image Processing
% 
% Usage:
%   : function [y : cube] = bilateral_filter_denoise(x : cube'const, s_r : scalar, s_s : scalar, T : int = 255)
% 
% Parameters:
% x - input image
% s_r - range damping parameter
% s_s - spatial damping parameter
% T - dynamic range
%
% Notes:
% "Small" values of s_r would lead to a large number of iterations N and to numeric accuracy issues
% in the implementation. To avoid this the number of iterations N will be limited to 100. This
% corresponds to a minimum s_r value of (2*T)/pi/sqrt(Nmax) with Nmax=100.
% 
% Returns:
% y - filtered image
function [y:cube] = bilateral_filter_denoise(x:cube, s_r:scalar, s_s:scalar, T:int=255)

    y = bilateral_filter2d(x, s_r, s_s, T)
        
end

% Function: comb
% 
% Compute the binomial coefficient of n over r
% 
% Usage:
%   : function [c] = comb(n : ??'const, r : ??'const)
% 
% Parameters:
% n - total number of objects
% r - number of selected objects
% 
% Returns:
% c - binomial coefficient of n over r
function c:scalar = comb(n,r)
    % use scalars to avoid integer overflow
    num:scalar = n
    den:scalar = 1
    for i=2..r
        num = num * (n-i+1)
        den = den * i
    end
    c = num/den
end
