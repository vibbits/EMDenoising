package be.vib.imagej;

import java.util.Properties;

public class BilateralParams extends DenoiseParams
{
	public int r;
	public float h;  // it's actually -h (because we want to avoid a negative range in the UI, and where less negative values (so a slider to the right) would mean less denoising)

	public static final int rMin = 1;
	public static final int rMax = 10;   // FIXME: check useful range
		
	public static final float hMin = 0;
	public static final float hMax = 5;
		
	public BilateralParams()
	{
		h = 1.9f;
		r = 6;
	}
	
	public BilateralParams(BilateralParams other)
	{
		this.h = other.h;
		this.r = other.r;
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
		
		return (obj instanceof BilateralParams) && (h == other.h) && (r == other.r);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(h).hashCode() ^  Integer.valueOf(r).hashCode();
	}
}
