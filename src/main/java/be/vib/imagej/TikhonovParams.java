package be.vib.imagej;

import java.util.Properties;

public class TikhonovParams extends DenoiseParams
{
	public static final float lambdaMin = 0.05f;
	public static final float lambdaMax = 75.0f;
	
	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 5.0f;
	
	public static final int iterationsMin = 1;
	public static final int iterationsMax = 100;
	
	public static final int blurKernelSize = 15;
	
	public boolean deconvolution;
	public float lambda; 
	public float sigma; // only used if deconvolution == true
	public int numIterations;
		
	public TikhonovParams()
	{
		deconvolution = false;
		lambda = 10.0f;		
		sigma = 1.0f;
		numIterations = 50;
	}
	
	public TikhonovParams(boolean deconvolution, float lambda, float sigma, int numIterations)
	{
		this.deconvolution = deconvolution;
		this.lambda = lambda;	
		this.sigma = sigma;
		this.numIterations = numIterations;
	}
	
	public TikhonovParams(TikhonovParams other)
	{
		this.deconvolution = other.deconvolution;
		this.lambda = other.lambda;
		this.sigma = other.sigma;
		this.numIterations = other.numIterations;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "tikhonov");
    	props.setProperty(PREFIX + "tikhonov.deconvolution", Boolean.toString(deconvolution));
    	props.setProperty(PREFIX + "tikhonov.lambda", Float.toString(lambda));
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
			return "no deconvolution; lambda " + lambda + "; " + numIterations + " iterations";
		else
			return "deconvolution; lambda " + lambda + "; sigma " + sigma + "; " + numIterations + " iterations";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		TikhonovParams other = (TikhonovParams)obj;
		return (obj instanceof TikhonovParams) && (deconvolution == other.deconvolution) && (lambda == other.lambda) && (sigma == other.sigma) && (numIterations == other.numIterations);
	}
	
	@Override
	public int hashCode()
	{
		return Boolean.valueOf(deconvolution).hashCode() ^ Float.valueOf(lambda).hashCode() ^ Float.valueOf(sigma).hashCode() ^ Integer.valueOf(numIterations).hashCode() ;
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		assert(noiseEstimate >= 0);		
		deconvolution = false;
		numIterations = 50;
		lambda = Math.max(lambdaMin, 413.051574707031f * noiseEstimate * noiseEstimate - 59.6000556945801f * noiseEstimate + 1.8710173368454f);
		sigma = 1.0f;
//		System.out.println("TikhonovParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> deconvolution=" + deconvolution + " lambda=" + lambda + " sigma=" + sigma);
	}
}
