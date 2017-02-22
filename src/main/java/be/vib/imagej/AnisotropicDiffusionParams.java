package be.vib.imagej;

public class AnisotropicDiffusionParams
{
	public static final float diffusionFactorMin = 0.01f;
	public static final float diffusionFactorMax = 200.0f;

	public final int numIterations = 5;       // number of diffusion iterations, each iteration takes a step of size stepSize
	public final float stepSize = 0.2f;       // sometimes called dt
	public final int diffusionFunction = 0;   // 0 = exponential, 1 = quadratic

	public float diffusionFactor;             // sometimes called k
	
	public AnisotropicDiffusionParams()
	{
		diffusionFactor = 80.0f;
	}
	
	public AnisotropicDiffusionParams(AnisotropicDiffusionParams other)
	{
		this.diffusionFactor = other.diffusionFactor;
	}
	
	@Override
	public String toString()
	{
		return "diffusion factor " + diffusionFactor;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		AnisotropicDiffusionParams other = (AnisotropicDiffusionParams)obj;
		
		return (obj instanceof AnisotropicDiffusionParams) && (diffusionFactor == other.diffusionFactor);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(diffusionFactor).hashCode();
	}
}
