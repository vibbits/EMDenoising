package be.vib.imagej;

import java.util.Properties;

public class TotalVariationParams extends DenoiseParams
{
	public static final float lambdaMin = 0.01f;
	public static final float lambdaMax = 1.0f;

	public static final int iterationsMin = 1;
	public static final int iterationsMax = 200;

	public static final float alphaMin = 0.01f;
	public static final float alphaMax = 1.0f;

	public float lambda;
	public int numIterations;
	public float alpha;
	
	public TotalVariationParams()
	{
		lambda = 0.05f;
		numIterations = 100;
		alpha = 0.01f;
	}
	
	public TotalVariationParams(TotalVariationParams other)
	{
		this.lambda = other.lambda;
		this.numIterations = other.numIterations;
		this.alpha = other.alpha;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "totalvariation");
    	props.setProperty(PREFIX + "totalvariation.lambda", Float.toString(lambda));
    	props.setProperty(PREFIX + "totalvariation.numiterations", Integer.toString(numIterations));
    	props.setProperty(PREFIX + "totalvariation.alpha", Float.toString(alpha));
    	return props;
    }

	@Override
	public String toString()
	{
		return "lambda " + lambda + "; " + numIterations + " iterations ; alpha " + alpha;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		TotalVariationParams other = (TotalVariationParams)obj;
		
		return (obj instanceof TotalVariationParams) && (lambda == other.lambda) && (numIterations == other.numIterations) && (alpha == other.alpha) ;
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(lambda).hashCode() ^ Integer.valueOf(numIterations).hashCode() ^ Float.valueOf(alpha).hashCode();
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		assert(noiseEstimate >= 0);
		
		// Suggested "ideal" denoising parameters
		
		// FIXME: base the defaults (and their range?) on the noise estimate
		
		lambda = 0.05f;
		numIterations = 100;
		alpha = 0.01f;
		
		System.out.println("TODO TotalVariationParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> lambda=" + lambda + " alpha=" + alpha);
	}
}
