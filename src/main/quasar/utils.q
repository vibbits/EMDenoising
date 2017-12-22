
% Generates gaussian filter of size N and standard deviation sigma
function [h:mat] = fgaussian(N:int, sigma:scalar)
    center = (N-1)/2
    n_min = -floor(center)
    n_max = ceil(center)
    hg = zeros(N,N)
    s = 0
    for i=n_min..n_max
        for j=n_min..n_max
            hg[i-n_min,j-n_min] = exp(-((i-(n_max-center))^2+(j-(n_max-center))^2)/(2*sigma^2))
            s = s + hg[i-n_min,j-n_min]
        end
    end
    h = hg ./ s
end
