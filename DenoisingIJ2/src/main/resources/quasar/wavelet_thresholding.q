
import "dtcwt.q"
import "system.q"

function w_thr = hard_thresh(w,T)
    w_thr = w.*(abs(w)>T)
end

function w_thr = soft_thresh(w,T)
    w_thr = sign(w).*max((abs(w)-T),0)
end

function img_den = wav_denoise(img,sigma,J,w1,w2,thr_type,alpha)
    w = dtcwt2d(img, w1, w2, J)
    for j=0..J-1
        for r=0..1
            for c=0..1
                for i=0..2
                    s = std(w[j][r,c][i])
                    T = alpha*sqrt(2)*sigma^2/s
                    if thr_type=="hard"
                        % hard thresholding
                        w[j][r,c][i] = hard_thresh(w[j][r,c][i],T)
                    else
                        % soft thresholding
                        w[j][r,c][i] = soft_thresh(w[j][r,c][i],T)
                    endif
                end
            end
        end
    end
    img_den = idtcwt2d(w, w1, w2, J)
end

function [] = main()

    img = imread("lena_big.tif")[:,:,0]

    sigma = 20
    img_noisy = img + sigma .* randn(size(img))
    
    J = 6   % number of scales
    w1 = filtercoeff_farras     % wavelet for the first scale
    w2 = filtercoeff_selcw[3,1] % wavelet for the other scales
    alpha = 1.5 % denoising param
    thresholding_type = "soft" % soft or hard
    
    tic()
    img_den = wav_denoise(img_noisy,sigma,J,w1,w2,thresholding_type,alpha)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig0=imshow(img_noisy)
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den)
    title(sprintf("Wavelet thresholding - psnr=%f dB", psnr(img_den,img)))

    fig0.connect(fig1)

end