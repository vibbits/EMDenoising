package be.vib.imagej;

public class GaussianParams
{
	public float sigma;
	
	public GaussianParams()
	{
		sigma = 1.5f;
	}
	
	public GaussianParams(GaussianParams other)
	{
		this.sigma = other.sigma;
	}
}
