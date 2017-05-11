package be.vib.imagej;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import be.vib.bits.QHost;
import be.vib.bits.jartools.Jar;

public class QuasarInitializationSwingWorker extends SwingWorker<Void, Void>
{
	private String device;
	private String algorithmsFolder;  // the .qlib (embedded in the plugin jar) with the denoising algorithms will be extracted here
	private boolean loadCompiler;
	private Runnable whenDone;
	
	public QuasarInitializationSwingWorker(String device, String algorithmsFolder, boolean loadCompiler, Runnable whenDone) 
	{
		this.device = device;
		this.algorithmsFolder = algorithmsFolder;
		this.loadCompiler = loadCompiler;
		this.whenDone = whenDone;
	}
	
	@Override
	public Void doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception
	{
		// Initialize Quasar now
		Callable<Void> task = () -> {
			System.out.println("QHost.init device = " + device + " load compiler = " + loadCompiler + " (Java thread=" + Thread.currentThread().getId() + ")");
			QHost.init(device, loadCompiler);
			
			// QHost.enableProfiling();
			// System.out.println("Quasar memory profiling enabled");
			
			System.out.println("Extracting algorithms");
			Jar.extractResource(algorithmsFolder, "qlib/vib_denoising_algorithms.qlib");
//			Jar.extractResource(algorithmsFolder, "qlib/vib_denoising_algorithms.q");
//			Jar.extractResource(algorithmsFolder, "qlib/blsgsm.q");
//			Jar.extractResource(algorithmsFolder, "qlib/estimate_noise.q");
//			Jar.extractResource(algorithmsFolder, "qlib/power_of_two_extension.q");
//			Jar.extractResource(algorithmsFolder, "qlib/gaussian_filter.q");
//			Jar.extractResource(algorithmsFolder, "qlib/nlmeans.q");
//			Jar.extractResource(algorithmsFolder, "qlib/anisotropic_diffusion.q");
//			Jar.extractResource(algorithmsFolder, "qlib/bilateral_filter.q");

			System.out.println("Loading algorithms");
			QuasarTools.loadAlgorithms(algorithmsFolder, "vib_denoising_algorithms.qlib");
//			QuasarTools.loadAlgorithms(algorithmsFolder, "vib_denoising_algorithms.q");
			return null;
		};
		
		QExecutor.getInstance().submit(task).get();
		
		// Schedule Quasar release for later, when the Java VM shuts down. This is ugly, but
		// there doesn't seem to be any other obvious way to release Quasar "at the very end".
		// (And Quasar can only be initialized and released a single time.)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				Callable<Void> task = () -> {
					System.out.println("Calling QHost.release()" + " (Java thread=" + Thread.currentThread().getId() + ")");
					QHost.release();
					System.out.println("Quasar host released");					
					return null;
				};
				
				try {
					QExecutor.getInstance().submit(task).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
				
		return null;
	}
	
	@Override
	public void done()
	{
		whenDone.run();
	}
}