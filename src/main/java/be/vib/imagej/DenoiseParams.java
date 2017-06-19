package be.vib.imagej;

import java.util.Properties;

public abstract class DenoiseParams
{
	// Prefix for the key strings used when storing
	// the denoising parameters as properties meta-data in the image.
	protected static final String PREFIX = "be.vib.emdenoising.";

	// The denoising parameters represented as "properties".
	// Together they precisely specify the denoising algorithm and its parameters.
    public abstract Properties getParameterList();

	// Set default algorithm parameters based on the image noise estimate 'noiseEstimate'.
    // noiseEstimate is an estimate for the standard deviation of the noise in the image,
    // assuming the image pixel intensities are in the range [0, 1].
	public abstract void setDefaultParameters(float noiseEstimate);
}
