package be.vib.imagej;

import java.awt.Rectangle;
import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

// A Java wrapper around a MAD (median absolute deviation) noise estimator implemented in Quasar.
public class NoiseEstimator implements Callable<Float>
{
	private ImageProcessor image;
	private ImageNormalizer normalizer;
	
	NoiseEstimator(ImageProcessor image, ImageNormalizer normalizer)
	{		
		// Restrict the noise estimation to the central region of the image
		// (for the sake of performance and to limit GPU memory usage in case of huge images).
		this.image = restrictSize(image, 2048);
		this.normalizer = normalizer;
	}
	
	// Return a copy of the image, restricted to maxSize x maxSize pixels.
	// If the image is larger than maxSize, the portion of maxSize pixels centered around the middle of the image is returned.
	private ImageProcessor restrictSize(ImageProcessor image, int maxSize)
	{
		int w = Math.min(maxSize, image.getWidth());
		int h = Math.min(maxSize, image.getHeight());
		int x = (image.getWidth() - w) / 2;
		int y = (image.getHeight() - h) / 2;
		Rectangle rect = new Rectangle(x, y, w, h);
				
		ImageProcessor imageCopy = image.duplicate();  // copy the image processor to avoid changing its ROI
		imageCopy.setRoi(rect);
		return imageCopy.crop();		
	}

	@Override
	public Float call() throws Exception
	{		
		final boolean toByteRange = true;  // the Quasar MAD noise estimator prefers a byte range as pixel data
		QValue noisyImageCube = normalizer.normalize(image, toByteRange);
		
		QFunction estimateNoise = new QFunction("estimate_noise_mad(mat,int)");			
		QValue noise = estimateNoise.apply(noisyImageCube, new QValue(2));
		
		noisyImageCube.dispose();
		
		return noise.getFloat() / 255.0f;  // Return the noise standard deviation, for image pixel intensities normalized to be in [0,1]	
	}

}
