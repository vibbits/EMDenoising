package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class AnisotropicDiffusionDenoiser extends Denoiser
{
	public AnisotropicDiffusionDenoiser(AnisotropicDiffusionParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call()
	{
		QFunction diffusion = new QFunction("denoise_anisotropic_diffusion(mat,int,scalar,scalar,string)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
		
		AnisotropicDiffusionParams params = (AnisotropicDiffusionParams)this.params;
		
		QValue denoisedImageCube = diffusion.apply(noisyImageCube,
				                                   new QValue(params.numIterations),
				                                   new QValue(params.stepSize),
				                                   new QValue(params.diffusionFactor),
				                                   new QValue(params.diffusionFunction));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}