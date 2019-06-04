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
		// Bigger tiles lead to significant performance improvements on the Quasar side,
		// but this needs to be balanced against graphics card memory constraints.
		// For each pixel in a tile some denoising algorithms allocate a significant
		// amount of memory on the Quasar side.
		// We may want to make the tile size dependent on amount of free graphics memory
		// as well as on the memory requirements of the particular denoising algorithm.
		return 1024;
	}
	
	public int imageMargin()
	{
		// IMPROVEME: The margin must be dependent on algorithm parameters to avoid artifacts along tile boundaries.
		// For normal parameter values however, test shows that a 16 pixel margin amply suffices, but it is in principle 
		// possible to manually enter large parameter values in the user interface that might trigger tiling artifacts.
		// (In practice this is not likely to happen because these large values would imply blurring
		// so large as to make the denoised image useless.)
		return 16;
	}
}