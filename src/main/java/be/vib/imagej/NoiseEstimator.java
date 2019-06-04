package be.vib.imagej;

import java.awt.Rectangle;
import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class NoiseEstimator implements Callable<Float>
{
	private ImageProcessor image;
	
	private static boolean liuNoiseEstimation = false; // if true then the Liu noise estimator is used; otherwise MAD is used
	// Note: Currently there are several issues with the Liu noise implementation in Quasar
	// (tau0 hard-coded, negative eigenvalues for covariance matrix,
	// excessive memory usage even for images of reasonable size - 1500 x 1500 pixels)
	// so we use MAD instead.
	
	NoiseEstimator(ImageProcessor image)
	{		
		// Restrict the noise estimation to the central region of the image (for the sake of performance but especially of memory efficiency).
		// The current implementation of the Liu noise estimator in estimate_noise.q allocates quite large memory blocks:
		// on the order of ((7 x 7) x (width x height) x sizeof(float)). 
		this.image = liuNoiseEstimation ? restrictSize(image, 512) : restrictSize(image, 2048);
	}
	
	// Return a copy of the image, restricted to maxSize x maxSize pixels.
	// If the image is larger than maxSize, the portion of maxSize pixels centered copy around the middle of the image is returned.
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
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		// For testing
		// QFunction imwrite = new QFunction("imwrite(string,cube)");
		// imwrite.apply(new QValue("e:\\noisy.tif"), noisyImageCube);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
		
		QValue noise = null;
				 
		if (liuNoiseEstimation)
		{
			QFunction estimateNoise = new QFunction("V_estimate_noise(mat)"); 
			// Note: the V_ prefix was a workaround for a problem in a previous version of Quasar when the function name (estimate_noise)
			// is identical to the source file name (estimate_noise.q). It can probably be removed again.
			noise = estimateNoise.apply(noisyImageCube);
		}
		else
		{
			QFunction estimateNoise = new QFunction("estimate_noise_mad(mat,int)");			
			noise = estimateNoise.apply(noisyImageCube, new QValue(2));
		}
		
		noisyImageCube.dispose();
		
		return noise.getFloat();		
	}

}
