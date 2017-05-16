%==================================================
% Implementation of anisotropic diffusion denoising
%==================================================
{!author name="Joris Roels"}
{!doc category="Image Processing/Restoration/Wavelets"}

import "dtcwt.q"
import "system.q"
import "estimate_noise.q"
import "power_of_two_extension.q"

%
% The main function - for testing the code
%
function [] = main()

    % read data
    img = imread("lena_big.tif")[:,:,0]/255
    
    % add noise
    sigma = 0.05
    img_noisy = img + sigma .* randn(size(img))
    
    % estimate noise level
    sigma_est = estimate_noise_liu(img_noisy)
    
    % set parameters
    J = 3 
    w1 = filtercoeff_farras
    w2 = filtercoeff_selcw[3,1]
    thresholding_type = "soft"
    T = 6.83660888671875*sigma_est^2 + 2.34318542480469*sigma_est - 0.00547122955322266
    
    % denoising
    tic()
    img_den = wav_denoise(img_noisy,J,w1,w2,thresholding_type,T)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(1/mean((x-y).^2))
    
    % visualization
    hold("off")
    fig0=imshow(img_noisy,[0,1])
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den,[0,1])
    title(sprintf("BLS-GSM - psnr=%f dB", psnr(img_den,img)))
    fig0.connect(fig1)

end

% Function: hard_thresh
%
% Performs hard tresholding on wavelet coefficients
%
% :function w_thr = __device__ hard_thresh(w,T)
%
% Parameters:
% w - input coefficients
% T - threshold
% 
% Returns:
% w_thr - thresholded coefficients
function w_thr = __device__ hard_thresh(w,T)
    w_thr = w.*(abs(w)>T)
end

% Function: soft_thresh
%
% Performs soft tresholding on wavelet coefficients
%
% :function w_thr = __device__ soft_thresh(w,T)
%
% Parameters:
% w - input coefficients
% T - threshold
% 
% Returns:
% w_thr - thresholded coefficients
function w_thr = __device__ soft_thresh(w,T)
    w_thr = sign(w).*max((abs(w)-T),0)
end

% Function: soft_thresh
%
% Performs wavelet thresholding on a noisy image
%
% :function img_den:mat = wav_denoise(img:mat,J:int,w1,w2,thr_type,T:scalar)
%
% Parameters:
% img - noisy image
% J - number of scales
% w1 - wavelet for the first scale
% w2 - wavelet for the other scales
% thr_type - type of thresholding: either "hard" or "soft"
% T - shrinkage threshold
% 
% Returns:
% w_thr - thresholded coefficients
function img_den:mat = wav_denoise(img:mat,J:int,w1,w2,thr_type,T:scalar)
	% print "wav_denoise J=", J, " w1=", w1, " w2=", w2, " T=", T

    % Do power-of-two extension of the image
    % (wavelet trf code assumes power-of-two image dimensions)
    orig_size = size(img)
    [img, topleft] = power_of_two_extension(img)
    	
    img_cube = repmat(img,[1,1,8])
    b = dtcwt3d_untiled(img_cube, w1, w2, J)

    % Size of the LLL band at scale J
    sz_LLL = int(size(img_cube,0..2)/2^J)

    % Soft-thresholding on the real and imaginary components of
    % the dual-tree complex wavelet coefficients separately
    match thr_type with 
    | "hard" ->            
        for k=0..3
            band = b[k]
            {!parallel for}
            for m=0..size(b[k],0)-1
                for n=0..size(b[k],1)-1
                    for l=0..size(b[k],2)-1
                        if m >= sz_LLL[0] || n >= sz_LLL[1] || l >= sz_LLL[2]
                            band[m,n,l] = hard_thresh(band[m,n,l], T)
                        endif
                    end
                end
            end
        end
    | "soft" ->
        for k=0..3
            band = b[k]
            {!parallel for}
            for m=0..size(b[k],0)-1
                for n=0..size(b[k],1)-1
                    for l=0..size(b[k],2)-1
                        if m >= sz_LLL[0] || n >= sz_LLL[1] || l >= sz_LLL[2]
                            band[m,n,l] = soft_thresh(band[m,n,l], T)
                        endif
                    end
                end
            end
        end    
    end    
    img_den = idtcwt3d_untiled(b, w1, w2, J)[:,:,0]

    % Remove power-of-two extension border again
    if (any(size(img_den) != orig_size))
        img_den = img_den[topleft[0]..topleft[0]+orig_size[0]-1, topleft[1]..topleft[1]+orig_size[1]-1]
    endif
end