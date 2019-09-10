package be.vib.imagej;

import java.awt.image.BufferedImage;

// Class representing a "value" in our cache with denoising previews:
// the denoised image and an estimate of its amount of blur.
public class DenoisePreviewCacheValue
{
	public BufferedImage denoisedPreview;
	public float blurEstimate;
	
	public DenoisePreviewCacheValue(BufferedImage denoisedPreview, float blurEstimate)
	{
		this.denoisedPreview = denoisedPreview;
		this.blurEstimate = blurEstimate;
	}
}
