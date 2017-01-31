% Bilateral filter
% Author: Simon Donné

import "colortransform.q"

% Function: compute_bilateral_filter
%
% Computes bilateral filter coefficients
%
% Parameters:
% Lab - image in LAB space
% n_width - width of the filter window
% n_height - height of the filter window
% alpha - divisor for the color distance
% beta - divisor for the spatial distance
% eucl_dist - use the euclidian distance (1), or the square of the distance (0)
% normalize - normalize the bilateral filter coefficients over each neighbourhood (1)
%
% :function bf:cube = compute_bilateral_filter(Lab:cube'safe, n_width:int, n_height:int, alpha:scalar, beta:scalar, eucl_dist:scalar, normalize:scalar)

function bf:cube = compute_bilateral_filter(Lab:cube'safe, n_width:int, n_height:int, _
                                                alpha:scalar, beta:scalar, eucl_dist:scalar, normalize:scalar)
    nx2:int = (n_width - 1)/2
    ny2:int = (n_height - 1)/2
    height:int = size(Lab,0)
    width:int  = size(Lab,1)
    
    bf = zeros(height, width, n_width * n_height)   % Beware of out-of-memory here - this may be a rather large cube.
    summ = zeros(height, width)
        
    function [] = __kernel__ fcalcbf(pos:ivec3)
        j = pos[0]
        i = pos[1]
        x = mod(pos[2], n_width)-nx2
        y = floor(pos[2]/n_width)-ny2
        if(j+y < 0 || j+y >= height || i+x < 0 || i+x >= width)
            bf[pos] = 0.0
        else
            diffL = Lab[j+y,i+x,0] - Lab[j, i,0]
            diffa = Lab[j+y,i+x,1] - Lab[j, i,1]
            diffb = Lab[j+y,i+x,2] - Lab[j, i,2]
            cdist = diffL^2 +diffa^2 +diffb^2
            dist  = x^2 + y^2
            bf[pos] = exp(-(cdist/alpha)-(dist/beta))
            summ[j,i] += bf[pos]
        endif
    end  
    function [] = __kernel__ fcalcbfeucl(pos:ivec3)
        j = pos[0]
        i = pos[1]
        x = mod(pos[2], n_width)-nx2
        y = floor(pos[2]/n_width)-ny2
        if(j+y < 0 || j+y >= height || i+x < 0 || i+x >= width)
            bf[pos] = 0.0
        else
            diffL = Lab[j+y,i+x,0] - Lab[j, i,0]
            diffa = Lab[j+y,i+x,1] - Lab[j, i,1]
            diffb = Lab[j+y,i+x,2] - Lab[j, i,2]
            cdist = sqrt(diffL^2 +diffa^2 +diffb^2)
            dist  = sqrt(x^2 + y^2)
            bf[pos] = exp(-(cdist/alpha)-(dist/beta))
            summ[j,i] += bf[pos]
        endif
    end
    
    if(eucl_dist==1)
        parallel_do(size(bf), fcalcbfeucl)
    else
        parallel_do(size(bf), fcalcbf)
    endif
    
    if(normalize)
        function [] = __kernel__ fnormbl(pos:ivec3)
            bf[pos] = bf[pos]/summ[pos[0], pos[1]]
        end
        parallel_do(size(bf), fnormbl)
    endif
end

% Function: apply_bilateral_filter
%
% Applies the bilateral filter to an image
%
% Parameters:
% bf - bilateral filter coefficients
% n_width - width of the filter window
% n_height - height of the filter window
%
% :function result:cube = apply_bilateral_filter(image:cube, bf:cube, n_width:int, n_height:int)
function result:mat = apply_bilateral_filter(image:mat, bf:cube, n_width:int, n_height:int)
    height:int = size(image,0)
    width:int  = size(image,1)
    ny2 = floor((n_height-1)/2)
    nx2 = floor((n_width -1)/2)
    
    result = zeros(height, width)
    summ = zeros(height, width)
    
    function [] = __kernel__ fsum(pos:ivec3)
        xoff = mod(pos[2], n_width)-nx2
        yoff = floor(pos[2]/n_width)-ny2
        if(pos[0]+yoff < 0 || pos[0]+yoff >= height || pos[1]+xoff < 0 || pos[1]+xoff >= width)
            return 
        endif
        result[pos[0], pos[1]] += image[pos[0]+yoff, pos[1]+xoff]*bf[pos]
        summ[pos[0], pos[1]] += bf[pos]
    end
    parallel_do([height, width, n_height*n_width], fsum)
    
    function [] = __kernel__ fnorm(pos:ivec2)
        x = result[pos] / summ[pos[0], pos[1]]
        result[pos] = x
    end
    parallel_do(size(result), fnorm)    
end

function [] = main()
    
    sigma = 20
    
    img = imread("lena_big.tif")
    img = img[:,:,0]
    
    img_noisy = img + randn(size(img))*sigma
    
    nx = 7
    ny = 7
    alpha = 10000 
    beta = 4
    
    bf = compute_bilateral_filter(img_noisy, nx,ny, alpha, beta, 0, 0)
    img_den = apply_bilateral_filter(img_noisy, bf, nx, ny)
    
    % computation of the PSNR
    psnr = (x, y) -> 10*log10(255^2/mean((x-y).^2))

    hold("off")
    fig1=imshow(img_noisy,[0,255])
    title(sprintf("Noisy image - psnr=%f dB", psnr(img_noisy,img)))
    fig2=imshow(img_den,[0,255])
    title(sprintf("Bilateral filter - psnr=%f dB", psnr(img_den,img)))
    
    fig1.connect(fig2)
end
