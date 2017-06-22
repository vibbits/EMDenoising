package be.vib.imagej;

import java.util.concurrent.Callable;

import ij.process.ImageProcessor;

public abstract class Denoiser implements Callable<ImageProcessor>
{
	protected ImageProcessor image; // original, noisy source image
	protected DenoiseParams params;
	
	public Denoiser(DenoiseParams params)
	{
		this.image = null;
		this.params = params;
	}
	
	public DenoiseParams getParams()
	{
		return params;
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
	
	public int imageTileSize()
	{
		return 1024;
	}
	
	public int imageMargin()
	{
		 // FIXME: Must be dependent on algorithm parameters to avoid artifacts along tile boundaries.
		//         For example, in case of the GaussianDenoiser with sigma=30 tile boundaries are visible.
		return 16;
	}
}