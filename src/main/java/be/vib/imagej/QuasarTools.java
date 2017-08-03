package be.vib.imagej;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import be.vib.bits.JavaQuasarBridge;
import be.vib.bits.QExecutor;
import be.vib.bits.QHost;
import be.vib.bits.jartools.Jar;

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
			JavaQuasarBridge.loadLibrary(quasarTempFolder);
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
			
			// if (loadCompiler)
			// {
            //   // memleaks profiling needs a host with functional compiler
			//	 QHost.enableProfiling(QHost.ProfilingMode.MEMLEAKS);
			//	 System.out.println("Quasar memory profiling enabled");
			// }
			
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
					e.printStackTrace();
				}				
			}
		});	
		
		quasarStarted = true;
	}

	private static void loadAlgorithms(String folder, String filename)
	{
		String module = Paths.get(folder, filename).toString();
		
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
}
