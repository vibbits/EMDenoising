package be.vib.imagej;

import java.util.Properties;

public class BLSGSMParams extends DenoiseParams
{
	public float sigma;
	public int scales; // = J = number of analysis scales
	
	public static final int scalesMin = 3;
	public static final int scalesMax = 7;

	public float sigmaMin;
	public float sigmaMax;
	
	public BLSGSMParams()
	{
		sigma = 0.25f;
		sigmaMin = 0.001f;
		sigmaMax = 0.5f;
		scales = 4;
	}
	
	public BLSGSMParams(float sigma, int scales)
	{
		this.sigma = sigma;
		this.sigmaMin = 0.001f;
		this.sigmaMax = 0.5f;
		this.scales = scales;
	}
	
	public BLSGSMParams(BLSGSMParams other)
	{
		this.sigma = other.sigma;
		this.sigmaMin = other.sigmaMin;
		this.sigmaMax = other.sigmaMax;
		this.scales = other.scales;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "blsgsm");
    	props.setProperty(PREFIX + "blsgsm.sigma", Float.toString(sigma));
    	props.setProperty(PREFIX + "blsgsm.scales", Integer.toString(scales));
    	return props;
    }

	@Override
	public String toString()
	{
		return "sigma " + sigma + " scales " + scales;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		BLSGSMParams other = (BLSGSMParams)obj;
		
		return (obj instanceof BLSGSMParams) && (sigma == other.sigma) && (sigmaMin == other.sigmaMin) && (sigmaMax == other.sigmaMax) && (scales == other.scales);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(sigma).hashCode() ^ Float.valueOf(sigmaMin).hashCode() ^ Float.valueOf(sigmaMax).hashCode() ^ Integer.valueOf(scales).hashCode();
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		scales = 4;
		sigma = noiseEstimate;
		
		System.out.println("BLSGSMParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> sigma=" + sigma +" ["+ sigmaMin + ", " + sigmaMax + "]");
	}
}
