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
		NLMS_SCD                // NLMS-SCD, like NLMS-SC but with deblurring
//		BM3D                    // Block-matching and 3D filtering
	};

	public DenoisingAlgorithm denoisingAlgorithm;
	public NonLocalMeansParams nonLocalMeansParams;	
	public NonLocalMeansSCParams nonLocalMeansSCParams;	
	public NonLocalMeansSCDParams nonLocalMeansSCDParams;	
	public GaussianParams gaussianParams;
	public BilateralParams bilateralParams;
	public WaveletThresholdingParams waveletThresholdingParams;
	public AnisotropicDiffusionParams anisotropicDiffusionParams;
	public BLSGSMParams blsgsmParams;
	
	public ImagePlus imagePlus; // original image or image stack (a reference not a copy)
	public Rectangle roi;  // null if the full image should be used; the ROI bounds are with respect to the original image (not the possibly rescaled on-screen version in the dialog)
	public ImageRange range; // the range of slices that need to be denoised

	public ImageProcessor previewOrigROI;
	public ImageProcessor previewDenoisedROI;
	// TODO: Maybe we want to cache the result of denoising the ROI with the last parameter values used (for each algorithm)?
	//       That would at least allow the user to compare the effect of different algorithms (by toggling the algorithm radio button)
	//       without incurring *any* waiting.
	
	public WizardModel()
	{
		denoisingAlgorithm = DenoisingAlgorithm.GAUSSIAN;
		
		nonLocalMeansParams = new NonLocalMeansParams();
		nonLocalMeansSCParams = new NonLocalMeansSCParams();
		nonLocalMeansSCDParams = new NonLocalMeansSCDParams();
		gaussianParams = new GaussianParams();
		bilateralParams = new BilateralParams();
		waveletThresholdingParams = new WaveletThresholdingParams();
		anisotropicDiffusionParams = new AnisotropicDiffusionParams();
		blsgsmParams = new BLSGSMParams();
	}
	
	public Object getDenoisingParams()
	{
		switch (denoisingAlgorithm)
		{
			case GAUSSIAN: return gaussianParams;
			case BILATERAL: return bilateralParams;
			case BLSGSM: return blsgsmParams;
			case WAVELET_THRESHOLDING: return waveletThresholdingParams;
			case ANISOTROPIC_DIFFUSION: return anisotropicDiffusionParams;
			case NLMS: return nonLocalMeansParams;
			case NLMS_SC: return nonLocalMeansSCParams;
			case NLMS_SCD: return nonLocalMeansSCDParams;
			default: assert(false); return null;
		}
	}
	
	public String getDenoisingAlgorithmName()
	{
		switch (denoisingAlgorithm)
		{
			case GAUSSIAN: return "Gaussian";
			case BILATERAL: return "Bilateral";
			case BLSGSM: return "BLS-GSM";
			case WAVELET_THRESHOLDING: return "Wavelet Thresholding";
			case ANISOTROPIC_DIFFUSION: return "Anisotropic Diffusion";
			case NLMS: return "Non-Local Means";
			case NLMS_SC: return "Non-Local Means SC";
			case NLMS_SCD: return "Non-Local Means SCD";
			default: assert(false); return "Unknown algorithm";
		}
	}
}
