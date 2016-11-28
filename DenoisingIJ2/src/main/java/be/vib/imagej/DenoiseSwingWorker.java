package be.vib.imagej;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;

class DenoiseSwingWorker extends SwingWorker<byte[], Void>
{
	Denoiser denoiser;
	
	public DenoiseSwingWorker(Denoiser denoiser)
	{
		this.denoiser = denoiser;
	}
	
	@Override
	public byte[] doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		Instant startTime = Instant.now();

		byte[] outputPixels = QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.

		Instant endTime = Instant.now();
		long durationMs = Duration.between(startTime, endTime).toMillis();
		long numPixels = denoiser.image.width * denoiser.image.height;
		System.out.println("Slice denoising time: " + durationMs + " ms " +
		         "(" + (double)numPixels / (double)durationMs + " kpix/s)");

		return outputPixels;
	}
	
	@Override
	public void done()
	{
		try
		{
			System.out.println("DenoiseSwingWorker done()");
			
			final byte[] outputPixels = get();
			
			ByteProcessor denoisedImage = new ByteProcessor(denoiser.image.width, denoiser.image.height, outputPixels);
						
			ImageStack denoisedStack = new ImageStack(denoisedImage.getWidth(), denoisedImage.getHeight());
			denoisedStack.addSlice("", denoisedImage);
			
			ImagePlus denoisedImagePlus = new ImagePlus("Denoised image", denoisedStack); // TODO: improve title - add original noisy image name
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
