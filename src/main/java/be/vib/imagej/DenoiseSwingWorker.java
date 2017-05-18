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
import ij.process.ShortProcessor;

class DenoiseSwingWorker extends SwingWorker<ImagePlus, Integer>
{
	private Algorithm algorithm;
	private ImagePlus noisyImagePlus;
	private ImageRange range;
	private JProgressBar progressBar;
	private Runnable whenDone;  // Will be run on the EDT as soon as the DenoiseSwingWorker is done denoising. Can be used to indicate in the UI that we are done.
	
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
		// doInBackground is run is a thread different from the Java Event Dispatch Thread (EDT). Do not update Java Swing components here.
		final int width = noisyImagePlus.getWidth();
		final int height = noisyImagePlus.getHeight();
		
		final int tileWidth = 1024;  // TODO: make this dependent on e.g. algorithm and its parameters
		final int tileHeight = 1024;
		final int margin = 16; // FIXME: must be dependent on algorithm parameters to avoid artifacts along tile boundaries
		
		final ImageStack noisyStack = noisyImagePlus.getStack();
		
		final Denoiser denoiser = algorithm.getDenoiser();
				
		ImageStack denoisedStack = new ImageStack(width, height);
		
		int tileNr = 0;
		for (int slice = range.getFirst(); slice <= range.getLast(); slice++)
		{
			ImageProcessor noisyImage = noisyStack.getProcessor(slice);
			ImageProcessor denoisedImage = (noisyImage instanceof ByteProcessor) ? new ByteProcessor(width, height) : new ShortProcessor(width, height); // blank image, will be filled below
			
			ImageTiler tiler = new ImageTiler(noisyImage, tileWidth, tileHeight, margin);
			for (ImageTile tile : tiler)
			{
				// Get a noisy tile from the original image
				ImageProcessor noisyTileImp = tile.getImageWithMargins();
				
				// Denoise the tile
				try
				{
					denoiser.setImage(noisyTileImp);
					ImageProcessor denoisedTileImp = QExecutor.getInstance().submit(denoiser).get(); // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.

					// Remove tile margins
					denoisedTileImp.setRoi(tile.getLeftMargin(), tile.getTopMargin(), tile.getWidthWithoutMargins(), tile.getHeightWithoutMargins());
					denoisedTileImp = denoisedTileImp.crop();
					
					// Put denoised tile at the correct position in the result image
					denoisedImage.insert(denoisedTileImp, tile.getXPositionWithoutMargins(), tile.getYPositionWithoutMargins());
				}
				catch (ExecutionException | InterruptedException e)
				{
					e.printStackTrace();
				}
				
				// Progress feedback
				tileNr++;
				final int numTiles = tiler.getNumTiles() * (range.getLast() - range.getFirst() + 1);
				publish((100 * tileNr) / numTiles);
			}

			denoisedStack.addSlice("", denoisedImage);			
		}
		
		String title = noisyImagePlus.getTitle() + " ["+ algorithm.getReadableName() + "]";
		ImagePlus denoisedImagePlus = new ImagePlus(title, denoisedStack);

		// Make sure the display range of our denoised result is the same as the noisy input.
		// Otherwise the denoised image may appear too dark or bright compared to the noisy version
		// even though the pixel values themselves are correct.
		ImageUtils.CopyDisplayRange(noisyImagePlus.getProcessor(), denoisedImagePlus.getProcessor());

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
		whenDone.run();
		
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
