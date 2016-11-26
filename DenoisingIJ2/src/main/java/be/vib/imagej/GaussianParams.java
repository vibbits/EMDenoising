package be.vib.imagej;

public class GaussianParams
{
	public float sigma;
	
	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 20.0f;
	
	public GaussianParams()
	{
		sigma = 1.5f;
	}
	
	public GaussianParams(GaussianParams other)
	{
		this.sigma = other.sigma;
	}

	@Override
	public String toString() {
		return "sigma " + sigma;
	}
}
