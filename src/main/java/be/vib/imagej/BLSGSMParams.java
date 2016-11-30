package be.vib.imagej;

public class BLSGSMParams
{
	public float sigma;
	
    public static final int J = 4; // number of scales
    public static final int K = 8; // number of orientations
	public static final String sparsityTrf = "dtcwt";
	
	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 100.0f;
	
	public BLSGSMParams()
	{
		sigma = 50.0f;
	}
	
	public BLSGSMParams(BLSGSMParams other)
	{
		this.sigma = other.sigma;
	}
	
	@Override
	public String toString()
	{
		return "sigma " + sigma;
	}
}
