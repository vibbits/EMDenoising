package be.vib.imagej;

public class WaveletThresholdingParams
{
	public float alpha;  // multiplicative scaling factor for the theoretically optimal threshold parameter (so alpha=1 uses the theoretical optimum, not always best in practice!)
	
	public WaveletThresholdingParams()
	{
		alpha = 1.5f;
	}
	
	public WaveletThresholdingParams(WaveletThresholdingParams other)
	{
		this.alpha = other.alpha;
	}
}
