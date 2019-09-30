package be.vib.imagej;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import be.vib.bits.QExecutor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

// The DenoiseEngine class provides higher level tasks on top of the actual denoising:
// iteration over stack slices, tiling of the input image slices, denoised image contrast
// handling and adding denoising parameter meta-data to the denoised result.
// The denoising itself of each image tile is handled by the Denoiser class.
//
// The DenoiseEngine class is the main access point from ImageJ scripts
// to the denoising machinery.
//
// The publish(), process() and isCancelled() methods are useful when the DenoiseEngine
// is used in combination with user interface elements that allow the user to interrupt (cancel)
// the denoising calculations and that provide progress feedback.
public class DenoiseEngine
{
	private Denoiser denoiser;
	
	public DenoiseEngine(Denoiser denoiser)
	{
		this.denoiser = denoiser;
	}
	
	public ImagePlus denoise(ImagePlus noisyImagePlus, ImageNormalizer normalizer, ImageRange range, String title)
	{
		final int width = noisyImagePlus.getWidth();
		final int height = noisyImagePlus.getHeight();
		
		final int tileSize = denoiser.imageTileSize();
		final int margin = denoiser.imageMargin();
		
		final ImageStack noisyStack = noisyImagePlus.getStack();
						
		ImageStack denoisedStack = new ImageStack(width, height);
		
		int tileNr = 0;
		for (int slice = range.getFirst(); slice <= range.getLast(); slice++)
		{
			if (isCancelled())
				continue;
			
			ImageProcessor noisyImage = noisyStack.getProcessor(slice);
			ImageProcessor denoisedImage = (noisyImage instanceof ByteProcessor) ? new ByteProcessor(width, height) : new ShortProcessor(width, height); // blank image, will be filled below
			
			ImageTiler tiler = new ImageTiler(noisyImage, tileSize, tileSize, margin);
			for (ImageTile tile : tiler)
			{
				if (isCancelled())
					continue;
								
				// Get a noisy tile from the original image
				ImageProcessor noisyTileImp = tile.getImageWithMargins();
				
				// Denoise the tile
				try
				{
					denoiser.setImage(noisyTileImp, normalizer);
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
		
		if (isCancelled())
			return null;
		
		title = ij.WindowManager.makeUniqueName(title);
		
		ImagePlus denoisedImagePlus = new ImagePlus(title, denoisedStack);

		// Make sure the display range of our denoised result is the same as the noisy input.
		// Otherwise the denoised image may appear too dark or bright compared to the noisy version
		// even though the pixel values themselves are correct.
		ImageUtils.CopyDisplayRange(noisyImagePlus.getProcessor(), denoisedImagePlus.getProcessor());
		
		// Add denoise parameters as properties to the denoised image.
		// In the end we will probably want to store them as OME XML.
		// For now use ordinary properties.
		String info = getConcatenatedDenoisingParameters(denoiser.getParams());
		denoisedImagePlus.setProperty("Info", info);

		return denoisedImagePlus;
	}
	
	public void publish(Integer... chunks)
	{
		process(Arrays.asList(chunks));
	}
	
	public void process(List<Integer> chunks)
	{
		for (Integer chunk : chunks)
		{
			System.out.println("Denoised: " + chunk + "%");
		}
	}
	
	public boolean isCancelled()
	{
		return false;
	}
	
	static private String getConcatenatedDenoisingParameters(DenoiseParams params)
	{
		Properties props = params.getParameterList();
		
		String str = "";	
        for (String key : props.stringPropertyNames())
        {
             String value = props.getProperty(key);
             str = str + key + " = " + value + "\n";
        }				
        return str;
	}
}
