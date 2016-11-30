package be.vib.imagej;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

class DenoiseSwingWorker extends SwingWorker<ImagePlus, Integer>
{
	private Denoiser denoiser;
	private ImagePlus noisyImagePlus;
	private ImageRange range;
	private JProgressBar progressBar;
	
	public DenoiseSwingWorker(Denoiser denoiser, ImagePlus noisyImagePlus, ImageRange range, JProgressBar progressBar)
	{
		this.denoiser = denoiser;
		this.noisyImagePlus = noisyImagePlus;
		this.range = range;
		this.progressBar = progressBar;
	}
	
	@Override
	public ImagePlus doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		// doInBackground is run is a thread different from the Java Event Dispatch Thread (EDT)
		final int width = noisyImagePlus.getWidth();
		final int height = noisyImagePlus.getHeight();
		
		final ImageStack noisyStack = noisyImagePlus.getStack();
				
		ImageStack denoisedStack = new ImageStack(width, height);
		
		for (int slice = range.getFirst(); slice <= range.getLast(); slice++)
		{
			ImageProcessor imageProcessor = noisyStack.getProcessor(slice);
			LinearImage image = new LinearImage(width, height, WizardModel.getPixelsCopy(imageProcessor));
			
			denoiser.setImage(image);
			byte[] outputPixels = QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
			
			ByteProcessor denoisedImage = new ByteProcessor(width, height, outputPixels);
			denoisedStack.addSlice("", denoisedImage);
			
			publish(slice);
		}
		
		String title = noisyImagePlus.getTitle() + " [denoised]";
		ImagePlus denoisedImagePlus = new ImagePlus(title, denoisedStack);
		return denoisedImagePlus;
	}
	
	@Override
	protected void process(List<Integer> chunks)  // gets called asynchronously on the Java EDT, update the UI here
	{
		for (Integer slice : chunks)
		{
			progressBar.setValue(slice);
		}
	}
	
	@Override
	public void done()  // executed on the EDT, update UI here.
	{
		System.out.println("DenoiseSwingWorker done()");
		try
		{
			ImagePlus denoisedImagePlus = get();
			denoisedImagePlus.show();
			
			// TODO: we also need to update the rest of the GUI (like status strings etc) - how to do this?
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
