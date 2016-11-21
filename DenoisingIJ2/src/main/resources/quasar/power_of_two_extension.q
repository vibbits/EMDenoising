% Frank Vernaillen
% November 2016

import "system.q"

% Function: power_of_two_extension
%
% Extends the given image to obtain a new image that has dimensions that
% are powers of two. The extension is done by centering the original image
% and mirroring along its edges.
%
% This is useful for extending images before passing them to transformations
% that only operate on power-of-two images.
%
% Parameters:
% img - input image
%
% Return values:
% img2 - The extended image. Its dimensions are powers of two:
%        either the original size or (for each dimension)the first larger value that is a power of 2.
% topleft - The coordinates of the top-left corner of the original image with respect
%           to the top left corner of the extended image. Useful for extracting the actual image 
%           (or a transformed version of it) again later.
%
function [img2 : mat, topleft] = power_of_two_extension(img)
    sz = size(img)
    sz2 = nextpow2(sz)            % closest powers of two that are the same size or larger as the original image
    
    % FIXME: images consisting of a single column are not supported
    % (the size operator will return a single element, not a [height, width] array, which breaks the code below!)
    assert(numel(sz) == 2)
    
 	if (all(sz == sz2))
 		topleft = [0, 0]
 		img2 = img
 	else   
	    topleft = floor((sz2-sz)/2)   % position of top left corner of the original image relative to the top left corner of its extension
	    
	    [tly, tlx] = topleft
	    [h, w] = sz
	    [newh, neww] = sz2    
	
	    img2 = uninit(sz2)
	    
	    % copy original image to center of extended image
	    img2[tly .. tly+h-1,  tlx .. tlx+w-1] = img
	    
	    % mirror the top part of the image
	    img2[0 .. tly-1, tlx .. tlx+w-1] = img[tly-1 .. -1 .. 0, 0 .. w-1]
	    
	    % mirror the bottom part
	    img2[tly+h .. newh-1, tlx .. tlx+w-1] = img[h-1 .. -1 .. (2*h-newh+tly), 0 .. w-1]
	    
	    % mirror the new image so far along its left edge
	    img2[:, 0 .. tlx-1] = img2[:, 2*tlx-1.. -1 .. tlx];
	    
	    % mirror along the right edge
	    img2[:, tlx+w .. neww-1] = img2[:, tlx+w-1 .. -1 .. 2*(tlx+w)-neww];
	endif
end

function [] = main()
    lena = imread("lena_big.tif")[:,:,0]
    lena_crop = lena[150..150+128, 150..150+256]  % extract an 129 x 257 image
    printf("Original image size: %d", size(lena_crop))
    
    [lena_pow2, topleft] = power_of_two_extension(lena_crop)
    imshow(lena_pow2)
    printf("Power-of-two mirror extended image size: %d topleft: %d", size(lena_pow2), topleft)
end
