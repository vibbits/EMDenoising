package be.vib.imagej;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

class DenoiseSwingWorker extends SwingWorker<ImagePlus, Void>
{
	Denoiser denoiser;
	ImagePlus noisyImagePlus;
	ImageRange range;
	
	public DenoiseSwingWorker(Denoiser denoiser, ImagePlus noisyImagePlus, ImageRange range)
	{
		this.denoiser = denoiser;
		this.noisyImagePlus = noisyImagePlus;
		this.range = range;
	}
	
	@Override
	public ImagePlus doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
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
		}
		
		ImagePlus denoisedImagePlus = new ImagePlus("Denoised image", denoisedStack); // TODO: improve title - add original noisy image name
		return denoisedImagePlus;
	}
	
//	@Override
//	protected void process(List<VVVVV> chunks)  // gets called asynchronously on the Java EDT
//	{
//		   
//	}
	
	@Override
	public void done()
	{
		try
		{
			System.out.println("DenoiseSwingWorker done()");
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
