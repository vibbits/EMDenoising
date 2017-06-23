package be.vib.imagej;

import java.util.Properties;

public class BilateralParams extends DenoiseParams
{
	public int r; // window size
	
	public float h;  // damping parameter; it's actually -h (because we want to avoid a negative range in the UI, and where less negative values (so a slider to the right) would mean less denoising)

	public float hMin;
	public float hMax;
		
	public static final int rMin = 1;
	public static final int rMax = 10;   // TODO: check useful range
		
	public BilateralParams()
	{
		r = 6;

		h = 1.9f;
		hMin = 0.0f;
		hMax = 5.0f;				
	}
	
	public BilateralParams(BilateralParams other)
	{
		this.r = other.r;

		this.h = other.h;
		this.hMin = other.hMin;
		this.hMax = other.hMax;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "bilateral");
    	props.setProperty(PREFIX + "bilateral.h", Float.toString(h));
    	props.setProperty(PREFIX + "bilateral.radius", Integer.toString(r));
    	return props;
    }

	@Override
	public String toString()
	{
		return "h " + h + "; r " + r;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		BilateralParams other = (BilateralParams)obj;
		
		return (obj instanceof BilateralParams) && (h == other.h) && (hMin == other.hMin) && (hMax == other.hMax) && (r == other.r);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(h).hashCode() ^ Float.valueOf(hMin).hashCode() ^ Float.valueOf(hMax).hashCode() ^ Integer.valueOf(r).hashCode();
	}

	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		// Suggested "ideal" denoising parameter
	    r = 6;
  	    h = 33.7677001953125f * noiseEstimate * noiseEstimate - 20.3271179199219f * noiseEstimate - 0.0491275787353516f;
  	    
  	    // FIXME - manual optimization
  	    h /= 2.0f;
		
		// Heuristic for useful range
		hMin = 0.001f;
		hMax = h * 2.0f;
		
//		System.out.println("BilateralParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> h=" + h + " ["+ hMin + ", " + hMax + "]");

	}
}
