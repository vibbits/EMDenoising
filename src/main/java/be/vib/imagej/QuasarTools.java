package be.vib.imagej;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import be.vib.bits.JavaQuasarBridge;
import be.vib.bits.QExecutor;
import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import be.vib.bits.jartools.Jar;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class QuasarTools
{
    private static String quasarTempFolder; // the .qlib (embedded in the plugin jar) with the denoising algorithms will be extracted here
    private static boolean quasarStarted = false;

	static
	{
		try
		{
			System.out.println("About to load JavaQuasarBridge dynamic library");
			quasarTempFolder = Files.createTempDirectory("vib_em_denoising_").toString();
			boolean useEmbeddedQuasar = false;  // TODO: should be true once we have the Quasar runtime distributions and embed them in the JAR
			JavaQuasarBridge.loadLibrary(quasarTempFolder, useEmbeddedQuasar);
			System.out.println("JavaQuasarBridge dynamic library loaded.");
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void startQuasar(String device, boolean loadCompiler) throws InterruptedException, ExecutionException
	{
		// FIXME: this naive flag avoids repeated initialization from the same thread, but we still need to fix
		// concurrent initialization from different threads (e.g. via the ImageJ plugin menu and via the an ImageJ script)
		if (quasarStarted)
			return;
		
		Callable<Void> task = () -> {
			System.out.println("QHost.init(device=" + device + ", loadcompiler=" + loadCompiler + ")");
			QHost.init(device, loadCompiler);
			
			QHost.printMachineInfo();
			
			// QHost.enableProfiling();
			// System.out.println("Quasar memory profiling enabled");
			
			System.out.println("Extracting algorithms");
			Jar.extractResource(quasarTempFolder, "qlib/vib_denoising_algorithms.qlib");

			System.out.println("Loading algorithms");
			QuasarTools.loadAlgorithms(quasarTempFolder, "vib_denoising_algorithms.qlib");
			return null;
		};
		
		// Initialize Quasar now and wait for it to complete.
		QExecutor.getInstance().submit(task).get();
		
		// Schedule Quasar release for later, when the Java VM shuts down. This is ugly, but
		// there doesn't seem to be any other obvious way to release Quasar "at the very end".
		// (And Quasar can only be initialized and released a single time.)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				Callable<Void> task = () -> {
					System.out.println("QHost.release()");
					QHost.release();
					System.out.println("Quasar host released");					
					return null;
				};
				
				try
				{
					QExecutor.getInstance().submit(task).get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					// CHECKME
					e.printStackTrace();
				}				
			}
		});	
		
		quasarStarted = true;
	}

	private static void loadAlgorithms(String folder, String filename)
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
	
	
	// TODO: move code below to a different Java class.
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
