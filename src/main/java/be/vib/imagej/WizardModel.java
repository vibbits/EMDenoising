package be.vib.imagej;

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
	
	public ImagePlus imagePlus; // original image or image stack (a reference not a copy). Can be null iff. the wizard shows the WizardPageROI; is non-null otherwise.
    
	public ImageRange range; // the range of slices that need to be denoised

	public ImageProcessor previewOrigROI;
	public ImageProcessor previewDenoisedROI;
	// TODO: Maybe we want to cache the result of denoising the ROI with the last parameter values used (for each algorithm)?
	//       That would at least allow the user to compare the effect of different algorithms (by toggling the algorithm radio button)
	//       without incurring *any* waiting.
	
	public WizardModel()
	{
		denoisingAlgorithm = DenoisingAlgorithm.GAUSSIAN;
		
		range = new ImageRange();
		
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
	
    public Denoiser getDenoiser() 
	{
		// Make an image denoiser. Since it will be used as a task that will be executed asynchronously,
		// we take a snapshot (deep copy) of the the denoising parameters as they are at this point in time.
		
		switch (denoisingAlgorithm)
		{
			case NLMS:
				return new NonLocalMeansDenoiser(new NonLocalMeansParams(nonLocalMeansParams));
			case NLMS_SC:
				return new NonLocalMeansSCDenoiser(new NonLocalMeansSCParams(nonLocalMeansSCParams));
			case NLMS_SCD:
				return new NonLocalMeansSCDDenoiser(new NonLocalMeansSCDParams(nonLocalMeansSCDParams));
			case GAUSSIAN:
				return new GaussianDenoiser(new GaussianParams(gaussianParams));
			case BILATERAL:
				return new BilateralDenoiser(new BilateralParams(bilateralParams));
			case WAVELET_THRESHOLDING:
				return new WaveletThresholdingDenoiser(new WaveletThresholdingParams(waveletThresholdingParams));
			case ANISOTROPIC_DIFFUSION:
				return new AnisotropicDiffusionDenoiser(new AnisotropicDiffusionParams(anisotropicDiffusionParams));
			case BLSGSM:
				return new BLSGSMDenoiser(new BLSGSMParams(blsgsmParams));
			default:
				assert(false);
				return new NoOpDenoiser();
		}
	}
}
