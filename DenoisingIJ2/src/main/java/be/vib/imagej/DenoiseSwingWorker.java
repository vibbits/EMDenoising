package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

class DenoiseSwingWorker extends SwingWorker<byte[], Void>
{
	Denoiser denoiser;
	ImagePanel imagePanel;
	ImageProcessor preview;
	
	public DenoiseSwingWorker(Denoiser denoiser, ImageProcessor preview, ImagePanel imagePanel)
	{
		this.denoiser = denoiser;
		this.preview = preview;
		this.imagePanel = imagePanel;
	}
	
	@Override
	public byte[] doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		byte[] outputPixels = QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
		return outputPixels;
	}
	
	@Override
	public void done()
	{
		try
		{
			System.out.println("DenoiseSwingWorker done()");
			
			final byte[] outputPixels = get();
			
			preview = new ByteProcessor(denoiser.image.width, denoiser.image.height, outputPixels);
			
			imagePanel.setImage(preview.getBufferedImage(), WizardPageDenoisingAlgorithm.maxPreviewSize);
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