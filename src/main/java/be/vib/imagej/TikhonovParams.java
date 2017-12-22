package be.vib.imagej;

import java.util.Properties;

public class TikhonovParams extends DenoiseParams
{
	public static final float lambdaMin = 0.02f; // CHECKME: the lowest value give a useless result when doing deconvolution
	public static final float lambdaMax = 8.0f; // CHECKME: 5 is better?
	
	public static final float sigmaMin = 0.01f; // CHECKME
	public static final float sigmaMax = 5.0f; // CHECKME
	
	public static final int iterationsMin = 1;
	public static final int iterationsMax = 100;
	
	public static final int blurKernelSize = 15;
	
	public boolean deconvolution;
	public float lambda1; // only used if deconvolution == false
	public float lambda2; // only used if deconvolution == true
	public float sigma; // only used if deconvolution == true
	public int numIterations;
	
	public TikhonovParams()
	{
		deconvolution = false;
		lambda1 = 0.5f;		
		lambda2 = 1.5f;
		sigma = 1.5f;
		numIterations = 50;
	}
	
	public TikhonovParams(TikhonovParams other)
	{
		this.deconvolution = other.deconvolution;
		this.lambda1 = other.lambda1;
		this.lambda2 = other.lambda2;
		this.sigma = other.sigma;
		this.numIterations = other.numIterations;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "tikhonov");
    	props.setProperty(PREFIX + "tikhonov.deconvolution", Boolean.toString(deconvolution));
    	props.setProperty(PREFIX + "tikhonov.lambda", Float.toString(!deconvolution ? lambda1 : lambda2));
    	props.setProperty(PREFIX + "tikhonov.numiterations", Integer.toString(numIterations));
    	if (deconvolution)
    	{
        	props.setProperty(PREFIX + "tikhonov.sigma", Float.toString(sigma));
    	}
    	return props;
    }

	@Override
	public String toString()
	{
		if (!deconvolution)
			return "no deconvolution; lambda " + lambda1 + "; " + numIterations + " iterations";
		else
			return "deconvolution; lambda " + lambda2 + "; sigma " + sigma + "; " + numIterations + " iterations";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		TikhonovParams other = (TikhonovParams)obj;
		
		return (obj instanceof TikhonovParams) && (deconvolution == other.deconvolution) && (lambda1 == other.lambda1) && (lambda2 == other.lambda2) && (sigma == other.sigma) && (numIterations == other.numIterations);
	}
	
	@Override
	public int hashCode()
	{
		return Boolean.valueOf(deconvolution).hashCode() ^ Float.valueOf(lambda1).hashCode() ^ Float.valueOf(lambda2).hashCode() ^ Float.valueOf(sigma).hashCode() ^ Integer.valueOf(numIterations).hashCode() ;
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		assert(noiseEstimate >= 0);

		// FIXME - can we come up with good initial values based on the noise estimate?
		deconvolution = false;
		numIterations = 50;
		lambda1 = 0.5f;
		lambda2 = 1.5f;
		sigma = 1.5f;
		System.out.println("TikhonovParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> deconvolution=" + deconvolution + " lambda=" + (deconvolution ? lambda2 : lambda1) + " sigma=" + sigma);
	}
}
