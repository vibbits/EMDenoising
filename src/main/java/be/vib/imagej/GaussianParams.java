package be.vib.imagej;

public class GaussianParams
{
	public float sigma;
	
	public static final float sigmaMin = 0.5f;
	public static final float sigmaMax = 10.0f;
	
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
	
	@Override
	public boolean equals(Object obj)
	{
		GaussianParams other = (GaussianParams)obj;
		
		return (obj instanceof GaussianParams) && (sigma == other.sigma);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(sigma).hashCode();
	}
}
