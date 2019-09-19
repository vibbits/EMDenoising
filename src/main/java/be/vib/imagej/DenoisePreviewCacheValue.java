package be.vib.imagej;

import java.awt.image.BufferedImage;

// Class representing a "value" in our cache with denoising previews:
// the denoised image and an estimate of its noise and blur levels.
public class DenoisePreviewCacheValue
{
	public BufferedImage denoisedPreview;
	public float noiseEstimate;
	public float blurEstimate;
	
	public DenoisePreviewCacheValue(BufferedImage denoisedPreview, float noiseEstimate, float blurEstimate)
	{
		this.denoisedPreview = denoisedPreview;
		this.noiseEstimate = noiseEstimate;
		this.blurEstimate = blurEstimate;
	}
}
