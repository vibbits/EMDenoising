package be.vib.imagej;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;
import ij.process.ByteProcessor;

public class Denoiser implements Callable<ByteProcessor>
{
	protected ByteProcessor image; // original, noisy source image
	
	public Denoiser()
	{
		this.image = null;
	}
	
	public void setImage(ByteProcessor image)
	{
		this.image = image;
	}

	// Important: call() *must* be run on the Quasar thread!
	// Returns a denoised version of the original image.
	// Its width and height will be the same as in the original image.
	@Override
	public ByteProcessor call() throws Exception
	{
		return null;
	}

	// Returns the Quasar function object for the function with the given signature.
	// If the function does not yet exist in the Quasar host, it will load it from filename (and compile it if needed)
	protected QFunction loadDenoiseFunction(String filename, String signature) throws NoSuchFileException
	{
		String functionName = extractFunctionName(signature);

		// TODO: important: support loading from JAR or so
		// TODO: important: load .qlib instead of .q (needed for installation on machine without Quasar license).
		
		if (!QHost.functionExists(functionName))
		{
			Path path = Preferences.getQuasarResourcesPath();
			String module = Paths.get(path.toString(), filename).toString();
			
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this GaussianDenoiser object.
			if (module.endsWith(".q"))
			{
				System.out.println("Loading source " + module);
				QHost.loadSourceModule(module);
			}
			else
			{
				System.out.println("Loading binary " + module);
				QHost.loadBinaryModule(module);
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