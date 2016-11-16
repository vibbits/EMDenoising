
import "imfilter.q"

function [] = main()

    img = imread("lena_big.tif")[:,:,0]

    sigma = 20
    img_noisy = img + sigma .* randn(size(img))
    
    sigma_filter = 1.5
    
    tic()
    img_den = gaussian_filter(img_noisy, sigma_filter, 0, "mirror")
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig0=imshow(img_noisy)
    title(sprintf("Input image - psnr=%f dB", psnr(img_noisy,img)))
    fig1=imshow(img_den)
    title(sprintf("Gaussian filter - psnr=%f dB", psnr(img_den,img)))

    fig0.connect(fig1)

end