package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public class QuasarInitializationSwingWorker extends SwingWorker<Void, Void>
{
	private String device;
	private Runnable onSuccess;
	private Runnable onFailure;
	
	public QuasarInitializationSwingWorker(String device, Runnable onSuccess, Runnable onFailure) 
	{
		this.device = device;
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
	}
	
	@Override
	public Void doInBackground() throws InterruptedException, ExecutionException
	{	
		boolean loadCompiler = false;
		QuasarTools.startQuasar(device, loadCompiler); // throws a RuntimeException on failure - if so it gets wrapped as an ExecutionException and caught in done()
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
}