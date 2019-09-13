%REQUIRES_DISPLAY
%==================================================
% Contains two noise estimation functions: 
%   - Mean Absolute Deviation: fast, but less accurate
%   - Method proposed by Liu et al.: accurate, but slower
%       X. Liu, M. Tanaka and M. Okutomi, 
%       "Noise level estimation using weak textured patches of a single noisy image," 
%       2012 19th IEEE International Conference on Image Processing, Orlando, FL, 2012, pp. 665-668
% March 2017 - Joris Roels
% Note: the Liu noise estimator is work in progress and currently not used in the denoising plugin.
%==================================================

{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration"}

import "system.q"
import "immedfilt.q"
import "linalg.q"
import "imfilter.q"

function [] = main()
    % read data
    img = imread("lena_big.tif")[:,:,1]
    
    %  noise levels
    sigmas = 5..5..100
    sigmas_mad = zeros(size(sigmas))
    sigmas_liu = zeros(size(sigmas))
    
    % save timings
    t_mad = 0
    t_liu = 0
    
    for i=0..numel(sigmas)-1
        % add noise 
        img_noise = img + sigmas[i]*randn(size(img))
        % estimate noise with MAD
        tic()
        sigmas_mad[i] = estimate_noise_mad(img_noise)
        t_mad += toc("")
        % estimate noise with method of Liu et. al.
        tic()
        sigmas_liu[i] = estimate_noise(img_noise)
        t_liu += toc("")
    end
    
    % visualization
    printf("Noise estimation (MAD) required %f ms on average", t_mad/numel(sigmas))
    printf("Noise estimation (Lui et. al.) required %f ms on average", t_liu/numel(sigmas))
    plot(sigmas,sigmas)
    hold("on")
    plot(sigmas,sigmas_mad)
    hold("on")
    plot(sigmas,sigmas_liu)
    legend("sigma","sigma MAD","sigma Liu et. al.")
end

% Function: estimate_noise
% 
% Estimates the noise level based on the method proposed by Liu et. al. The method analyzes weakly textured patches in order to estimate the noise variance
% 
% Usage:
%   : function [sigma : scalar] = estimate_noise(img : mat)
% 
% Parameters:
% img - input image
% 
% Returns:
% sigma - estimated noise level
function sigma:scalar = estimate_noise(img:mat)
    sigma = estimate_noise_liu(img)
end

% Function: estimate_noise_mad
% 
% Estimates the noise level by computing the median absolute deviation (MAD)
% 
% Usage:
%   : function [sigma : scalar] = estimate_noise_mad(img : mat, r : int = 2)
% 
% Parameters:
% img - input image
% r - window size of the median filter
% 
% Returns:
% sigma - estimated noise standard deviation
function sigma:scalar = estimate_noise_mad(img:mat,r:int=2)
    img_med:mat = immedfilt(img,r)
    sigma = median(transpose(reshape(abs(img-img_med),[size(img,0)*size(img,1)])))[0] / 0.6745
end

% Function: estimate_noise_liu
% 
% Estimates the noise level based on the method proposed by Liu et. al. The method analyzes weakly textured patches in order to estimate the noise variance
%       X. Liu, M. Tanaka and M. Okutomi, 
%       "Noise level estimation using weak textured patches of a single noisy image," 
%       2012 19th IEEE International Conference on Image Processing, Orlando, FL, 2012, pp. 665-668
% 
% Usage:
%   : function [sigma : scalar] = estimate_noise_liu(img : mat, patch_size : int = 7, itr : int = 2, conf : scalar = 0.999999)
% 
% Parameters:
% img - input image
% patch_size - patch size
% itr - number of iterations
% conf - confidence value for gamma distribution
% 
% Returns:
% sigma - estimated noise standard deviation
function sigma:scalar = estimate_noise_liu(img:mat,patch_size:int=7,itr:int=2,conf:scalar=0.999999)
    sigma = 0
    [m,n] = size(img,0..1)
    
    kh = [-0.5,0,0.5]
    kv = transpose(kh)
    imgh = imfilter(img,kh)[:,1..n-2].^2
    imgv = imfilter(img,kv)[1..m-2,:].^2
    
%    Dh = convmtx2(kh,patch_size,patch_size)
%    Dv = convmtx2(kv,patch_size,patch_size)
%    DD = transpose(Dh)*Dh + transpose(Dv)*Dv
%    r = rank(DD)
%    tr = trace(DD)
%    
%    tau0 = gaminv([conf],r/2,2.0*tr/r)[0] % TODO: numerically instable near 0 and 1, should be fixed (using a fixed tau0 value for now)
    tau0 = 81.8
    
    X = im2col(img,[patch_size,patch_size])
    Xh = im2col(imgh,[patch_size,patch_size-2])
    Xv = im2col(imgv,[patch_size-2,patch_size])
    
    Xtr = sum(vertcat(Xh,Xv),0)
    
    % noise level estimation
    tau = 1/0 % initialize to infinity
    if (size(X,1)<size(X,0))
        sig2 = 0
    else    
        cov = X*transpose(X)/(size(X,1))
        [v,d] = symeig(cov)
        sig2 = min(d)
    endif
    
    for i=1..itr-1
        % weak texture selection
        tau = sig2*tau0
        p = find(Xtr<tau)[:,1]
        Xtr = Xtr[:,p]
        X = X[:,p]
        
        % noise level estimation
        if (size(X,1)<size(X,0))
            break
        endif
        cov = X*transpose(X)/(size(X,1)-1)
        [v,d] = symeig(cov)
        sig2 = min(d)
    end
    
    sigma = sqrt(sig2)
end

% Function: im2col
% 
% Rearranges image blocks into columns using a (column-priority) sliding window approach
% 
% Usage:
%   : function [B : mat] = im2col(A : mat, s : ivec2'const)
% 
% Parameters:
% A - input image
% s - window size
% 
% Returns:
% B - output blocks
function B:mat = im2col(A:mat,s:ivec2)
    [m,n] = s
    [mm,nn] = size(A,0..1)
    function [] = __kernel__ im2col_kernel(B:mat,A:mat,pos:ivec4)
        B[pos[2]*n+pos[3],pos[1]*(mm-m+1)+pos[0]] = A[pos[0]+pos[3],pos[1]+pos[2]]
    end
    B = zeros(m*n,(mm-m+1)*(nn-n+1))
    parallel_do([mm-m+1,nn-n+1,m,n],B,A,im2col_kernel)
end

% Function: convmtx2
% 
% Helper function that derives Toeplitz matrices of the gradient operator
% 
% Usage:
%   : function [T : mat] = convmtx2(H : mat, m : int, n : int)
% 
% Parameters:
% H - gradient operator
% m - horizontal patch size
% n - vertical patch size
% 
% Returns:
% T - Toeplitz matrix
function T:mat = convmtx2(H:mat,m:int,n:int)
    [hm,hn] = size(H,0..1)
    function [] = __kernel__ convmtx2_kernel(T:mat,H:mat,pos:ivec3)
        T[pos[0]*(n-hn+1)+pos[1],(pos[0]+pos[2])*n+pos[1]..(pos[0]+pos[2])*n+pos[1]+hn-1] = H[pos[2],:]
    end
    T = zeros((m-hm+1)*(n-hn+1),m*n)
    parallel_do([m-hm+1,n-hn+1,hm],T,H,convmtx2_kernel)
end

% Function: gamma_integration
% 
% Computes an approximation of the gamma integral using the composite trapezoidal rule
%       \int\limits_{0}^{x} t^{a-1} exp{-t/b} dt
% 
% Usage:
%   : function [int_value : vec] = gamma_integration(x : vec, a : scalar, b : scalar, n : int = 100000)
% 
% Parameters:
% x - vector of input values
% a - gamma shape parameter
% b - gamma scale parameter
% n - number of composite trapezoids
% 
% Returns:
% int_value - vector of approximated integral values
function int_value:vec = gamma_integration(x:vec,a:scalar,b:scalar,n:int=100000)
    ts = transpose(1..n-1)*(x./n)
    vs = ts.^(a-1).*exp(-ts./b)
    int_value = (x./n) .* (sum(vs,0) + (x.^(a-1).*exp(-x./b)./2))
end

% Function: gamcdf
% 
% Computes the gamma cumulative distribution function
% 
% Usage:
%   : function [res : vec] = gamcdf(x : vec, a : scalar, b : scalar)
% 
% Parameters:
% x - vector input values
% a - gamma shape parameter
% b - gamma scale parameter
% 
% Returns:
% p - vector with gamma cumulative distribution values
function p:vec = gamcdf(x:vec,a:scalar,b:scalar)
    p = gamma_integration(x,a,b)./(b^a * gamma([a])[0])
end

% Function: gamma_inv
% 
% Computes the inverse of the gamma cummulative distribution function, this is inversion is currently computed using the Newton-Raphson method
% 
% Usage:
%   : function [x : vec] = gamma_inv(p : vec'const, a : scalar, b : scalar, iter : int = 50)
% 
% Parameters:
% p - vector with probabilities
% a - gamma shape parameter
% b - gamma scale parameter
% iter - number of Newton-Raphson iterations
% 
% Returns:
% x - vector with inverse values such that p = gamcdf(x,a,b)
function x = gaminv(p:vec,a:scalar,b:scalar,iter:int=50)
    x = ones(size(p))
    for i=0..iter-1
        dx = p-gamcdf(x,a,b)
        dx = dx./(x.^(a-1).*exp(-x./b)./(b^a)./gamma([a])[0])
        x += dx
    end
end


% Function: gamma
% 
% computes the gamma function for each element of xdf
% implementation references: "An Overview of Software Development for Special
%                             Functions", W. J. Cody, Lecture Notes in Mathematics,
%                             506, Numerical Analysis Dundee, 1975, G. A. Watson
%                             (ed.), Springer Verlag, Berlin, 1976.
%
%                             Computer Approximations, Hart, Et. Al., Wiley and
%                             sons, New York, 1968.
% 
% Usage:
%   : function [res : vec] = gamma(x : vec)
% 
% Parameters:
% x - input vector
% 
% Returns:
% res - gamma(x)
function res:vec = gamma(x:vec)
    % constants
    p = [-1.71618513886549492533811e0, 2.47656508055759199108314e1,_
         -3.79804256470945635097577e2, 6.29331155312818442661052e2,_
         8.66966202790413211295064e2, -3.14512729688483675254357e4,_
         -3.61444134186911729807069e4, 6.64561438202405440627855e4]
    q = [-3.08402300119738975254353e1, 3.15350626979604161529144e2,_
         -1.01515636749021914166146e3, -3.10777167157231109440444e3,_
          2.25381184209801510330112e4, 4.75584627752788110767815e3,_
         -1.34659959864969306392456e5, -1.15132259675553483497211e5]
    c = [-1.910444077728e-03, 8.4171387781295e-04,_
         -5.952379913043012e-04, 7.93650793500350248e-04,_
         -2.777777777777681622553e-03, 8.333333333333333331554247e-02,_
          5.7083835261e-03]
    
    res = zeros(size(x))
    xn = zeros(size(x))
    
    % catch negative x
    kneg = find(x<=0)
    if !isempty(kneg)
        kneg1 = kneg[:,1]
        y = -x[kneg1]
        y1 = zeros(size(y))
        y1[y1>0] = floor(y1[y1>0])
        y1[y1<0] = ceil(y1[y1<0])
        y1 = int(y1)
        res[kneg1] = y - y1
        mody1 = mod(y1,2)
        fact = -pi ./ sin(pi*res[kneg1]) .* (1-2*abs(mody1))
        x[kneg1] = y+1
    endif
    
    % x is now positive
    % map x in interval [0,1] to [1,2]
    k = find(x<1)
    if !isempty(k)
        k1 = k[:,1]
        x1 = x[k1]
        x[k1] = x1+1
    else
        x1 = []
    endif
    
    % map x in interval [1,12] to [1,2]
    k = find(x<12)
    if !isempty(k)
        k1 = k[:,1]
        xn_temp = x[k1]
        xn_temp_pos = find(xn_temp>0)
        if !isempty(xn_temp_pos)
            xn_temp_pos = xn_temp_pos[:,1]
            xn_temp[xn_temp_pos] = floor(xn_temp[xn_temp_pos])
        endif
        xn_temp_neg = find(xn_temp<0)
        if !isempty(xn_temp_neg)
            xn_temp_neg = xn_temp_neg[:,1]
            xn_temp[xn_temp_neg] = ceil(xn_temp[xn_temp_neg])
        endif
        xn[k1] = xn_temp - 1
        x[k1] = x[k1] - xn[k1]
    endif
    
    % evaluate approximation for 1.0<x<2.0
    if !isempty(k)
        k = k[:,1]
        z = x[k1] - 1
        xnum = 0*z
        xden = xnum + 1
        for i = 0..7
            xnum = (xnum + p[i]) .* z
            xden = xden .* z + q[i]
        end
        res[k1] = xnum ./ xden + 1
    endif
    
    % adjust result for case  0.0<x<1.0
    if !isempty(k)
        k = k[:,1]
        res[k1] = res[k1] ./ x1
    endif
    
    % adjust result for case  2.0<x<12.0
    for j = 1..max(xn)
        k = find(xn)
        if !isempty(k)
            k1 = k[:,1]
            res[k1] = res[k1] .* x[k1]
            x[k1] = x[k1] + 1
            xn[k1] = xn[k1] - 1
        endif
    end
    
    % evaluate approximation for x>=12
    k = find(x >= 12)
    if !isempty(k)
        k1 = k[:,1]
        y = x[k1]
        ysq = y .* y
        s = c[6]
        for i = 0..5
            s = s ./ ysq + c[i]
        end
        spi = 0.9189385332046727417803297
        s = s ./ y - y + spi
        s = s + (y-0.5).*log(y)
        res[k1] = exp(s)
    endif
    
    % final adjustments
    if !isempty(kneg)
        res[kneg] = fact ./ res[kneg]
    endif
end
