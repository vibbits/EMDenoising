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
	
	NoiseEstimator(ImageProcessor image)
	{		
		// The implementation of the Liu noise estimator in estimate_noise.q allocates quite large memory blocks: on the order of ((7 x 7) x (width x height) x sizeof(float))
		// To avoid CUDA out-of-memory conditions on larger images we estimate the noise from the central 512x512 pixel region of the image.
		this.image = restrictSize(image, 512);
	}
	
	private ImageProcessor restrictSize(ImageProcessor image, int maxSize)
	{
		int w = Math.min(maxSize, image.getWidth());
		int h = Math.min(maxSize, image.getHeight());
		int x = (image.getWidth() - w) / 2;
		int y = (image.getHeight() - h) / 2;
		Rectangle rect = new Rectangle(x, y, w, h);
				
		ImageProcessor imageCopy = image.duplicate();  // CHECKME: we copy the image processor to avoid changing its ROI, is this really needed?
		imageCopy.setRoi(rect);
		return imageCopy.crop();		
	}

	@Override
	public Float call() throws Exception
	{
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
//		QFunction imwrite = new QFunction("imwrite(string,cube)");
//		imwrite.apply(new QValue("e:\\noisy.tif"), noisyImageCube);

		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
		
		QValue noise = null;
		
		boolean liu = false; // There are several issues with this implementation (tau0 hard-coded, negative eigenvalues for covariance matrix, excessive memory usage even for images of reasonable size - 1500 x 1500 pixels) 
		if (liu)
		{
			QFunction estimateNoise = new QFunction("V_estimate_noise(mat)"); 
			// Note: the V_ is a workaround for a problem in the current version of Quasar when the function name (estimate_noise) is identical to the source file name (estimate_noise.q)
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
