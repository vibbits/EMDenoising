package be.vib.imagej;

public class WaveletThresholdingParams
{
	public float alpha;  // multiplicative scaling factor for the theoretically optimal threshold parameter (so alpha=1 uses the theoretical optimum, not always best in practice!)

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
}
