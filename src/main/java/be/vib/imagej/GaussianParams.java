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
		sigmaMin = 0.5f;
		sigmaMax = 5.0f;
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
		// Suggested "ideal" denoising parameter
		sigma = 8.06167602539063f * noiseEstimate + 0.534878730773926f;
		
		// FIXME - manual optimization
		sigma *= 0.75f;
		
		// Heuristic for useful sigma parameter range.
		sigmaMin = 0.001f;
		sigmaMax = sigma * 1.2f;
		
		System.out.println("GaussianParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> sigma=" + sigma + " ["+ sigmaMin + ", " + sigmaMax + "]");
	}
}
