package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class TotalVariationDenoiser extends Denoiser
{
	public TotalVariationDenoiser(TotalVariationParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call()
	{
		QFunction total_variation_denoise = new QFunction("total_variation_denoise(mat,scalar,int,scalar)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
				
		TotalVariationParams params = (TotalVariationParams)this.params;
		
		QValue denoisedImageCube = total_variation_denoise.apply(noisyImageCube,
                                                                 new QValue(params.lambda),
				                                                 new QValue(params.numIterations),
				                                                 new QValue(TotalVariationParams.alpha));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}