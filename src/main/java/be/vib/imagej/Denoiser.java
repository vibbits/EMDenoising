package be.vib.imagej;

import java.util.concurrent.Callable;

import ij.process.ImageProcessor;

public class Denoiser implements Callable<ImageProcessor>
{
	protected ImageProcessor image; // original, noisy source image
	
	public Denoiser()
	{
		this.image = null;
	}
	
	public void setImage(ImageProcessor image)
	{
		this.image = image;
	}

	// Important: call() *must* be run on the Quasar thread!
	// Returns a denoised version of the original image.
	// Its width and height will be the same as in the original image.
	@Override
	public ImageProcessor call() throws Exception
	{
		return null;
	}
}