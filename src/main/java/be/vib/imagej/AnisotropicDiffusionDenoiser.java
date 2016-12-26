package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class AnisotropicDiffusionDenoiser extends Denoiser
{
	private final AnisotropicDiffusionParams params;
	
	public AnisotropicDiffusionDenoiser(AnisotropicDiffusionParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public LinearImage call()
	{
		QFunction diffusion = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\anisotropic_diffusion.q",
                                                  "anisotropic_diffusion(mat,int,scalar,scalar,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
				
		QValue result = diffusion.apply(imageCube,
				                        new QValue(params.numIterations),
				                        new QValue(params.stepSize),
				                        new QValue(params.diffusionFactor),
				                        new QValue(params.diffusionFunction));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);

		result.dispose();
		imageCube.dispose();		

		return new LinearImage(image.width, image.height, outputPixels);
	}
}