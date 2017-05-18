package be.vib.imagej;

public class WaveletThresholdingParams
{
	public float threshold;

	public static final int J = 3;  // number of analysis scales
	public static final String thresholdType = "soft"; // use soft thresholding ("hard" is for hard thresholding)
	public static final float thresholdMin = 0.0f;
	public static final float thresholdMax = 4.0f;
	
	public WaveletThresholdingParams()
	{
		threshold = 1.0f;
	}
	
	public WaveletThresholdingParams(WaveletThresholdingParams other)
	{
		this.threshold = other.threshold;
	}

	@Override
	public String toString()
	{
		return "threshold " + threshold;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		WaveletThresholdingParams other = (WaveletThresholdingParams)obj;
		
		return (obj instanceof WaveletThresholdingParams) && (threshold == other.threshold);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(threshold).hashCode();
	}
}
