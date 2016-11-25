package be.vib.imagej;

public class NonLocalMeansSCDParams
{
	public static final float hMin = 0.01f;
	public static final float hMax = 50.0f;
	
	public static final float lambdaMin = 0.01f;  // FIXME
	public static final float lambdaMax = 50.0f;  // FIXME
	
	public static final float sigma0Min = 0.01f;  // FIXME
	public static final float sigma0Max = 100.0f;  // FIXME
	
	public static final int numIterationsMin = 1;  // FIXME
	public static final int numIterationsMax = 100;  // FIXME
	
	public float h;
	public float sigma0;
	public float lambda;   // trade-off denoising versus deconvolution
	public int numIterations;
	
	public static final int blurKernelSize = 15;
	public static final float blurKernelSigma = 1.0f;
	
	public static final int halfSearchSize = 5;
	public static final int halfBlockSize = 4;
	public static final float alpha = 0.05f;
	
	public static final float[] emCorrFilterInv = { 
			0.003548810180648f,
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
		h = 13.5f;
		lambda = 0.3f;
		numIterations = 25;
		sigma0 = 20.0f;
	}
	
	public NonLocalMeansSCDParams(NonLocalMeansSCDParams other)
	{
		this.h = other.h;
		this.lambda = other.lambda;
		this.numIterations = other.numIterations;
		this.sigma0 = other.sigma0;
	}
}