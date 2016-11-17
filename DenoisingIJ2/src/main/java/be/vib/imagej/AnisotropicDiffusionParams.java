package be.vib.imagej;

public class AnisotropicDiffusionParams
{
	public float diffusionFactor;             // sometimes called k
	public final int numIterations = 5;       // number of diffusion iterations, each iteration takes a step of size stepSize
	public final float stepSize = 0.2f;       // sometimes called dt
	public final int diffusionFunction = 0;   // 0 = exponential, 1 = quadratic
	
	public AnisotropicDiffusionParams()
	{
		diffusionFactor = 40.0f;
	}
	
	public AnisotropicDiffusionParams(AnisotropicDiffusionParams other)
	{
		this.diffusionFactor = other.diffusionFactor;
	}
}
