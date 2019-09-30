package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class BilateralDenoiser extends Denoiser
{	
	public BilateralDenoiser(BilateralParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call()
	{
		QFunction bilateralFilter = new QFunction("bilateral_filter_denoise(cube,scalar,scalar,int)"); 
				
		final boolean byteRange = true;  // bilateral filter expects values in [0,255]
		QValue noisyImageCube = normalizer.normalize(image, byteRange);

		BilateralParams params = (BilateralParams)this.params;
		
		QValue denoisedImageCube = bilateralFilter.apply(noisyImageCube,
				                                         new QValue(params.rangeSigma),
				                                         new QValue(params.spatialSigma),
				                                         new QValue(255));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
