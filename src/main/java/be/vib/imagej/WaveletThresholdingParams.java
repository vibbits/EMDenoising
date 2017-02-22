package be.vib.imagej;

public class WaveletThresholdingParams
{
	public float alpha;  // multiplicative scaling factor for the theoretically optimal threshold parameter (so alpha=1 uses the theoretical optimum, not always best in practice!)

	public static final int J = 6;  // number of scales
	public static final float sigma = 20.0f;
	public static final String thresholding = "soft"; // use soft thresholding ("hard" is for hard thresholding)
	
	public static final float alphaMin = 0.01f;
	public static final float alphaMax = 25.0f;
	
	public WaveletThresholdingParams()
	{
		alpha = 5.0f;
	}
	
	public WaveletThresholdingParams(WaveletThresholdingParams other)
	{
		this.alpha = other.alpha;
	}

	@Override
	public String toString()
	{
		return "alpha " + alpha;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		WaveletThresholdingParams other = (WaveletThresholdingParams)obj;
		
		return (obj instanceof WaveletThresholdingParams) && (alpha == other.alpha);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(alpha).hashCode();
	}
}
