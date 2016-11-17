package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.process.ByteProcessor;

class DenoiseSwingWorker extends SwingWorker<byte[], Void>
{
	Denoiser denoiser;
	WizardModel model;
	ImagePanel imagePanel;
	
	public DenoiseSwingWorker(Denoiser denoiser, WizardModel model, ImagePanel imagePanel)
	{
		this.denoiser = denoiser;
		this.model = model;
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
			
			model.previewDenoisedROI = new ByteProcessor(denoiser.image.width, denoiser.image.height, outputPixels);
			
			imagePanel.setImage(model.previewDenoisedROI.getBufferedImage(), WizardPageDenoisingAlgorithm.maxPreviewSize);
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