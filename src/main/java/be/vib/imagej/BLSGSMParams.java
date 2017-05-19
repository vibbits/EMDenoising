package be.vib.imagej;

public class BLSGSMParams
{
	public float sigma;
	public int scales; // =J
	
    //public static final int J = 4; // number of analysis scales
	
	public static final int scalesMin = 3;
	public static final int scalesMax = 7;

	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 0.5f;
	
	public BLSGSMParams()
	{
		sigma = 0.25f;
		scales = 4;
	}
	
	public BLSGSMParams(BLSGSMParams other)
	{
		this.sigma = other.sigma;
		this.scales = other.scales;
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
		
		return (obj instanceof BLSGSMParams) && (sigma == other.sigma) && (scales == other.scales);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(sigma).hashCode() ^ Integer.valueOf(scales).hashCode();
	}
}
