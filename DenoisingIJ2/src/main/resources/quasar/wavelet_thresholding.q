
import "dtcwt.q"
import "system.q"

function w_thr = hard_thresh(w,T)
    w_thr = w.*(abs(w)>T)
end

function w_thr = soft_thresh(w,T)
    w_thr = sign(w).*max((abs(w)-T),0)
end

function img_den = wav_denoise(img:mat, sigma:scalar, J:int, w1brol:mat, w2brol:mat, thr_type:string, alpha:scalar)
	printf("From Quasar: wav_denoise: size of img=%d\n", size(img))
    printf("From Quasar: wav_denoise: sigma=%f J=%d w1=%f w2=%f thr_type=%s alpha=%f\n", sigma, J, w1brol, w2brol, thr_type, alpha)
    
    
    w1 = filtercoeff_farras     % wavelet for the first scale
    w2 = filtercoeff_selcw[3,1] % wavelet for the other scales
    
    
    print(type(img))
    print(type(sigma))
    print(type(J))
    print(type(w1brol))
    print(type(w2brol))
    print(type(thr_type))
    print(type(alpha))

    print(type(w1))
    print(type(w2))
    
    w = dtcwt2d(img, w1, w2, J)

    
	printf("From Quasar: wav_denoise: size of img=%d\n", size(img))

    for j=0..J-1
        for r=0..1
            for c=0..1
                for i=0..2
                	printf("%d %d %d %d\n", j, r, c, i)
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
    printf("From quasar: wav_denoise: thresholded, about to inverse trf")
    img_den = idtcwt2d(w, w1, w2, J)
    printf("From quasar: wav_denoise: thresholded, inverse trfed")
    print(size(img_den))
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