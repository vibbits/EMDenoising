package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

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
		QuasarTools.startQuasar(device, algorithmsFolder, loadCompiler);			
		return null;
	}
	
	@Override
	public void done()
	{
		whenDone.run();
	}
}