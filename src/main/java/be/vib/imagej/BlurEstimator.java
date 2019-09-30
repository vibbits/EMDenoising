package be.vib.imagej;

import java.util.concurrent.Callable;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

// Java wrapper around the Quasar blurMetric() function for 
// estimating the amount of blur in an image.
public class BlurEstimator implements Callable<Float>
{
	private ImageProcessor image;
	
	BlurEstimator(ImageProcessor image)
	{		
		this.image = image;
	}

	@Override
	public Float call() throws Exception
	{
		QValue imageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(imageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
		
		QFunction estimateBlur = new QFunction("blurMetric(mat)");			
		QValue blur = estimateBlur.apply(imageCube);
		
		imageCube.dispose();
		
		return blur.getFloat();		
	}

}
