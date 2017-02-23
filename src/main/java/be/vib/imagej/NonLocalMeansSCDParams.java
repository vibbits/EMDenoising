package be.vib.imagej;

public class NonLocalMeansSCDParams
{
	public static final float hMin = 0.01f;
	public static final float hMax = 75.0f;
	
	public static final float sigma0Min = 0.01f;
	public static final float sigma0Max = 100.0f;
	
	public static final int halfSearchSize = 5;
	public static final int halfBlockSize = 4;
	public static final float alpha = 0.05f;  // "amount" of signal dependency of the noise - make it a user parameter?
	
	public float h;  // parameter influencing the NLMS algorithm: larger h will yield better denoising but more blurring (A small h will give smaller weights to neighborhoods that differ a lot from the neighborhood of the pixel being denoised.)
	public float sigma0; // noise variance in the absence of signal
	public boolean deconvolution;  // if true, use the NLMS-SCD algorithm, otherwise use NLMS-SC.
	public DeconvolutionParams deconvolutionParams;

	public class DeconvolutionParams
	{
		public static final int blurKernelSize = 15;
		public static final float blurKernelSigma = 1.0f;
		
		public static final float lambdaMin = 0.0f;
		public static final float lambdaMax = 5.0f;
		
		public static final int numIterationsMin = 5;
		public static final int numIterationsMax = 30;
		
		public float lambda;   // trade-off denoising versus deconvolution
		public int numIterations;
		
		DeconvolutionParams()
		{
			lambda = 0.3f;
			numIterations = 25;
		}

		public DeconvolutionParams(DeconvolutionParams other)
		{
			this.lambda = other.lambda;
			this.numIterations = other.numIterations;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			DeconvolutionParams other = (DeconvolutionParams)obj;
			
			return (obj instanceof DeconvolutionParams) && (lambda == other.lambda) && (numIterations == other.numIterations);
		}
		
		@Override
		public int hashCode()
		{
			return Float.valueOf(lambda).hashCode() ^  Integer.valueOf(numIterations).hashCode();
		}
	};
	
	public static final float[] emCorrFilterInv = { 0.003548810180648f,
										            0.006457459824059f,
										            0.007150416544695f,
										            0.010395250498662f,
										            0.018758809056068f,
										            0.021360913009926f,
										            0.045297563880590f,
										            0.039260499682212f,
										            0.123410138059489f,
										            0.022063139838911f,
										            0.443138357376189f,
										           -0.479376389377209f,
										            1.955721404909547f,
										           -0.479376389377209f,
										            0.443138357376189f,
										            0.022063139838911f,
										            0.123410138059488f,
										            0.039260499682212f,
										            0.045297563880590f,
										            0.021360913009926f,
										            0.018758809056068f,
										            0.010395250498662f,
										            0.007150416544695f,
										            0.006457459824060f,
										            0.003548810180647f };

	public NonLocalMeansSCDParams()
	{
		h = 40.0f;
		sigma0 = 20.0f;
		deconvolution = false;
		deconvolutionParams = new DeconvolutionParams();
	}
	
	public NonLocalMeansSCDParams(NonLocalMeansSCDParams other)
	{
		this.h = other.h;
		this.sigma0 = other.sigma0;
		this.deconvolution = other.deconvolution;
		this.deconvolutionParams = other.deconvolutionParams;
	}
	
	@Override
	public String toString()
	{
		return deconvolution ? ("h " + h + "; sigma0 " + sigma0 + "; deconvolution: " + deconvolutionParams.numIterations + " iterations" + "; lambda " + deconvolutionParams.lambda) 
			                 : ("h " + h + "; sigma0 " + sigma0 + "; no deconvolution");
	}
	
	@Override
	public boolean equals(Object obj)
	{
		NonLocalMeansSCDParams other = (NonLocalMeansSCDParams)obj;
		
		return (obj instanceof NonLocalMeansSCDParams) && (h == other.h) && (sigma0 == other.sigma0) && (deconvolution == other.deconvolution) && deconvolutionParams.equals(other.deconvolutionParams);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(h).hashCode() ^  Float.valueOf(sigma0).hashCode() ^  Boolean.valueOf(deconvolution).hashCode() ^  deconvolutionParams.hashCode();
	}
}