{!author name="Joris Roels"}
{!doc category="Scripts/Blur level"}

% Section: Overview
%
% This is a straightforward port of the blurMetric MATLAB implementation:
% https://nl.mathworks.com/matlabcentral/fileexchange/24676-image-blur-metric
% The method is described in 
%   "The Blur Effect: Perception and Estimation with a New No-Reference Perceptual Blur Metric" 
%   Crete-Roffet F., Dolmiere T., Ladret P., Nicolas M. - GRENOBLE - 2007 
%   In SPIE proceedings - SPIE Electronic Imaging Symposium Conf Human Vision and Electronic 
%   Imaging, Etats-Unis d'Amerique (2007)
% 
import "imfilter.q"

% Function: blur_metric
%
% Computes the blur level of the specified input image.
function blur = blurMetric(I)
    [y, x] = size(I)
    
    Hv = [1, 1, 1, 1, 1, 1, 1, 1, 1] / 9
    Hh = transpose(Hv)
    
    B_Ver = imfilter(I,Hv)  % blur the input image in vertical direction
    B_Hor = imfilter(I,Hh)  % blur the input image in horizontal direction
    
    D_F_Ver = abs(I[:,0..x-2] - I[:,1..x-1])  % variation of the input image (vertical direction)
    D_F_Hor = abs(I[0..y-2,:] - I[1..y-1,:])  % variation of the input image (horizontal direction)

    D_B_Ver = abs(B_Ver[:,0..x-2] - B_Ver[:,1..x-1])  % variation of the blurred image (vertical direction)
    D_B_Hor = abs(B_Hor[0..y-2,:] - B_Hor[1..y-1,:])  % variation of the blurred image (horizontal direction)

    T_Ver = D_F_Ver - D_B_Ver % difference between two vertical variations of 2 image (input and blurred)
    T_Hor = D_F_Hor - D_B_Hor % difference between two horizontal variations of 2 image (input and blurred)

    V_Ver = max(0,T_Ver)
    V_Hor = max(0,T_Hor)

    S_D_Ver = sum(D_F_Ver[1..y-2,1..x-2])
    S_D_Hor = sum(D_F_Hor[1..y-2,1..x-2])

    S_V_Ver = sum(V_Ver[1..y-2,1..x-2])
    S_V_Hor = sum(V_Hor[1..y-2,1..x-2])

    blur_F_Ver = (S_D_Ver-S_V_Ver)/S_D_Ver
    blur_F_Hor = (S_D_Hor-S_V_Hor)/S_D_Hor

    blur = max(blur_F_Ver,blur_F_Hor)
end

function [] = main()
    img_in = imread("lena_big.tif")
    img_in = mean(img_in, 2)
    blur = blurMetric(img_in)
    print(blur)
end
