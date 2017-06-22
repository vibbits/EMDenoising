package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class QuasarInitializationSwingWorker extends SwingWorker<Void, Void>
{
	private String device;
	private boolean loadCompiler;
	private Runnable whenDone;
	
	public QuasarInitializationSwingWorker(String device, boolean loadCompiler, Runnable whenDone) 
	{
		this.device = device;
		this.loadCompiler = loadCompiler;
		this.whenDone = whenDone;
	}
	
	@Override
	public Void doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception
	{	
		QuasarTools.startQuasar(device, loadCompiler);			
		return null;
	}
	
	@Override
	public void done()
	{
		whenDone.run();
	}
}