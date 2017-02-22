package be.vib.imagej;

public class BilateralParams
{
	public float alpha;
	public float beta;
	
	public static final float alphaMin = 0.01f;
	public static final float alphaMax = 20000.0f;
	
	public static final float betaMin = 0.01f;
	public static final float betaMax = 20.0f;
	
	public static final int nx = 7;
	public static final int ny = 7;
	
	public static final float euclDist = 0.0f;
	public static final float normalize = 0.0f;
	
	public BilateralParams()
	{
		alpha = 10000.0f;
		beta = 4.0f;
	}
	
	public BilateralParams(BilateralParams other)
	{
		this.alpha = other.alpha;
		this.beta = other.beta;
	}
	
	@Override
	public String toString()
	{
		return "alpha " + alpha + "; beta " + beta;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		BilateralParams other = (BilateralParams)obj;
		
		return (obj instanceof BilateralParams) && (alpha == other.alpha) && (beta == other.beta);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(alpha).hashCode() ^  Float.valueOf(beta).hashCode();
	}
}
