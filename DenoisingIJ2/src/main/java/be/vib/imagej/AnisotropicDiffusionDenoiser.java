package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class AnisotropicDiffusionDenoiser extends Denoiser
{
	final AnisotropicDiffusionParams params;
	
	public AnisotropicDiffusionDenoiser(LinearImage image, AnisotropicDiffusionParams params)
	{
		super(image);
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{
		if (!QHost.functionExists("anisotropic_diffusion"))
		{
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this AnisotropicDenoiser object.
			QHost.loadSourceModule("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\anisotropic_diffusion.q");
		}
		
		assert(QHost.functionExists("anisotropic_diffusion"));
		
		QFunction anisotropicDiffusion = new QFunction("anisotropic_diffusion(mat,int,scalar,scalar,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
				
		QValue result = anisotropicDiffusion.apply(imageCube,
				                                   new QValue(params.numIterations),
				                                   new QValue(params.stepSize),
				                                   new QValue(params.diffusionFactor),
				                                   new QValue(params.diffusionFunction));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);

		result.delete();
		imageCube.delete();		

		return outputPixels;
	}
}