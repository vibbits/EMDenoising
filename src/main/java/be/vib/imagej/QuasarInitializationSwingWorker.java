package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import be.vib.bits.QHost;

class QuasarInitializationSwingWorker extends SwingWorker<Void, Void>
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
	public Void doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		QExecutor.getInstance().submit(() -> {
			System.out.println("QHost.init device = " + device + " load compiler = " + loadCompiler);
			QHost.init(device, loadCompiler);
			System.out.println("QHost.init done");
		}).get();
		
		return null;
	}
	
	@Override
	public void done()
	{
		whenDone.run();
	}
}