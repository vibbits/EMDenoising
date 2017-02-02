
function img_den:mat = denoise_anisotropic_diffusion(img:mat,niter:int,dt:scalar,k:scalar,diff_function:int)
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
    
    % iteration
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
        if diff_function==0
            cN = exp(-(g_n/k).^2)
            cE = exp(-(g_e/k).^2)
            cS = exp(-(g_s/k).^2)
            cW = exp(-(g_w/k).^2)
            cNE = exp(-(g_ne/k).^2)
            cSE = exp(-(g_se/k).^2)
            cSW = exp(-(g_sw/k).^2)
            cNW = exp(-(g_nw/k).^2)
        else
            cN = 1./(1+(g_n/k).^2)
            cE = 1./(1+(g_e/k).^2)
            cS = 1./(1+(g_s/k).^2)
            cW = 1./(1+(g_w/k).^2)
            cNE = 1./(1+(g_ne/k).^2)
            cSE = 1./(1+(g_se/k).^2)
            cSW = 1./(1+(g_sw/k).^2)
            cNW = 1./(1+(g_nw/k).^2)
        endif
        
        % diffusion update
        img_den += dt*( (1/(dy^2))*cN.*g_n + (1/(dy^2))*cS.*g_s + _
                        (1/(dx^2))*cW.*g_w + (1/(dx^2))*cE.*g_e + _
                        (1/(dd^2))*cNE.*g_ne + (1/(dd^2))*cSE.*g_se + _
                        (1/(dd^2))*cSW.*g_sw + (1/(dd^2))*cNW.*g_nw )
    end
end

function [] = main()
    
    sigma = 20
    
    niter = 5
    dt = 0.2
    k = 40
    diff_function = 0 % 0 for exponential, 1 for quadratic
    
    img = imread("lena_big.tif")
    img = img[:,:,0]
    
    img_noisy = img + randn(size(img))*sigma
    
    print "Computing anisotropic diffusion ..."
    tic()
    img_den = denoise_anisotropic_diffusion(img_noisy,niter,dt,k,diff_function)
    toc()
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig1=imshow(img_noisy,[0,255])
    title(sprintf("Noisy image - psnr=%f dB", psnr(img_noisy,img)))
    fig2=imshow(img_den,[0,255])
    title(sprintf("Anisotropic diffusion - psnr=%f dB", psnr(img_den,img)))
    
    fig1.connect(fig2)

end