package be.vib.imagej;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.swing.SwingWorker;

import be.vib.bits.QExecutor;

public class DenoisePreviewSwingWorker extends SwingWorker<BufferedImage, Void>
{
	Denoiser denoiser;
	Function<BufferedImage, Void> cacheAndShow;
	
	public DenoisePreviewSwingWorker(Denoiser denoiser, Function<BufferedImage, Void> cacheAndShow) 
	{
		this.denoiser = denoiser;
		this.cacheAndShow = cacheAndShow;
	}
	
	@Override
	public BufferedImage doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		return QExecutor.getInstance().submit(denoiser).get().getBufferedImage(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
	}
	
	@Override
	public void done()
	{
		try
		{
			System.out.println("DenoisePreviewSwingWorker done");
			BufferedImage denoisedPreview = get();
			cacheAndShow.apply(denoisedPreview);
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