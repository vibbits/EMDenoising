package be.vib.imagej;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import ij.ImagePlus;

// The DenoiseSwingWorker class is a wrapper around the DenoiseEngine class.
// Its main task is to provide denoising progress feedback to the
// user interface, and to allow the user to cancel the denoising calculations.
public class DenoiseSwingWorker extends SwingWorker<ImagePlus, Integer>
{
	private Algorithm algorithm;
	private ImagePlus noisyImagePlus;
	private ImageRange range;
	private JProgressBar progressBar;
	private Runnable whenDone;  // Will be run on the EDT as soon as the DenoiseSwingWorker is done denoising. Can be used to indicate in the UI that we are done.
	
	private class SwingDenoiseEngine extends DenoiseEngine
	{
		SwingDenoiseEngine(Algorithm algorithm)
		{
			super(algorithm.getDenoiserCopy());
		}
		
		@Override 
		public void publish(Integer... chunks )
		{
			DenoiseSwingWorker.this.publish(chunks);
		}
		
		@Override 
		public void process(List<Integer> chunks)
		{
			DenoiseSwingWorker.this.process(chunks);
		}
		
		@Override 
		public boolean isCancelled()
		{
			return DenoiseSwingWorker.this.isCancelled();
		}
	}
	
	public DenoiseSwingWorker(Algorithm algorithm, ImagePlus noisyImagePlus, ImageRange range, JProgressBar progressBar, Runnable whenDone)
	{
		this.algorithm = algorithm;
		this.noisyImagePlus = noisyImagePlus;
		this.range = range;
		this.progressBar = progressBar;
		this.whenDone = whenDone;
	}
	
	@Override
	public ImagePlus doInBackground()
	{
		// The method doInBackground is run is a thread different from the Java Event Dispatch Thread (EDT).
		// Do not update Java Swing components here.
		
		DenoiseEngine engine = new SwingDenoiseEngine(algorithm);

		String title = noisyImagePlus.getTitle() + " ["+ algorithm.getReadableName() + "]";
		return engine.denoise(noisyImagePlus, range, title);
	}
	
	@Override
	protected void process(List<Integer> chunks)
	{
		// Method process() is executed on the Java EDT, so we can update the UI here.
		
		for (Integer slice : chunks)
		{
			progressBar.setValue(slice);
		}
	}
	
	@Override
	public void done()  
	{
		// Method done() is executed on the Java EDT, we can update the UI here.
		
		whenDone.run();
		
		if (isCancelled())
			return;
		
		// Display the denoised image.
		try
		{
			ImagePlus denoisedImagePlus = get();
			denoisedImagePlus.show();
		}
		catch (ExecutionException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}
