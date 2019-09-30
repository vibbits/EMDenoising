package be.vib.imagej;

import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class ImageNormalizer
{
	final static int LO_PERCENTILE = 2;  // 2%
	final static int HI_PERCENTILE = 98; // 98%
	
	boolean imageIs8Bit; // is the input image 8 or 16-bits/pixel?
	
	// 2% and 98% percentiles of the image, only used if imageIs8Bit == false (i.e. we are dealing with 16-bit images)
	int loPercentile; 
	int hiPercentile;
	
	public ImageNormalizer(ImagePlus image)
	{
		ImageProcessor ip = ImageUtils.currentProcessor(image);
		final int dynamicRange = (int)ImageUtils.bitRange(ip);
		imageIs8Bit = (dynamicRange == 255);
		if (!imageIs8Bit)
		{
			assert(ip instanceof ShortProcessor);
			final int[] hist = build_histogram((ShortProcessor)ip);
			loPercentile = ith_percentile(hist, ip.getPixelCount(), LO_PERCENTILE);
			hiPercentile = ith_percentile(hist, ip.getPixelCount(), HI_PERCENTILE);
			if (loPercentile == hiPercentile)
			{
				loPercentile = 0;
				hiPercentile = dynamicRange;
			}
		}
		else
		{
			loPercentile = hiPercentile = -1; // unused
		}
	}
	
	private static int[] build_histogram(ShortProcessor image)  // for 16-bit images only
	{
		// Create array to hold histogram of our 16-bit pixel intensities
		final int histogram_size = 65536;
		int hist[] = new int[histogram_size];
		for (int i = 0; i < histogram_size; i++)
			hist[i] = 0;
			
		// Fill histogram
		final int num_pixels_in_image = image.getPixelCount();
		short[] pixels = (short[])image.getPixels();
		for (int i = 0; i < num_pixels_in_image; i++)
		{
			// ShortProcessor stores the true unsigned 16-bit image pixel intensities in the Java datatype 'short',
			// which is a signed 16-bit number, so we need to convert it back to _un_signed 16-bit.
			int intensity = Short.toUnsignedInt(pixels[i]);  

			// Accumulate in histogram
			hist[intensity]++;
		}		
		return hist;
	}
	
	private static int ith_percentile(int[] hist, int num_pixels_in_image, int percentile)
	{
		assert(percentile >= 0 && percentile <= 100);	
		
		// Find the percentile by summing over the histogram until we 
		// hit the desired number of pixels.
		float target_fraction = percentile / 100.0f;
		int target_num_pixels = (int)Math.ceil(target_fraction * num_pixels_in_image);
		
		int cumul = 0;
		for (int i = 0; i < hist.length; i++)
		{
			if (cumul >= target_num_pixels)
				return i;
			cumul += hist[i];
		}

		return target_num_pixels;
	}
	
	public QValue normalize(ImageProcessor ip, boolean toByteRange)  // if toByteRange==true then normalize to [0,255] otherwise to [0,1]
	{
		QValue cube = ImageUtils.newCubeFromImage(ip);
		
		if (imageIs8Bit)  // original image is 8-bit/pixel
		{
			if (toByteRange)
			{
				// normalize from [0,255] to [0,255]
				// nothing to be done here 
			}
			else
			{
				// normalize from [0,255] to [0,1]
				QUtils.inplaceDivide(cube, 255.0f);		
			}
		}
		else // original image is 16-bit/pixel
		{
			if (toByteRange)
			{
				// normalize from 16-bit [lo,hi] to [0,255]
				QUtils.inplaceSubtract(cube, (float)loPercentile);
				QUtils.inplaceDivide(cube, ((float)(hiPercentile - loPercentile)) / 255.0f);
				QValue lo = new QValue(0.0f);
				QValue hi = new QValue(255.0f);
				QUtils.inplaceClamp(cube, lo, hi);	
			}
			else
			{
				// normalize from 16-bit [lo,hi] to [0,1]
				QUtils.inplaceSubtract(cube, (float)loPercentile);
				QUtils.inplaceDivide(cube, (float)(hiPercentile - loPercentile));
				QValue lo = new QValue(0.0f);
				QValue hi = new QValue(1.0f);
				QUtils.inplaceClamp(cube, lo, hi);
			}
		}

		// Test
		// float f = cube.at(0, 0).getFloat();
		
		return cube;
	}
	
	public ImageProcessor denormalize(ImageProcessor ip, QValue cube, boolean fromByteRange)  // if fromByteRange==true, then denormalize from [0,255] otherwise from [0,1]
	{		
		if (imageIs8Bit) // original image is 8-bit/pixel
		{
			if (fromByteRange)
			{
				// denormalize from [0,255] to [0,255]
				// nothing to be done here 
			}
			else
			{
				// denormalize from [0,1] to [0,255]
				QUtils.inplaceMultiply(cube, 255.0f);		
			}
		}
		else // original image is 16-bit/pixel
		{
			if (fromByteRange)
			{
				// denormalize from [0,255] to 16-bit [lo,hi]
				QUtils.inplaceMultiply(cube, ((float)(hiPercentile - loPercentile)) / 255.0f);
				QUtils.inplaceAdd(cube, (float)loPercentile);	
			}
			else
			{
				// denormalize from [0,1] to 16-bit [lo,hi]
				QUtils.inplaceMultiply(cube, (float)(hiPercentile - loPercentile));
				QUtils.inplaceAdd(cube, (float)loPercentile);	
			}
		}
		
		ImageProcessor denormalizedIp = ImageUtils.newImageFromCube(ip, cube);  // note: newImageFromCube() also clips cube values to the allowed 8-bit or 16-bit pixel values
		return denormalizedIp;
	}
}