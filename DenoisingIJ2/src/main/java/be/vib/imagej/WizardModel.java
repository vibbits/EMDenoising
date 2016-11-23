package be.vib.imagej;

import java.awt.Rectangle;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * WizardModel stores all model data for the wizard. It is shared by all wizard pages.
 * Typically user input done in one wizard page is stored in the model, and will influence
 * what is shown on another wizard page later.
 *
 */
public class WizardModel
{
	public enum DenoisingAlgorithm
	{
		GAUSSIAN,               // Simple Gaussian low-pass filter
		BILATERAL,              // Bilateral filter
		BLSGSM,                 // BLS-GSM (Bayesian least squares - Gaussian scale mixture)
		WAVELET_THRESHOLDING,
		ANISOTROPIC_DIFFUSION,
		NLMS,                   // Non-local means filtering
		NLMS_SC,                // NLMS-SC, an extension of non-local means filtering that deals with the fact that EM noise is correlated and signal-dependent
		NLMS_SCD,               // NLMS-SCD, like NLMS-SC but with deblurring
		BM3D                    // Block-matching and 3D filtering
	};

	public DenoisingAlgorithm denoisingAlgorithm;
	public NonLocalMeansParams nonLocalMeansParams;	
	public NonLocalMeansSCParams nonLocalMeansSCParams;	
	public NonLocalMeansSCDParams nonLocalMeansSCDParams;	
	public GaussianParams gaussianParams;
	public WaveletThresholdingParams waveletThresholdingParams;
	public AnisotropicDiffusionParams anisotropicDiffusionParams;
	public BLSGSMParams blsgsmParams;
	
	ImagePlus imagePlus; // original image or image stack (a reference not a copy)
	Rectangle roi;  // null if the full image should be used; the ROI bounds are with respect to the original image (not the possibly rescaled on-screen version in the dialog)
	ImageRange range; // the range of slices that need to be denoised

	ImageProcessor previewOrigROI;
	ImageProcessor previewDenoisedROI;
	// TODO: Maybe we want to cache the result of denoising the ROI with the last parameter values used (for each algorithm)?
	//       That would at least allow the user to compare the effect of different algorithms (by toggling the algorithm radio button)
	//       without incurring any waiting.
	
	WizardModel()
	{
		denoisingAlgorithm = DenoisingAlgorithm.GAUSSIAN;
		
		nonLocalMeansParams = new NonLocalMeansParams();
		nonLocalMeansSCParams = new NonLocalMeansSCParams();
		nonLocalMeansSCDParams = new NonLocalMeansSCDParams();
		gaussianParams = new GaussianParams();
		waveletThresholdingParams = new WaveletThresholdingParams();
		anisotropicDiffusionParams = new AnisotropicDiffusionParams();
		blsgsmParams = new BLSGSMParams();
	}
}
