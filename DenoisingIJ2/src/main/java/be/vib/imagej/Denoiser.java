package be.vib.imagej;

import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;

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

	// Returns the Quasar function with the given signature. If the function
	// does not exist yet in the Quasar host, it will load it from sourceFile.
	protected QFunction loadDenoiseFunction(String sourceFile, String signature)
	{
		String function = extractFunction(signature);
		
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

	// Extracts the function name from a Quasar function signature.
	// For example, given the signature "gaussian_filter(mat,scalar,int,string)"
	// it returns "gaussian_filter".
	private String extractFunction(String signature)
	{
		int i = signature.indexOf('(');
		assert(i != -1);
		
		return signature.substring(0, i);
	}	
}