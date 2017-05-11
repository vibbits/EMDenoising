package be.vib.imagej;

import java.nio.file.Paths;

import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class QuasarTools
{
	public static void loadAlgorithms(String folder, String filename)
	{
		String module = Paths.get(folder, filename).toString();
		
		// FIXME: Qhost.loadSourceModule/loadBinaryModule seems to fail silently if "module" does not exist?
		
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
	
//	// Returns the Quasar function object for the function with the given signature.
//	// If the function does not yet exist in the Quasar host, it will load it from filename (and compile it if needed)
//	public static QFunction loadDenoiseFunction(String filename, String signature) throws NoSuchFileException
//	{
//		String functionName = extractFunctionName(signature);
//		
//		if (!QHost.functionExists(functionName))
//		{
//			Path path = Preferences.getQuasarResourcesPath();
//			String module = Paths.get(path.toString(), filename).toString();
//			
//			// Lazy loading of the source module for this denoising function.
//			// Once it is loaded it will persist in the Quasar host
//			// until the host is released.
//			if (module.endsWith(".q"))
//			{
//				System.out.println("Loading source " + module);
//				QHost.loadSourceModule(module);
//			}
//			else
//			{
//				System.out.println("Loading binary " + module);
//				QHost.loadBinaryModule(module);
//			}
//		}		
//
//		return new QFunction(signature);
//	}

//	// Extracts the function name from a Quasar function signature.
//	// For example, given the signature "gaussian_filter(mat,scalar,int,string)"
//	// it returns "gaussian_filter".
//	private static String extractFunctionName(String signature)
//	{
//		int i = signature.indexOf('(');
//		assert(i != -1);
//		
//		return signature.substring(0, i);
//	}	
	
	public static QValue newCubeFromImage(ImageProcessor image)
	{		
		if (image instanceof ByteProcessor)
		{
			return QUtils.newCubeFromGrayscaleByteArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		}
		else if (image instanceof ShortProcessor)
		{
			return QUtils.newCubeFromGrayscaleShortArray(image.getWidth(), image.getHeight(), (short[])image.getPixels());			
		}
		else
		{
			throw new RuntimeException("Only 8 bit/pixel and 16 bit/pixel grayscale images are supported.");
		}
	}
	
	public static int bitDepth(ImageProcessor image)
	{
		return image.getBitDepth();
	}
	
	public static float bitRange(ImageProcessor image)
	{
		return (1 << bitDepth(image)) - 1;
	}

	public static ImageProcessor newImageFromCube(ImageProcessor image, QValue cube)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		if (image instanceof ByteProcessor)
		{
			byte[] pixels = QUtils.newGrayscaleByteArrayFromCube(width, height, cube);
			return new ByteProcessor(width, height, pixels);
		}
		else if (image instanceof ShortProcessor)
		{
			short[] pixels = QUtils.newGrayscaleShortArrayFromCube(width, height, cube);
			return new ShortProcessor(width, height, pixels, null);
		}
		else
		{
			throw new RuntimeException("Only 8 bit/pixel and 16 bit/pixel grayscale images are supported.");
		}
	}
	
}
