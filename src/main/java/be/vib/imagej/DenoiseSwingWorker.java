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
	private String algorithmName;
	private JProgressBar progressBar;
	private Runnable whenDone;  // Will be run on the EDT as soon as the DenoiseSwingWorker is done denoising. Can be used to indicate in the UI that we are done.
	
	public DenoiseSwingWorker(Denoiser denoiser, ImagePlus noisyImagePlus, ImageRange range, String algorithmName, JProgressBar progressBar, Runnable whenDone)
	{
		this.denoiser = denoiser;
		this.noisyImagePlus = noisyImagePlus;
		this.range = range;
		this.algorithmName = algorithmName;
		this.progressBar = progressBar;
		this.whenDone = whenDone;
	}
	
	@Override
	public ImagePlus doInBackground() throws InterruptedException, ExecutionException  // TODO: check what happens with exception - should we handle it ourselves here?
	{
		// doInBackground is run is a thread different from the Java Event Dispatch Thread (EDT). Do not update Java Swing components here.
		final int width = noisyImagePlus.getWidth();
		final int height = noisyImagePlus.getHeight();
		
		final int tileWidth = 1024;  // TODO: make this dependent on e.g. algorithm and its parameters
		final int tileHeight = 1024;
		final int margin = 16; // FIXME: must be dependent on algorithm parameters to avoid artifacts along tile boundaries
		
		final ImageStack noisyStack = noisyImagePlus.getStack();
				
		ImageStack denoisedStack = new ImageStack(width, height);
		
		for (int slice = range.getFirst(); slice <= range.getLast(); slice++)
		{
			ImageProcessor noisyImage = noisyStack.getProcessor(slice);
			ByteProcessor denoisedImage = new ByteProcessor(width, height); // blank image, will be filled below
			
			ImageTiler tiler = new ImageTiler(noisyImage, tileWidth, tileHeight, margin);
			for (ImageTile tile : tiler)
			{
				// Get a noisy tile from the original image
				ByteProcessor noisyTileImp = (ByteProcessor)tile.getImageWithMargins();
				
				// Denoise the tile
				denoiser.setImage(noisyTileImp);
				ImageProcessor denoisedTileImp = QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
								
				// Remove tile margins
				denoisedTileImp.setRoi(tile.getLeftMargin(), tile.getTopMargin(), tile.getWidthWithoutMargins(), tile.getHeightWithoutMargins());
				denoisedTileImp = denoisedTileImp.crop();
				
				// Put denoised tile at the correct position in the result image
				denoisedImage.insert(denoisedTileImp, tile.getXPositionWithoutMargins(), tile.getYPositionWithoutMargins());
			}

			denoisedStack.addSlice("", denoisedImage);
			
			publish(100 * (slice - range.getFirst() + 1) / (range.getLast() - range.getFirst() + 1));
		}
		
		String title = noisyImagePlus.getTitle() + " ["+ algorithmName + "]";
		ImagePlus denoisedImagePlus = new ImagePlus(title, denoisedStack);
		return denoisedImagePlus;
	}
	
	@Override
	protected void process(List<Integer> chunks)  // executed on the Java EDT, so we can update the UI here
	{
		for (Integer slice : chunks)
		{
			progressBar.setValue(slice);
		}
	}
	
	@Override
	public void done()  // executed on the Java EDT, we can update the UI here.
	{
		try
		{
			whenDone.run();
			
			ImagePlus denoisedImagePlus = get();
			denoisedImagePlus.show();			
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
