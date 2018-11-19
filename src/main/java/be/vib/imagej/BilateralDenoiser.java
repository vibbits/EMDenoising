package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class BilateralDenoiser extends Denoiser
{	
	public BilateralDenoiser(BilateralParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction bilateralFilter = new QFunction("bilateral_filter_denoise(cube,scalar,scalar,int)"); 
				
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		final int dynamicRange = (int)ImageUtils.bitRange(image);
		
		final float r = dynamicRange / 255.0f;

		// IMPROVEME: avoid the scaling, the Quasar bilateral filter can probably handle 16-bit image data
		if (dynamicRange != 255)
		{
			// Scale pixels values to [0, 255]
			QUtils.inplaceDivide(noisyImageCube, r);  
		}

		BilateralParams params = (BilateralParams)this.params;
		
		QValue denoisedImageCube = bilateralFilter.apply(noisyImageCube,
				                                         new QValue(params.rangeSigma),
				                                         new QValue(params.spatialSigma),
				                                         new QValue(255));
		
		noisyImageCube.dispose();

		if (dynamicRange != 255)
		{
			// Scale pixels values back to original range
			QUtils.inplaceMultiply(denoisedImageCube, r); 
		}

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
