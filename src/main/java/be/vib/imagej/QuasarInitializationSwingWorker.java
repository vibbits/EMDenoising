package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.JavaQuasarBridge;
import ij.IJ;

public class QuasarInitializationSwingWorker extends SwingWorker<Void, Void>
{
	private Runnable onSuccess;
	private Runnable onFailure;
	
	public QuasarInitializationSwingWorker(Runnable onSuccess, Runnable onFailure) 
	{
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
	}
	
	@Override
	public Void doInBackground() throws InterruptedException, ExecutionException
	{	
		// Use the QUASAR_PATH environment variable to specify where to find the Quasar runtime.
		// If this variable is set, then Quasar will be started from there.
		// If this variable is not set, then set it to point to the Fiji.app\Quasar folder. A minimal Quasar should have been installed there.
		String quasarPath = JavaQuasarBridge.getQuasarPath();
		System.out.println("Querying: QUASAR_PATH=" + quasarPath);
		if (quasarPath == null)
		{
			JavaQuasarBridge.setQuasarPath(getFijiQuasarPath());
			System.out.println("QUASAR_PATH environment variable was not set, so using " + getFijiQuasarPath());
		}
		else
		{
			System.out.println("Using Quasar pointed to by existing environment variable QUASAR_PATH=" + quasarPath);
		}

		// Start the Quasar host
		JavaQuasarBridge.startQuasar("cuda", false); // throws a RuntimeException on failure - if so it gets wrapped as an ExecutionException and caught in done()	
		
		// Schedule Quasar release for later, when the Java VM shuts down. This is ugly, but
		// there doesn't seem to be any other obvious way to release Quasar "at the very end".
		// (And Quasar can only be initialized and released a single time.)
		JavaQuasarBridge.addQuasarShutdownHook();
		
		// Extract the .qlib file with our denoising Quasar code from our jar file into a temporary folder, and ask Quasar to load it.
		JavaQuasarBridge.extractAndLoadModule("be.vib.imagej.QuasarInitializationSwingWorker", "qlib/vib_denoising_algorithms.qlib", "vib_denoising_algorithms.qlib", "vib_em_denoising_");
		
		return null;
	}
	
	@Override
	public void done()
	{
		try
		{
			get(); // get the result of doInBackground() - Void if it was successful, an ExecutionException if it failed
			onSuccess.run();
		}
		catch (ExecutionException e)
		{
			e.getCause().printStackTrace();
			onFailure.run();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private static String getFijiQuasarPath()
	{
		// TODO: check platform (windows, linux, mac) and architecture (32/64 bit) - for now only 64bit Windows is supported
		return IJ.getDir("imagej") + java.io.File.separator + "Quasar";
	}
}