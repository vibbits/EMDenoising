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
		NONLOCALMEANS,
		TIKHONOV
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

    // Returns the denoising parameters (a reference, not a deep copy)
    abstract public DenoiseParams getParams();

    // Returns a copy of the denoising parameters (a deep copy, not a reference)
    // This is useful if you want a snapshot of the parameters at this point in time
    // but for use in a task that will be executed asynchronously later.
    abstract public DenoiseParams getParamsCopy();

	// Returns an image denoiser. Since the denoiser will be used as a task that will be executed asynchronously,
	// a snapshot (deep copy) is taken of the the denoising parameters as they are at this point in time.
    abstract public Denoiser getDenoiserCopy();
    
    abstract public DenoiseParamsPanelBase getPanel();

	public void setDefaultParameters(float noiseEstimate)
	{
		getParams().setDefaultParameters(noiseEstimate);
		getPanel().updatePanelFromParams();
	}
}
