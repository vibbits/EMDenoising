package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;
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
	public ByteProcessor call()
	{
		QFunction diffusion = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\anisotropic_diffusion.q",
                                                  "anisotropic_diffusion(mat,int,scalar,scalar,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
				
		QValue result = diffusion.apply(imageCube,
				                        new QValue(params.numIterations),
				                        new QValue(params.stepSize),
				                        new QValue(params.diffusionFactor),
				                        new QValue(params.diffusionFunction));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);

		result.dispose();
		imageCube.dispose();		

		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}