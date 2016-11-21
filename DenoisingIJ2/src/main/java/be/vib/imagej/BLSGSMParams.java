package be.vib.imagej;

public class BLSGSMParams
{
	public float sigma;
	
	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 100.0f;
	
	public BLSGSMParams()
	{
		sigma = 20.0f;
	}
	
	public BLSGSMParams(BLSGSMParams other)
	{
		this.sigma = other.sigma;
	}
}
