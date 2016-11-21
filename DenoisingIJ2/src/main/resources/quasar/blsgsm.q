import "system.q"
import "multirestransforms.q"
import "linalg.q"
import "power_of_two_extension.q"

{!author name="Bart Goossens"}
{!doc category="Image Processing/Restoration/Wavelets"}

% Function: compute_covmtx
%
% Parallel computation of a spatial covariance matrix 
% without assuming spatial stationarity
%
% :function C = compute_covmtx(x, wnd)
%
% Parameters:
% x - input image / wavelet subband
% wnd - size of the local window
%
% Returns:
% C - output covariance matrix, dimensions: prod(wnd) x prod(wnd)
%
% See also:
% <compute_autocor>, <autocor_to_covmtx>, <compute_covmtx_spat_stationary>
function C = compute_covmtx(x, wnd)
    p_all = __device__ (x : ivec2) -> x[0] && x[1]
    function [] = __kernel__ compute_covmtx_kernel(x : mat, C : mat'unchecked, wnd : ivec2, blkpos : ivec2, blkdim : ivec2)

        d = wnd[0]*wnd[1]  % dimension of the local window
        bins = shared_zeros(blkdim)  % Note - we assume that blkdim is a power of two!
        
        nblocks = int(ceil(float(size(x))./blkdim))
        max_bit = max(blkdim[0],blkdim[1])
        
        for dm=0..d-1
            for dn=0..dm
                % step 1 - parallel sum - note - we only consider the upper diagonal here
                v1 = [int(dm/wnd[1]),mod(dm,wnd[1])]
                v2 = [int(dn/wnd[1]),mod(dn,wnd[1])] 
                val = 0.0
                
                for m=0..nblocks[0]-1
                    for n=0..nblocks[1]-1
                        p = blkpos + [m,n] .* blkdim
                        val += x[p+v1] * x[p+v2]
                    end
                end
                bins[blkpos] = val
         
                % step 2 - reduction
                syncthreads

                bit = 1
                while (bit < max_bit)
                    if (mod(blkpos[0],bit*2) + mod(blkpos[1],bit*2) == 0)
                        bins[blkpos] += (p_all(blkpos + [0,bit] < blkdim) ? bins[blkpos + [0,bit]] : 0.0) + 
                                        (p_all(blkpos + [bit,0] < blkdim) ? bins[blkpos + [bit,0]] : 0.0) + 
                                        (p_all(blkpos + [bit,bit] < blkdim) ? bins[blkpos + [bit,bit]] : 0.0)
                    endif
                    syncthreads
                    bit *= 2
                end

                % step 3 - write covariance matrix to the output
                if sum(blkpos) == 0
                    val = bins[0,0]
                    C[dm,dn] = val
                    C[dn,dm] = val % symmetry
                endif
            end
        end
    end

    C = zeros(wnd.^2)
    parallel_do(max_block_size(compute_covmtx_kernel, [16,16]),x,C,wnd,compute_covmtx_kernel)
    %parallel_do([1,1],x,C,wnd,compute_covmtx_kernel)
    C = C / numel(x)

end

% Function: compute_autocor
%
% Parallel computation of a spatial autocorrelation function
% i.e. assuming spatial stationarity
%
% :function R = compute_autocor(x, wnd)
%
% Parameters:
% x - input image / wavelet subband
% wnd - size of the local window
%
% Returns:
% An autocorrelation function of size (wnd[0]*2-1) x wnd[1]
%
% See also:
% <compute_covmtx>, <autocor_to_covmtx>
function R = compute_autocor(x, wnd)
    p_all = __device__ (x : ivec2) -> x[0] && x[1]
    function [] = __kernel__ compute_autocor_kernel(x : mat, R : mat'unchecked, wnd : ivec2, blkpos : ivec2, blkdim : ivec2)

        d = wnd[0]*wnd[1]  % dimension of the local window
        assert(d<=15)
        bins = shared_zeros(d,blkdim[0],blkdim[1])  % Note - we assume that blkdim is a power of two!

        nblocks = int((size(x)+blkdim-[1,1])./blkdim)
        max_bit = max(blkdim[0],blkdim[1])

        syncthreads

        % step 1 - parallel sum
        for m=0..nblocks[0]-2
            for n=0..nblocks[1]-2
                p = blkpos + [m,n] .* blkdim
                xp = x[p]
                p = p - [int(wnd[0]/2), 0]
                for dm=0..d-1
                    v = [int(dm/wnd[1]),mod(dm,wnd[1])]
                    bins[dm,blkpos[0],blkpos[1]] += xp * x[p+v]
                end
            end
        end

        m = nblocks[0]-1  % bottom row of blocks
        for n=0..nblocks[1]-1
            p = blkpos + [m,n] .* blkdim
            if p[0] < size(x,0) && p[1] < size(x,1)
                xp = x[p]
                p = p - [int(wnd[0]/2), 0]
                for dm=0..d-1
                    v = [int(dm/wnd[1]),mod(dm,wnd[1])]
                    bins[dm,blkpos[0],blkpos[1]] += xp * x[mod(int(p+v),size(x))]
                end
            endif
        end

        n = nblocks[1]-1 % right row of blocks
        for m=0..nblocks[0]-2
            p = blkpos + [m,n] .* blkdim
            if p[0] < size(x,0) && p[1] < size(x,1)
                xp = x[p]
                p = p - [int(wnd[0]/2), 0]
                for dm=0..d-1
                    v = [int(dm/wnd[1]),mod(dm,wnd[1])]       
                    bins[dm,blkpos[0],blkpos[1]] += xp * x[mod(int(p+v),size(x))]
                end
            endif
        end
     
        % step 2 - reduction
        syncthreads

        bit = 1
        while bit < max_bit
            if mod(blkpos[0],bit*2) + mod(blkpos[1],bit*2) == 0
                for dm=0..d-1
                    q = [dm,blkpos[0],blkpos[1]]
                    bins[q] += (p_all(blkpos + [0,bit] < blkdim) ?  bins[q + [0,0,bit]] : 0.0) + _
                               (p_all(blkpos + [bit,0] < blkdim) ?  bins[q + [0,bit,0]] : 0.0) + _
                               (p_all(blkpos + [bit,bit] < blkdim) ?  bins[q + [0,bit,bit]] : 0.0)
                end
            endif
            syncthreads
            bit *= 2
        end

        % step 3 - write covariance matrix to the output
        if sum(blkpos) == 0
            for dm=0..d-1
                val = bins[dm,0,0]
                R[int(dm/wnd[1]),mod(dm,wnd[1])] = val            
            end
        endif
    end

    R = zeros(wnd[0]*2-1,wnd[1])
    parallel_do(max_block_size(compute_autocor_kernel, [16,32]),x,R,[wnd[0]*2-1,wnd[1]],compute_autocor_kernel)
    assert(sum(R) != 0)
    R = R / numel(x)

end

% Function: autocor_to_covmtx
%
% Conversion from a spatial autocorrelation function to spatial covariance matrix
% with a local window of the specified size
%
% :function C = autocor_to_covmtx(R, wnd = [3,3])
%
% Parameters:
% R - input spatial autocorrelation function
% wnd - the size of the spatial window (default: [3,3])
%
% Returns:
% C - output covariance matrix, dimensions: prod(wnd) x prod(wnd)
% 
% See also:
% <compute_autocor>, <compute_covmtx>
function C = autocor_to_covmtx(R, wnd = [3,3])

    C = zeros(wnd.^2) 
    
    function [] = __kernel__  autocor_to_covmtx_kernel(R : mat, C : mat, wnd : ivec2, pos : ivec2)
        m = pos[0]
        n = pos[1]
        if n <= m
            c = [wnd[0]-1, 0] % center
            v = [int(m/wnd[1]),mod(m,wnd[1])] - [int(n/wnd[1]),mod(n,wnd[1])]
            if v[1]<0
                v = -v
            endif
            val = R[c + v]
            C[m,n] = val
            C[n,m] = val % symmetry
        endif
    end
    parallel_do(size(C), R, C, wnd, autocor_to_covmtx_kernel)
end

% Function: symsqrt_and_inv
%
% Computation of the symmetric square root of a matrix and its inverse
%
% :function [S, S_inv] = symsqrt_and_inv(C)
%
function [S, S_inv] = symsqrt_and_inv(C)
    [U, Lambda, V] = svd(C)
    %print "svd err=",sum(sum(abs(U*Lambda*transpose(V)-C)))
    Lambda = diag(Lambda)
    S = U*diag(sqrt(Lambda))*transpose(V)
    S_inv = U*diag(1./sqrt(Lambda))*transpose(V)
end

% Function: compute_covmtx_spat_stationary
%
% Computation of the covariance matrix under the assumption of spatial stationarity
%
% :function C = compute_covmtx_spat_stationary(x, wnd)
%
% Parameters:
% x - input image / wavelet subband
% wnd - size of the local window
%
% Returns:
% C - output covariance matrix, dimensions: prod(wnd) x prod(wnd)
%
% See also:
% <compute_covmtx>
function C = compute_covmtx_spat_stationary(x, wnd)
    R = compute_autocor(x, wnd)
    C = autocor_to_covmtx(R, wnd)
end

% Function: correct_eigenvalues
%
% Make: sure the eigenvalues of the symmetric matrix are positive. All
% negative eigenvalues are replaced by a small positive constant
%
% :function C = correct_eigenvalues(X : mat, epsilon = 1e-4)
%
% Parameters:
% X - a symmetric matrix
% epsilon - a small positive constant
% 
% Returns:
% C - a positive definite matrix
function C = correct_eigenvalues(X : mat, epsilon = 1e-4)
    [U, Lambda, V] = svd(X)
    n = size(X,0)
    C = U*diag(max(epsilon*ones(n), diag(Lambda)))*transpose(V)
end

% Function: jeffreys_prior
%
% Calculates the probability density function for the hidden multiplier of
% Jeffrey's non-informative prior for a Gaussian Scale Mixture
%
% :function [z, p_z] = jeffreys_prior(K : int)
%
% Parameters:
% K - the number of samples for the resulting pdf
%
% Returns:
% z - a discrete vector of values for the hidden multiplier
% p_z - the vector with the probabilities for each component of z
function [z, p_z] = jeffreys_prior(K : int)
    z = exp(linspace(-21.5,-3.5,K))
    p_z = ones(K)/K
    z = z / sum(z.*p_z)
end

% Function: bkf_prior
%
% Calculates the probability density function for the hidden multiplier of
% Bessel K Form prior for a Gaussian Scale Mixture
%
% :function [z, p_z] = bkf_prior(K,tau)
%
% Parameters:
% K - the number of samples for the resulting pdf
% tau - is related to the fourth central moment of the distribution and controls
%    the steepness of the distribution. tau=1 corresponds to a Laplacian 
%    distribution, tau->infinity corresponds to a Gaussian distribution. tau is
%    always positive.
%
% Returns:
% z - a discrete vector of values for the hidden multiplier
% p_z - the vector with the probabilities for each component of z
function [z, p_z] = bkf_prior(K,tau=1)
    r = linspace(-4,7,K+1)
    z = 0.5*(exp(r[0..K-1]) + exp(r[1..K]))
    p_z = z .^ (tau-1)
    p_z[0] = p_z[0] * 0.5
    p_z[K-1] = p_z[K-1] * 0.5
    p_z = p_z / sum(p_z) % normalize
    z = z / sum(z.*p_z)
end

% Function: denoise_band_blsgsm
%
% Denoising of a wavelet subband using the BLS-GSM method
%
% :function [] = denoise_band_blsgsm(x, wnd, C_n, sigma_n, prior_dist = "Jeffrey (fast)")
%
% Parameters:
% x - subband to denoise
% wnd - the size of the local window
% C_n - noise covariance matrix
% sigma_n - the noise standard deviation
% prior_dist - the prior distribution to be used
%     "Jeffrey": Jeffrey's non-informative prior
%     "Jeffrey (fast)": Jeffrey's non-informative prior with lower precision
%     "BKF": Bessel K Form prior
%
function [] = denoise_band_blsgsm(x, wnd, C_n, sigma_n, prior_dist = "Jeffrey (fast)")

    % The central coefficient in the window
    d = prod(wnd)
    c = floor(d/2)

    % Prior distribution for z & the number of mixture components
    match prior_dist with
    | "Jeffrey" ->
        K = 12
        [z, p_z] = jeffreys_prior(K)
    | "Jeffrey (fast)" ->
        K = 4
        [z, p_z] = jeffreys_prior(K)
    | "BKF" ->
        K = 4
        [z, p_z] = bkf_prior(K, 0.7) % TODO - estimate tau from the image
    | _ ->
        error("Unknown prior distribution specified!")
    end

    if isscalar(sigma_n)
        C_n = C_n * sigma_n.^2
    endif
    
    C_y = compute_covmtx_spat_stationary(x, wnd)
    C_x = correct_eigenvalues(C_y - C_n, 1e-2)

    % Symmetric matrix square root of the noise covariance
    [S, S_inv] = symsqrt_and_inv(C_n)

    % Compute S^{-1}C_{x}S^{-T} -> SVD Q Lambda Q^T
    [Q, LambdaE, Q2] = svd(S_inv*C_x*transpose(S_inv))
    Lambda = diag(LambdaE)

    % Compute U=(SQ)^{-1}; this transform will decorrelate the observed noisy samples
    % The matrix inverse can be replaced by a multiplication
    U = transpose(Q) * S_inv

    % Compute U^(-1)=SQ
    U_inv = S*Q

    % Correct eigenvalues if necessary
    epsilon = 1e-2
    Lambda = max(epsilon*ones(d), Lambda)

    % Only one column of U_inv is needed (the one corresponding to the central coefficient)
    tf_U_inv = U_inv[c,:]

    % Logarithm of the determinant of C_z=z C_x + C_n
    v_ld = zeros(K)
    for k=0..K-1
        v_ld[k] = 4*d + logdet(z[k] * C_x + C_n)
        if isnan(v_ld[k])
            v_ld[k] = 0
        endif
    end

    % Estimator for stationary noise
    function [] = __kernel__ blsgsm_estimator(x : mat'circular, y : mat'unchecked, U : mat'unchecked, 
        tf_U_inv : vec'unchecked, v_ld : vec'unchecked, Lambda : vec'unchecked, _
        z : vec'unchecked, p_z : vec'unchecked, wnd : ivec2, pos : ivec2, blkdim : ivec2, blkpos : ivec2)
        d = wnd[0]*wnd[1]
        c = pos - int(wnd/2)
        K = numel(v_ld)
        assert(d<=9 && K<=4)
        v = shared(d,blkdim[0],blkdim[1])
        p_yz = shared(K,blkdim[0],blkdim[1])

        % decorrelate the observed coefficient vector
        y_est = 0.0
        for m=0..d-1
            sum = 0.0
            for n=0..d-1
                p = [int(n/wnd[1]),mod(n,wnd[1])] + c
                sum += U[m,n] * x[p]
            end
            v[m,blkpos[0],blkpos[1]] = sum
        end

        % compute the likelihood p(y|z_k)
        p_y = 0.0
        for k=0..K-1
            sum = v_ld[k]
            for i=0..d-1
                val = v[i,blkpos[0],blkpos[1]]
                sum += val*val/(z[k]*Lambda[i]+1)
            end
            val = p_z[k]*exp(-0.5*sum)
            p_yz[k,blkpos[0],blkpos[1]] = val
            p_y += val
        end

        % estimate the noise-free signal
        y_est = 0.0
        if p_y > 0
            for k=0..K-1
                val = 0.0
                for i=0..d-1
                    r = z[k] * Lambda[i]
                    val += tf_U_inv[i] * r/(r+1)*v[i,blkpos[0],blkpos[1]]
                end
                y_est += val * p_yz[k,blkpos[0],blkpos[1]]/p_y
            end
        endif
        y[pos] = y_est
    end

    % Estimator for non-stationary noise:
    % The local covariance matrix at position (m,n) is defined by:
    % C_n'(m,n) = C_n * V_n(m,n)
    %
    function [] = __kernel__ blsgsm_estimator_nonstat(x : mat'circular, y : mat'unchecked, U : mat'unchecked, 
        V_n : mat'unchecked, tf_U_inv : vec'unchecked, v_ld : vec'unchecked, Lambda : vec'unchecked, _
        z : vec'unchecked, p_z : vec'unchecked, wnd : ivec2, vscale : ivec2, pos : ivec2, blkdim : ivec2, blkpos : ivec2)
        d = wnd[0]*wnd[1]
        c = pos - int(wnd/2)
        K = numel(v_ld)
        assert(d<=9 && K<=4)
        v = shared(d,16,16)
        p_yz = shared(K,16,16)
        v_n = max(V_n[pos.*vscale]^2,1e-4)

        % decorrelate the observed coefficient vector
        y_est = 0.0
        for m=0..d-1
            sum = 0.0
            for n=0..d-1
                p = [int(n/wnd[1]),mod(n,wnd[1])] + c
                sum += U[m,n] * x[p]
            end
            v[m,blkpos[0],blkpos[1]] = sum
        end

        % compute the likelihood p(y|z_k)
        p_y = 0.0
        for k=0..K-1
            sum = v_ld[k]
            for i=0..d-1
                val = v[i,blkpos[0],blkpos[1]]
                sum += val*val/(z[k]*Lambda[i]+v_n)
            end
            val = p_z[k]*exp(-0.5*max(-8.0,sum))
            p_yz[k,blkpos[0],blkpos[1]] = val
            p_y += val
        end

        % estimate the noise-free signal
        y_est = 0.0
        if p_y > 0
            for k=0..K-1
                val = 0.0
                for i=0..d-1
                    r = z[k] * Lambda[i]
                    val += tf_U_inv[i] * r/(r+v_n)*v[i,blkpos[0],blkpos[1]]
                end
                y_est += val * p_yz[k,blkpos[0],blkpos[1]]/p_y
            end
        endif
        y[pos] = y_est
    end
    y = copy(x)
    
    if isscalar(sigma_n)
        parallel_do([size(x,0..2),max_block_size([16,32,1])],y,x,U,tf_U_inv,v_ld,Lambda,z,p_z,wnd,blsgsm_estimator)
    else
        vscale = size(sigma_n,0..1) ./ size(x,0..1) % subsample in case the dimensions don't match
        parallel_do([size(x,0..2),max_block_size([16,16,1])],y,x,U,sigma_n,tf_U_inv,v_ld,Lambda,z,p_z,wnd,vscale,blsgsm_estimator_nonstat)    
    endif
end

% Function: denoise_image_blsgsm
%
% Denoises an image using the method of BLS-GSM. Both grayscale and RGB color images are
% supported.
%
% :function y = denoise_image_blsgsm(img_noisy, sigma, sparsity_tf="dtcwt", J=5, K=8)
%
% Parameters:
% img_noisy - noisy input image
% sigma - the noise standard deviation
% sparsity_tf - the transform used for achieving sparsity
%      "shearlet": the bandlimited discrete shearlet transform
%      "wavelet": the orthogonal wavelet transform
%      "dtcwt": the dual-tree complex wavelet transform
%      "steerable pyramid": steerable pyramids
% J - the number of analysis scales for the transform
% K - the number of analysis orientations for the transform (shearlet and steerable
%     pyramids only)
function y = denoise_image_blsgsm(img_noisy, sigma, sparsity_tf="dtcwt", J=5, K=8)
    % Do power-of-two extension of the image
    % (wavelet trf code assumes power-of-two image dimensions)
    orig_size = size(img_noisy)
    [img_noisy, topleft] = power_of_two_extension(img_noisy)

    % Denoise    
    sz = size(img_noisy)
    match sparsity_tf with
    | "shearlet" -> 
            [S, S_H] = build_energyweighted_dst2d(sz, K, J)
    | "wavelet"  -> 
            [S, S_H] = build_dwt(db8, J)
    | "dtcwt"    -> 
            [S, S_H] = build_dtcwt(filtercoeff_farras, filtercoeff_dualfilt, J)
    | "steerable pyramid"      -> 
            [S, S_H] = build_stp(sz, K, J)
    | _          -> 
            error("Sparsity transform unknown!")
    end    

    w = S(img_noisy)
    w2 = S(randn(size(img_noisy)))

    wnd = [3,3]
    % nc = size(img_noisy, 2)

    bands = lincell(w)
    bands2 = lincell(w2)

    for j=0..numel(bands)-5
        C_n = compute_covmtx_spat_stationary(bands2[j], wnd) 
        denoise_band_blsgsm(bands[j], wnd, C_n, sigma)
    end
    y = S_H(w)

    % Remove power-of-two extension border again
    if (any(size(y) != orig_size))
    	y = y[topleft[0]..topleft[0]+orig_size[0]-1, topleft[1]..topleft[1]+orig_size[1]-1]
    endif    
end

%
% The main function - for testing the code
%
function [] = main()

    img = imread("lena_big.tif")[:,:,0]

    sigma = 20
    img_noisy = img + sigma .* randn(size(img))
    
    J = 4 % number of scales
    K = 8 % number of orientations

%    tic()
%    [S, S_H] = build_dtcwt(filtercoeff_farras, filtercoeff_dualfilt, J)
%    
%    w = S(img_noisy)
%    w2 = S(randn(size(img_noisy)))
%
%    wnd = [3,3]
%
%    bands = lincell(w)
%    bands2 = lincell(w2)
%    
%    for j=0..numel(bands)-5
%        C_n = compute_covmtx_spat_stationary(bands2[j], wnd) 
%        denoise_band_blsgsm(bands[j], wnd, C_n, sigma)
%    end
%    img_den = S_H(w)
%    toc()
    
    tic()
    img_den=denoise_image_blsgsm(img_noisy, sigma, "dtcwt", J, K)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig0=imshow(img_noisy)
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den)
    title(sprintf("Deconvolution with non-local prior - psnr=%f dB", psnr(img_den,img)))

    fig0.connect(fig1)

end