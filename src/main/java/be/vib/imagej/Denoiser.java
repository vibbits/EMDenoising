package be.vib.imagej;

import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;

public class Denoiser implements Callable<LinearImage>
{
	protected LinearImage image; // original, noisy source image
	
	public Denoiser()
	{
		this.image = null;
	}
	
	public void setImage(LinearImage image)
	{
		this.image = image;
	}

	// Important: call() *must* be run on the Quasar thread!
	// Returns a denoised version of the original image.
	// Its width and height will be the same as in the original image.
	@Override
	public LinearImage call() throws Exception
	{
		return null;
	}

	// Returns the Quasar function object for the function with the given signature.
	// If the function does not yet exist in the Quasar host, it will load it from sourceFile and compile it.
	protected QFunction loadDenoiseFunction(String sourceFile, String signature)
	{
		String functionName = extractFunctionName(signature);

		// TODO: important: support loading from JAR or so
		// TODO: important: load .qlib instead of .q (needed for installation on machine without Quasar license).
		
		if (!QHost.functionExists(functionName))
		{
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this GaussianDenoiser object.
			if (sourceFile.endsWith(".q"))
			{
				System.out.println("Loading source " + sourceFile);
				QHost.loadSourceModule(sourceFile);
			}
			else
			{
				System.out.println("Loading binary " + sourceFile);
				QHost.loadBinaryModule(sourceFile);
			}
		}		

		
		return new QFunction(signature);
	}

	// Extracts the function name from a Quasar function signature.
	// For example, given the signature "gaussian_filter(mat,scalar,int,string)"
	// it returns "gaussian_filter".
	private String extractFunctionName(String signature)
	{
		int i = signature.indexOf('(');
		assert(i != -1);
		
		return signature.substring(0, i);
	}	
}