package be.vib.imagej;

// The Algorithm class groups three different facets of a denoising algorithm implementation:
// - the denoiser itself, which is a wrapper around the Quasar implementation of the denoising algorithm
// - the parameters that influence the denoising algorithm
// - the user interface that offers the user controls for specifying the parameters

public abstract class Algorithm
{
	public enum Name
	{
		GAUSSIAN,               // Simple Gaussian low-pass filter
		BILATERAL,              // Bilateral filter
		BLSGSM,                 // BLS-GSM (Bayesian Least Squares - Gaussian Scale Mixture)
		WAVELET_THRESHOLDING,
		ANISOTROPIC_DIFFUSION,
		NLMS,                   // Non-local means filter
		NLMS_SCD                // NLMS-SCD, an extension of the non-local means filter which deals with the fact that EM noise is correlated and signal-dependent, and, optionally does a deconvolution to try and undo blurring
//		BM3D                    // Block-matching and 3D filtering
	};
	
	private Name name;
	
	public Algorithm(Name name)
	{
		this.name = name;
	}
	
	public Name getName()
	{
		return name;
	}

	// Returns the name of the denoising algorithm as a user readable string. Meant for displaying in the user interface.
    abstract public String getReadableName();

    // Returns a copy of the denoising parameters (so a deep copy, not a reference)
    abstract public Object getParams();

	// Returns an image denoiser. Since the denoiser will be used as a task that will be executed asynchronously,
	// a snapshot (deep copy) is taken of the the denoising parameters as they are at this point in time.
    abstract public Denoiser getDenoiser();
    
    // TODO
    abstract public DenoiseParamsPanelBase getPanel();
}
