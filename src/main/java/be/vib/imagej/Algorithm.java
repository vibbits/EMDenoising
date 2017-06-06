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
		NONLOCALMEANS
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
    abstract public DenoiseParams getParams();

	// Returns an image denoiser. Since the denoiser will be used as a task that will be executed asynchronously,
	// a snapshot (deep copy) is taken of the the denoising parameters as they are at this point in time.
    abstract public Denoiser getDenoiser();
    
    abstract public DenoiseParamsPanelBase getPanel();

}
