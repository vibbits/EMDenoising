package be.vib.imagej;

import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

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
	
	protected QFunction loadDenoiseFunction(String sourceFile, String function, String signature)
	{
		if (!QHost.functionExists(function))
		{
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this GaussianDenoiser object.
			QHost.loadSourceModule(sourceFile);
		}
		
		assert(QHost.functionExists(function));
		
		return new QFunction(signature);
	}
}