% Denoising algorithms, courtesy of the UGent IPI research group.
% http://ipi.ugent.be
% This .q file bundles the various algorithms in a single .q file.
% We can then compile it into a single .qlib that can be used in the EM Denoising plugin for Fiji.
import "anisotropic_diffusion.q" 
import "nlmeans_scd.q"                       
import "bilateral_filter.q"     
import "gaussian_filter.q"             
import "wavelet_thresholding.q"
import "blsgsm.q"