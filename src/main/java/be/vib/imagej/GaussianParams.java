package be.vib.imagej;

import java.util.Properties;

public class GaussianParams extends DenoiseParams
{
	public float sigma;
	public float sigmaMin;
	public float sigmaMax;
	
	public GaussianParams()
	{
		sigma = 1.5f;
		sigmaMin = 0.001f;
		sigmaMax = 4.5f;
	}
	
	public GaussianParams(float sigma)
	{
		this.sigma = sigma;
		this.sigmaMin = 0.001f;
		this.sigmaMax = 4.5f;
	}
	
	public GaussianParams(GaussianParams other)
	{
		this.sigma = other.sigma;
		this.sigmaMin = other.sigmaMin;
		this.sigmaMax = other.sigmaMax;
	}

	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "gaussian");
    	props.setProperty(PREFIX + "gaussian.sigma", Float.toString(sigma));
    	return props;
    }
	
	@Override
	public String toString()
	{
		return "sigma " + sigma;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		GaussianParams other = (GaussianParams)obj;
		
		return (obj instanceof GaussianParams) && (sigma == other.sigma) && (sigmaMin == other.sigmaMin) && (sigmaMax == other.sigmaMax);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(sigma).hashCode() ^ Float.valueOf(sigmaMin).hashCode() ^ Float.valueOf(sigmaMax).hashCode();
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		sigma = 8.00950813293457f * noiseEstimate + 0.366672605276108f;
		
		System.out.println("GaussianParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> sigma=" + sigma + " ["+ sigmaMin + ", " + sigmaMax + "]");
	}
}
