package be.vib.imagej;

import java.util.Properties;

public class AnisotropicDiffusionParams extends DenoiseParams
{
	public static final float stepSizeMin = 0.01f;
	public static final float stepSizeMax = 0.25f;  // above a stepSize of 0.25 we start seeing stipple artifacts (for 8 and 16 bit images, and for various diffusion factors and number of iterations)

	public static final int iterationsMin = 1;
	public static final int iterationsMax = 40;

	public final String diffusionFunction = "exp";   // "exp" = exponential, "quad" = quadratic

	public float diffusionFactorMin;
	public float diffusionFactorMax; // CHECKME: does this depend on the range of image pixels e.g. [0, 255] or [0, 65535] or [0, 1]? Does it depend on the actual range of pixel values, not the range implied by the bit depth?

	public float diffusionFactor; // sometimes called k
	public int numIterations;     // number of diffusion iterations; each iteration takes a time step of size stepSize
	public float stepSize;        // sometimes called dt

	
	public AnisotropicDiffusionParams()
	{
		diffusionFactor = 0.5f;
		diffusionFactorMin = 0.01f;
		diffusionFactorMax = 1.0f;
		
		numIterations = 5;
		stepSize = 0.2f;
		
		// CHECKME
		// - What is the interaction between these parameters?
		//   stepSize * numIterations probably matters, but stepSize has to be small enough to get a reasonably good solution for the diffusion partial differential equation
		// - How does changing stepSize * numIterations compare to changing the diffusionFactor instead?
	}
	
	public AnisotropicDiffusionParams(AnisotropicDiffusionParams other)
	{
		this.diffusionFactor = other.diffusionFactor;
		this.diffusionFactorMin = other.diffusionFactorMin;
		this.diffusionFactorMax = other.diffusionFactorMax;

		this.numIterations = other.numIterations;
		this.stepSize = other.stepSize;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "anisotropicdiffusion");
    	props.setProperty(PREFIX + "anisotropicdiffusion.diffusionfactor", Float.toString(diffusionFactor));
    	props.setProperty(PREFIX + "anisotropicdiffusion.numiterations", Integer.toString(numIterations));
    	props.setProperty(PREFIX + "anisotropicdiffusion.stepsize", Float.toString(stepSize));
    	return props;
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
		
		return (obj instanceof AnisotropicDiffusionParams) && (diffusionFactor == other.diffusionFactor) && (diffusionFactorMin == other.diffusionFactorMin) && (diffusionFactorMax == other.diffusionFactorMax) && (stepSize == other.stepSize) && (numIterations == other.numIterations);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(diffusionFactor).hashCode() ^ Float.valueOf(diffusionFactorMin).hashCode() ^ Float.valueOf(diffusionFactorMax).hashCode() ^ Float.valueOf(stepSize).hashCode() ^ Integer.valueOf(numIterations).hashCode() ;
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		assert(noiseEstimate >= 0);
		
		// Suggested "ideal" denoising parameters
		diffusionFactor = 2.90186309814453f * noiseEstimate * noiseEstimate + 1.53053665161133f * noiseEstimate + 0.00475215911865234f;
		numIterations = 5;
		stepSize = 0.2f;
		
	    // FIXME - manual optimization
		stepSize = 0.1f;
		numIterations = 10;
	    diffusionFactor /= 10.0f;
	    
		
		// Heuristic for useful range
		diffusionFactorMin = 0.0001f;
		diffusionFactorMax = diffusionFactor * 2.0f;

//		System.out.println("AnisotropicDiffusionParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> diffusionFactor=" + diffusionFactor + " ["+ diffusionFactorMin + ", " + diffusionFactorMax + "]");
	}
}
