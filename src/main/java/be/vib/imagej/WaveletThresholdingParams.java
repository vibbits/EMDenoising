package be.vib.imagej;

import java.util.Properties;

public class WaveletThresholdingParams extends DenoiseParams
{
	public float threshold;
	public float thresholdMin;
	public float thresholdMax;

	public static final int J = 3;  // number of analysis scales
	public static final String thresholdType = "soft"; // use soft thresholding ("hard" is for hard thresholding)
	
	public WaveletThresholdingParams()
	{
		threshold = 0.5f;
		thresholdMin = 0.0f;
		thresholdMax = 2.0f;
	}
	
	public WaveletThresholdingParams(WaveletThresholdingParams other)
	{
		this.threshold = other.threshold;
		this.thresholdMin = other.thresholdMin;
		this.thresholdMax = other.thresholdMax;
	}

	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "waveletthresholding");
    	props.setProperty(PREFIX + "waveletthresholding.threshold", Float.toString(threshold));
    	return props;
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
		
		return (obj instanceof WaveletThresholdingParams) && (threshold == other.threshold) && (thresholdMin == other.thresholdMin) && (thresholdMax == other.thresholdMax);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(threshold).hashCode() ^ Float.valueOf(thresholdMin).hashCode() ^ Float.valueOf(thresholdMax).hashCode();
	}
	
	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		assert(noiseEstimate >= 0);
		
		// Suggested "ideal" denoising parameter
		threshold = 6.83660888671875f * noiseEstimate * noiseEstimate + 2.34318542480469f * noiseEstimate - 0.00547122955322266f;
		
		// FIXME? Manually optimized, why does above formula not yield a better result?
		threshold /= 8.0f;
		
		// Heuristic for useful range
		thresholdMin = 0.001f;
		thresholdMax = threshold * 1.2f;
		
		System.out.println("WaveletThresholdingParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> threshold=" + threshold + " ["+ thresholdMin + ", " + thresholdMax + "]");
	}
}
