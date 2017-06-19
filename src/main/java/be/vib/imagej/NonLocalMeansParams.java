package be.vib.imagej;

import java.util.Properties;

public class NonLocalMeansParams extends DenoiseParams
{
	public static final int halfSearchSizeMin = 1;
	public static final int halfSearchSizeMax = 20;
	
	public static final int halfBlockSizeMin = 1;
	public static final int halfBlockSizeMax = 10;
	
	public float h;  // parameter influencing the NLMS algorithm: larger h will yield better denoising but more blurring (A small h will give smaller weights to neighborhoods that differ a lot from the neighborhood of the pixel being denoised.)

	public float hMin;
	public float hMax;
	
	public int halfBlockSize;
	public int halfSearchSize;
	
	public boolean decorrelation; // if true, do decorrelation with decorrelationParams (signal dependency of noise is not taken into account)
	public boolean deconvolution; // if true, do deconvolution with deconvolutionParams

	public DeconvolutionParams deconvolutionParams;
	
	// Fixed correlation filter. However in theory it depends on dwell time - better would be to estimate it from the image.
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
			numIterations = 20;
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

	public NonLocalMeansParams()
	{
		h = 2.0f;
		hMin = 0.001f;
		hMax = 3.0f;
		halfBlockSize = 4;
		halfSearchSize = 5;
		decorrelation = false;
		deconvolution = false;
		deconvolutionParams = new DeconvolutionParams();
	}
	
	public NonLocalMeansParams(NonLocalMeansParams other)
	{
		this.h = other.h;
		this.hMin = other.hMin;
		this.hMax = other.hMax;		
		this.halfBlockSize = other.halfBlockSize;
		this.halfSearchSize = other.halfSearchSize;
		this.decorrelation = other.decorrelation;
		this.deconvolution = other.deconvolution;
		this.deconvolutionParams = other.deconvolutionParams;
	}
	
	@Override
    public Properties getParameterList()
    {
    	Properties props = new Properties();
    	props.setProperty(PREFIX + "algorithm", "nonlocalmeans");
    	props.setProperty(PREFIX + "nonlocalmeans.h", Float.toString(h));
    	props.setProperty(PREFIX + "nonlocalmeans.halfblocksize", Integer.toString(halfBlockSize));
    	props.setProperty(PREFIX + "nonlocalmeans.halfsearchsize", Integer.toString(halfSearchSize));

    	props.setProperty(PREFIX + "nonlocalmeans.decorrelation", Boolean.toString(decorrelation));
    	
    	props.setProperty(PREFIX + "nonlocalmeans.deconvolution", Boolean.toString(deconvolution));
    	if (deconvolution)
    	{
        	props.setProperty(PREFIX + "nonlocalmeans.deconvolution.lambda", Float.toString(deconvolutionParams.lambda));
        	props.setProperty(PREFIX + "nonlocalmeans.deconvolution.numiterations", Integer.toString(deconvolutionParams.numIterations));
    	}

    	return props;
    }

	@Override
	public String toString()
	{
		String s = "h " + h + "; half block size " + halfBlockSize + "; half search size " + halfSearchSize;
		s = s + (decorrelation ? "; decorrelation" : "; no decorrelation");
		s = s + (deconvolution ? "; deconvolution: " + deconvolutionParams.numIterations + " iterations" + ", lambda " + deconvolutionParams.lambda : "; no deconvolution");
		return s;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		NonLocalMeansParams other = (NonLocalMeansParams)obj;
		
		return (obj instanceof NonLocalMeansParams) && (h == other.h) && (hMin == other.hMin) && (hMax == other.hMax) 
				                                    && (halfBlockSize == other.halfBlockSize) && (halfSearchSize == other.halfSearchSize)
				                                    && (decorrelation == other.decorrelation)
				                                    && (deconvolution == other.deconvolution) && deconvolutionParams.equals(other.deconvolutionParams);
	}
	
	@Override
	public int hashCode()
	{
		return Float.valueOf(h).hashCode() ^ Float.valueOf(hMin).hashCode() ^ Float.valueOf(hMax).hashCode() 
				                           ^ Integer.valueOf(halfBlockSize).hashCode() ^ Integer.valueOf(halfSearchSize).hashCode()
				                           ^ Boolean.valueOf(decorrelation).hashCode()
				                           ^ Boolean.valueOf(deconvolution).hashCode() ^ deconvolutionParams.hashCode();
	}
	@Override
	public void setDefaultParameters(float noiseEstimate)
	{
		// ? IMPROVEME ?
		//
		// Suggest "ideal" denoising parameters.
		//
		// The problem here is that the "ideal" parameters depend on whether or not to do decorrelation and deconvolution.
		// But we get here before the user specified that in the user interface...
		// For now we assume no decorrelation and no deconvolution, but that will probably not yield the best denoised result. :-/
		
		decorrelation = false;
		deconvolution = false;
		halfSearchSize = 5; 
		halfBlockSize = 4;
	    h = 3.14044952392578f * noiseEstimate * noiseEstimate + 1.86890029907227f * noiseEstimate + 0.0483760833740234f;
	    
	    // FIXME - manual optimization
	    h /= 10.0f;
		
		// Heuristic for useful range
		hMin = 0.0001f;
		hMax = h * 2.0f;

		System.out.println("NonLocalMeansParams.setDefaultParams noiseEstimate=" + noiseEstimate + " -> h=" + h + " ["+ hMin + ", " + hMax + "]");
	}
	
	/*
    half_search_size = 5
    half_block_size = 4
    
    h = 3.14044952392578*sigma_est^2 + 1.86890029907227*sigma_est + 0.0483760833740234
    lambda = 0.6 % automatic lambda estimation suboptimal, manual choice
    h_c = 1.79779577255249*sigma_est + 0.0488053560256958
    lambda_c = 0.01 % automatic lambda estimation suboptimal, manual choice
    num_iter = 20
    
    % non-local denoising
    img_den = denoise_nlmeans(img_noisy, half_search_size, half_block_size, h)

    % non-local denoising (correlated)
    img_den_c = denoise_nlmeans_c(img_noisy, half_search_size, half_block_size, h_c, em_corr_filter_inv)

    % non-local deconvolution
    img_dec = deconv_nlmeans(img_noisy,blur_kernel,lambda,num_iter,half_search_size,half_block_size,h)

    % non-local deconvolution (correlated)
    img_dec_c = deconv_nlmeans_c(img_noisy,blur_kernel,lambda_c,num_iter,half_search_size,half_block_size,h_c,em_corr_filter_inv)
    */

}