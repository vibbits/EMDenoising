package be.vib.imagej;

import java.util.concurrent.Callable;

class Denoiser implements Callable<byte[]>
{
	public final LinearImage image; // original, noisy source image
	
	Denoiser(LinearImage image)
	{
		this.image = image;
	}

	// Important: *must* be run on the Quasar thread!
	// Returns a new array with the denoised version of image.pixels.
	// Its width and height must be the same as in the original image.
	@Override
	public byte[] call() throws Exception
	{
		return null;
	}
}