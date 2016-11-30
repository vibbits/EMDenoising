package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class NonLocalMeansDenoiser extends Denoiser
{
	private final NonLocalMeansParams params;
	
	public NonLocalMeansDenoiser(NonLocalMeansParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{		
		QFunction nlmeans = loadDenoiseFunction("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\nlmeans_sc.q",
                                                "denoise_nlmeans(mat,int,int,scalar)");
		
		// The files nlmeans_denoising_stillimages.q and nlmeans_sc.q both contain implementation of the NLMS filter, but their API is slightly different.
		// The nlmeans_denoising_stillimages implementation is more general, but we stick to nlmeans_sc.q so NLMS and NLMS-SC share the same code.
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		QValue result = nlmeans.apply(imageCube,
				                      new QValue(params.halfSearchSize),
				                      new QValue(params.halfBlockSize),
				                      new QValue(params.h));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();		
		
		return outputPixels;
	}
}