package be.vib.imagej;

public class NonLocalMeansSCDParams
{
	public static final float hMin = 0.01f;
	public static final float hMax = 75.0f;
	
	public static final float lambdaMin = 0.01f;
	public static final float lambdaMax = 50.0f;
	
	public static final float sigma0Min = 0.01f;
	public static final float sigma0Max = 100.0f;
	
	public static final int numIterationsMin = 1;
	public static final int numIterationsMax = 100;
	
	public float h;
	public float sigma0;
	public boolean deconvolution;  // if true, use the NLMS-SCD algorithm, otherwise use NLMS-SC.
	public float lambda;   // trade-off denoising versus deconvolution  (this parameter is only used if deconvolution == true)
	public int numIterations; // (this parameter is only used if deconvolution == true)
	
	public static final int blurKernelSize = 15;  // (this parameter is only used if deconvolution == true)
	public static final float blurKernelSigma = 1.0f;  // (this parameter is only used if deconvolution == true)
	
	// TODO: move the deconvolution-specific parameters into a little class of its own?
	
	public static final int halfSearchSize = 5;
	public static final int halfBlockSize = 4;
	public static final float alpha = 0.05f;
	
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
		// parameters always offered in the UI
		h = 40.0f;
		sigma0 = 20.0f;
		deconvolution = false;
		
		// additional parameters, only offered when deconvolution = true
		lambda = 0.3f;
		numIterations = 25;
	}
	
	public NonLocalMeansSCDParams(NonLocalMeansSCDParams other)
	{
		this.h = other.h;
		this.sigma0 = other.sigma0;
		this.deconvolution = other.deconvolution;
		this.lambda = other.lambda;
		this.numIterations = other.numIterations;
	}
	
	@Override
	public String toString()
	{
		return deconvolution ? ("h " + h + "; sigma0 " + sigma0 + "; deconvolution: " + numIterations + " iterations" + "; lambda " + lambda) 
			                 : ("h " + h + "; sigma0 " + sigma0 + "; no deconvolution");
	}
}