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

import ij.ImagePlus;
import ij.process.ImageProcessor;

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
		// TODO: carefully chcek this code
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
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
	 * Note: the display range are the minimum and maximum values shown
	 * in the B&C (Brightness and Contrast) window in ImageJ/Fiji. 
	 * 
	 * @param src The source image.
	 * @param dst The destination image.
	 */
	public static void CopyDisplayRange(ImageProcessor src, ImageProcessor dst)
	{
		dst.setMinAndMax(src.getMin(), src.getMax());
	}
}
