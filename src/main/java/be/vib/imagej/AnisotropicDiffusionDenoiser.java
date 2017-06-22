package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class AnisotropicDiffusionDenoiser extends Denoiser
{
	public AnisotropicDiffusionDenoiser(AnisotropicDiffusionParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction diffusion = new QFunction("denoise_anisotropic_diffusion(mat,int,scalar,scalar,string)");
		
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		AnisotropicDiffusionParams params = (AnisotropicDiffusionParams)this.params;
		
		QValue denoisedImageCube = diffusion.apply(noisyImageCube,
				                                   new QValue(params.numIterations),
				                                   new QValue(params.stepSize),
				                                   new QValue(params.diffusionFactor),
				                                   new QValue(params.diffusionFunction));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}