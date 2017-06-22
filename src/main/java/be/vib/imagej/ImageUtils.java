package be.vib.imagej;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.Rectangle;

public class ImageUtils
{
	/**
	 * Converts the given array of grayscale values into a width x height grayscale image.
	 * 
	 * @param bytes An array of width * height grayscale values (0=black, 255=white)
	 * @param width The width of the image
	 * @param height The height of the image
	 * @return The corresponding grayscale BufferedImage
	 */
	public static BufferedImage createGrayscaleBufferedImage(byte[] bytes, int width, int height)
	{
		// Cfr: http://www.programcreek.com/java-api-examples/index.php?api=java.awt.image.Raster
		
		ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		
		boolean hasAlpha = false;
		boolean isAlphaPremultiplied = false;
		ComponentColorModel colorModel = new ComponentColorModel(colorSpace, hasAlpha, isAlphaPremultiplied, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		
		SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
		DataBufferByte dataBuffer = new DataBufferByte(bytes, width * height);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
		
		BufferedImage image = new BufferedImage(colorModel, raster, false, null);
		return image;
	}
	
	/**
	 * 
	 * @param image The original unscaled image
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage scaleImage(BufferedImage image, int width, int height)
	{
		if (image == null) return null;
		
		BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics2D.drawImage(image, 0, 0, width, height, null);
		graphics2D.dispose();
		
		return scaledImage;
	}
	
	public static BufferedImage createGrayscaleTestImage(int w, int h)
	{
		byte[] pixels = new byte[w * h];
		for (int i = 0; i < w * h; i++)
			pixels[i] = (byte)(i%257);
		
		return ImageUtils.createGrayscaleBufferedImage(pixels, w, h);		
	}
	
	public static BufferedImage deepCopy(BufferedImage image)
	{
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}	

	/** 
	 * Returns a new ImageProcessor with a copy of the current slice in the given image or image stack,
	 * cropped to the ROI of the given image.
	 */
	public static ImageProcessor cropImage(ImagePlus image)
	{
		int slice = image.getCurrentSlice();
		ImageStack stack = image.getStack();
		ImageProcessor imp = stack.getProcessor(slice);
		imp.setRoi(image.getRoi());
		return imp.crop();
	}
	
	/**
	 * Crops the given image to a given rectangle.
	 * 
	 * @param image The original image.
	 * @param rect The rectangle to crop the image to.
	 * @return A new (copied) image with only the cropped portion of the original image. 
	 */
	public static BufferedImage cropImage(BufferedImage image, Rectangle rect)
	{
		// getSubImage() returns an image that shares data with the original image
		// so we deep copy it.
		if (rect == null || rect.isEmpty())
			return deepCopy(image);
		else
			return deepCopy(image.getSubimage(rect.x, rect.y, rect.width, rect.height));
	}
	
	/**
	 * Copy the display range from the source to the destination image.
	 * 
	 * The display range are the minimum and maximum values shown
	 * in the B&C (Brightness and Contrast) window in ImageJ/Fiji.
	 * It may be different from the minimum and maximum pixel values in the image. 
	 * 
	 * @param src The source image.
	 * @param dst The destination image.
	 */
	public static void CopyDisplayRange(ImageProcessor src, ImageProcessor dst)
	{
		dst.setMinAndMax(src.getMin(), src.getMax());
	}

	public static QValue newCubeFromImage(ImageProcessor image)
	{		
		if (image instanceof ByteProcessor)
		{
			return QUtils.newCubeFromGrayscaleByteArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		}
		else if (image instanceof ShortProcessor)
		{
			return QUtils.newCubeFromGrayscaleShortArray(image.getWidth(), image.getHeight(), (short[])image.getPixels());			
		}
		else
		{
			throw new RuntimeException("Only 8 bit/pixel and 16 bit/pixel grayscale images are supported.");
		}
	}

	public static int bitDepth(ImageProcessor image)
	{
		return image.getBitDepth();
	}

	public static float bitRange(ImageProcessor image)
	{
		return (1 << bitDepth(image)) - 1;
	}

	public static ImageProcessor newImageFromCube(ImageProcessor image, QValue cube)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		if (image instanceof ByteProcessor)
		{
			byte[] pixels = QUtils.newGrayscaleByteArrayFromCube(width, height, cube);
			return new ByteProcessor(width, height, pixels);
		}
		else if (image instanceof ShortProcessor)
		{
			short[] pixels = QUtils.newGrayscaleShortArrayFromCube(width, height, cube);
			return new ShortProcessor(width, height, pixels, null);
		}
		else
		{
			throw new RuntimeException("Only 8 bit/pixel and 16 bit/pixel grayscale images are supported.");
		}
	}
	
//	/**
//	 * Returns the maximum pixel value present in the image
//	 * (or zero for a 0x0 image). It is not necessarily the same
//	 * as the maximum display value ImageProcessor.getMax().
//	 * 
//	 * @param image the image
//	 * @throws RuntimeException if the image is not 8 or 16 bit / pixel
//	 */
//	public static float maxValue(ImageProcessor image)
//	{
//		final int n = image.getPixelCount();
//		
//		if (image instanceof ByteProcessor)
//		{
//			byte[] pixels = (byte[])image.getPixels();
//			byte max = 0;
//			for (int i = 0; i < n; i++)
//			{
//				byte v = pixels[i];
//				if (v > max)
//					max = v;
//			}
//			return max;
//		}
//		else if (image instanceof ShortProcessor)
//		{
//			short[] pixels = (short[])image.getPixels();
//			short max = 0;
//			for (int i = 0; i < n; i++)
//			{
//				short v = pixels[i];
//				if (v > max)
//					max = v;
//			}
//			return max;		
//		}
//		else
//		{
//			throw new RuntimeException("Only 8 bit/pixel and 16 bit/pixel grayscale images are supported.");
//		}
//	}

	
}
