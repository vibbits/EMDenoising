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
		threshold = 1.0f;
		thresholdMin = 0.0f;
		thresholdMax = 2.75f;
	}

	public WaveletThresholdingParams(float threshold)
	{
		this.threshold = threshold;
		this.thresholdMin = 0.0f;
		this.thresholdMax = 2.75f;
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
		
		threshold = 6.69702291488647f * noiseEstimate * noiseEstimate + 2.10050129890442f * noiseEstimate;

		System.out.println("WaveletThresholdingParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> threshold=" + threshold + " ["+ thresholdMin + ", " + thresholdMax + "]");
	}
}
