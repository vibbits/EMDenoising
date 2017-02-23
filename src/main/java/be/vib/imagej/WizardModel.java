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
		NLMS_SCD                // NLMS-SCD, an extension of non-local means filtering that deals with the fact that EM noise is correlated and signal-dependent, and, optionally does a deconvolution to try and undo lens blur
//		BM3D                    // Block-matching and 3D filtering
	};

	public DenoisingAlgorithm denoisingAlgorithm;

	public GaussianParams gaussianParams;
	public BilateralParams bilateralParams;
	public BLSGSMParams blsgsmParams;
	public WaveletThresholdingParams waveletThresholdingParams;
	public AnisotropicDiffusionParams anisotropicDiffusionParams;
	public NonLocalMeansParams nonLocalMeansParams;	
	public NonLocalMeansSCDParams nonLocalMeansSCDParams;	

	public ImagePlus imagePlus; // original image or image stack (a reference not a copy). Can be null iff. the wizard shows the WizardPageROI; is non-null otherwise.
    
	public ImageRange range; // the range of slices that need to be denoised

	public ImageProcessor previewOrigROI;
	
	public WizardModel()
	{
		denoisingAlgorithm = DenoisingAlgorithm.GAUSSIAN;
		
		gaussianParams = new GaussianParams();
		bilateralParams = new BilateralParams();
		blsgsmParams = new BLSGSMParams();
		waveletThresholdingParams = new WaveletThresholdingParams();
		anisotropicDiffusionParams = new AnisotropicDiffusionParams();
		nonLocalMeansParams = new NonLocalMeansParams();
		nonLocalMeansSCDParams = new NonLocalMeansSCDParams();

		range = new ImageRange();
	}
	
	// Returns a copy of the current denoising parameters. 
	public Object getDenoisingParams()
	{
		switch (denoisingAlgorithm)
		{
			case GAUSSIAN: return new GaussianParams(gaussianParams);
			case BILATERAL: return new BilateralParams(bilateralParams);
			case BLSGSM: return new BLSGSMParams(blsgsmParams);
			case WAVELET_THRESHOLDING: return new WaveletThresholdingParams(waveletThresholdingParams);
			case ANISOTROPIC_DIFFUSION: return new AnisotropicDiffusionParams(anisotropicDiffusionParams);
			case NLMS: return new NonLocalMeansParams(nonLocalMeansParams);
			case NLMS_SCD: return new NonLocalMeansSCDParams(nonLocalMeansSCDParams);
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
