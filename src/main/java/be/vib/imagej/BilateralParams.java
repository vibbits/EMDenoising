package be.vib.imagej;

import java.util.Properties;

public class BilateralParams extends DenoiseParams
{
	// sigma for pixel intensity range
	public float rangeSigma;
	public float rangeSigmaMin;
	public float rangeSigmaMax;
	
	// sigma for spatial extent
	public float spatialSigma;
	public float spatialSigmaMin;
	public float spatialSigmaMax;
		
	public BilateralParams()
	{
		rangeSigma = 50.0f;
		rangeSigmaMin = 1.0f;
		rangeSigmaMax = 75.0f;
		
		spatialSigma = 2.0f;
		spatialSigmaMin = 1.0f;
		spatialSigmaMax = 25.0f;	
	}
	
	public BilateralParams(float rangeSigma, float spatialSigma)
	{
		this.rangeSigma = rangeSigma;
		this.rangeSigmaMin = 1.0f;
		this.rangeSigmaMax = 75.0f;

		this.spatialSigma = spatialSigma;
		this.spatialSigmaMin = 1.0f;
		this.spatialSigmaMax = 25.0f;;
	}
	
	public BilateralParams(BilateralParams other)
	{
		this.rangeSigma = other.rangeSigma;
		this.rangeSigmaMin = other.rangeSigmaMin;
		this.rangeSigmaMax = other.rangeSigmaMax;
		
		this.spatialSigma = other.spatialSigma;
		this.spatialSigmaMin = other.spatialSigmaMin;
		this.spatialSigmaMax = other.spatialSigmaMax;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "bilateral");
    	props.setProperty(PREFIX + "bilateral.rangeSigma", Float.toString(rangeSigma));
    	props.setProperty(PREFIX + "bilateral.spatialSigma", Float.toString(spatialSigma));
    	return props;
    }

	@Override
	public String toString()
	{
		return "rangeSigma " + rangeSigma + "; spatialSigma " + spatialSigma;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		BilateralParams other = (BilateralParams)obj;
		
		return (obj instanceof BilateralParams) &&
			   (rangeSigma == other.rangeSigma) && (rangeSigmaMin == other.rangeSigmaMin) && (rangeSigmaMax == other.rangeSigmaMax) && 
			   (spatialSigma == other.spatialSigma) && (spatialSigmaMin == other.spatialSigmaMin) && (spatialSigmaMax == other.spatialSigmaMax);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(rangeSigma).hashCode() ^ Float.valueOf(rangeSigmaMin).hashCode() ^ Float.valueOf(rangeSigmaMax).hashCode() ^
			   Float.valueOf(spatialSigma).hashCode() ^ Float.valueOf(spatialSigmaMin).hashCode() ^ Float.valueOf(spatialSigmaMax).hashCode();
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		// TODO
		
		System.out.println("BilateralParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> rangeSigma=" + rangeSigma + " ["+ rangeSigmaMin + ", " + rangeSigmaMax + "]" +
				                                                                                  " spatialSigma=" + spatialSigma + " ["+ spatialSigmaMin + ", " + spatialSigmaMax + "]");

	}
}
