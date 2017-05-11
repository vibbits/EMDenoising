package be.vib.imagej;

public class AnisotropicDiffusionParams
{
	public static final float diffusionFactorMin = 0.01f;
	public static final float diffusionFactorMax = 1.0f; // CHECKME: this depends on the range of image pixels e.g. [0, 255] or [0, 65535] or [0, 1]. Annoying, it probably depends on the actual range of pixel values, not the range implied by the bit depth.

	public static final float stepSizeMin = 0.01f;
	public static final float stepSizeMax = 0.25f;  // above a stepSize of 0.25 we start seeing stipple artifacts (for 8 and 16 bit images, and for variaous diffusion factors and number of iterations)

	public static final int iterationsMin = 1;
	public static final int iterationsMax = 40;

	public final String diffusionFunction = "exp";   // "exp" = exponential, "quad" = quadratic

	public float diffusionFactor;             // sometimes called k
	public int numIterations;       // number of diffusion iterations, each iteration takes a step of size stepSize
	public float stepSize;       // sometimes called dt

	
	public AnisotropicDiffusionParams()
	{
		diffusionFactor = 0.5f;
		numIterations = 5;
		stepSize = 0.2f;
		// FIXME? what is the interaction between these parameters?  stepSize * numIterations probably matters (but stepSize has to be small enough to avoid numerical instability?)
		//        how does changing stepSize * numIterations compare to changing the diffusionFactor instead?
	}
	
	public AnisotropicDiffusionParams(AnisotropicDiffusionParams other)
	{
		this.diffusionFactor = other.diffusionFactor;
		this.numIterations = other.numIterations;
		this.stepSize = other.stepSize;
	}
	
	@Override
	public String toString()
	{
		return "diffusion factor " + diffusionFactor + "; " + numIterations + " iterations ; step size " + stepSize;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		AnisotropicDiffusionParams other = (AnisotropicDiffusionParams)obj;
		
		return (obj instanceof AnisotropicDiffusionParams) && (diffusionFactor == other.diffusionFactor) && (stepSize == other.stepSize) && (numIterations == other.numIterations);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(diffusionFactor).hashCode() ^ Float.valueOf(stepSize).hashCode() ^ Integer.valueOf(numIterations).hashCode() ;
	}

	// Set default algorithm parameters based on the image noise estimate 'sigmaEstimate'.
	public void setDefaultParameters(float sigmaEstimate)
	{
		diffusionFactor = 2.90186309814453f * sigmaEstimate * sigmaEstimate + 1.53053665161133f * sigmaEstimate + 0.00475215911865234f;
	}	
}
