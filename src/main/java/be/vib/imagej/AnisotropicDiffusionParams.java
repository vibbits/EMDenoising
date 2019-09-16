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
	public float diffusionFactorMax;

	public float diffusionFactor;  // sometimes called k
	public int numIterations;  // number of diffusion iterations; each iteration takes a time step of size stepSize
	public float stepSize;  // sometimes called dt

	
	public AnisotropicDiffusionParams()
	{
		diffusionFactor = 0.5f;
		diffusionFactorMin = 0.001f;
		diffusionFactorMax = 1.5f;
		numIterations = 5;
		stepSize = 0.2f;
	}
	
	public AnisotropicDiffusionParams(float diffusionFactor, int numIterations, float stepSize)
	{
		this.diffusionFactor = diffusionFactor;
		this.diffusionFactorMin = 0.001f;
		this.diffusionFactorMax = 1.5f;
		this.numIterations = numIterations;
		this.stepSize = stepSize;
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
		
		diffusionFactor = Math.max(diffusionFactorMin, 2.77789974212646f * noiseEstimate * noiseEstimate + 1.54128849506378f * noiseEstimate);  // note: avoids division by zero for diffusion factor = 0
		numIterations = 5;
		stepSize = 0.2f;

		System.out.println("AnisotropicDiffusionParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> diffusionFactor=" + diffusionFactor + " ["+ diffusionFactorMin + ", " + diffusionFactorMax + "]");
	}
}
