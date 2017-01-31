package be.vib.imagej;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;
import ij.process.ImageProcessor;

public class DenoisePreviewSwingWorker extends SwingWorker<ImageProcessor, Void>
{
	Denoiser denoiser;
	ImagePanel imagePanel;
	ImageProcessor denoisedPreview;
	
	// - denoiser knows the input image that needs to be denoised
	// - we will set denoisedPreview to the denoised result
	public DenoisePreviewSwingWorker(Denoiser denoiser, ImageProcessor denoisedPreview, ImagePanel imagePanel) 
	{
		this.denoiser = denoiser;
		this.denoisedPreview = denoisedPreview;
		this.imagePanel = imagePanel;
	}
	
	@Override
	public ImageProcessor doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		return QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
	}
	
	@Override
	public void done()
	{
		try
		{
			System.out.println("DenoisePreviewSwingWorker done()");
			
			final ImageProcessor denoisedPreview = get();
						
			imagePanel.setImage(denoisedPreview.getBufferedImage());
//			imagePanel.setText(null);
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