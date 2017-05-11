package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class AnisotropicDiffusionDenoiser extends Denoiser
{
	private final AnisotropicDiffusionParams params;
	
	public AnisotropicDiffusionDenoiser(AnisotropicDiffusionParams params)
	{
		super();
		this.params = params;
	}

	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction diffusion = new QFunction("denoise_anisotropic_diffusion(mat,int,scalar,scalar,string)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		QValue denoisedImageCube = diffusion.apply(noisyImageCube,
				                                   new QValue(params.numIterations),
				                                   new QValue(params.stepSize),
				                                   new QValue(params.diffusionFactor),
				                                   new QValue(params.diffusionFunction));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}