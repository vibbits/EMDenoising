package be.vib.imagej;

public class NonLocalMeansSCDParams
{
	public static final float hMin = 0.01f;
	public static final float hMax = 75.0f;
	
	public static final float sigma0Min = 0.01f;
	public static final float sigma0Max = 100.0f;
	
	public static final int halfSearchSize = 5;
	public static final int halfBlockSize = 4;
	public static final float alpha = 0.05f;
	
	public float h;
	public float sigma0;
	public boolean deconvolution;  // if true, use the NLMS-SCD algorithm, otherwise use NLMS-SC.
	public DeconvolutionParams deconvolutionParams;

	public class DeconvolutionParams
	{
		public static final int blurKernelSize = 15;
		public static final float blurKernelSigma = 1.0f;
		
		public static final float lambdaMin = 0.01f;
		public static final float lambdaMax = 50.0f;
		
		public static final int numIterationsMin = 1;
		public static final int numIterationsMax = 100;
		
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
}