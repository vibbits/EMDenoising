package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

class NonLocalMeansDenoiser extends Denoiser
{
	private final NonLocalMeansParams params;
	
	public NonLocalMeansDenoiser(NonLocalMeansParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call()
	{		
		QFunction nlmeans = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\nlmeans_sc.q",
                                                "denoise_nlmeans(mat,int,int,scalar)");
		
		// The files nlmeans_denoising_stillimages.q and nlmeans_sc.q both contain implementation of the NLMS filter, but their API is slightly different.
		// The nlmeans_denoising_stillimages implementation is more general, but we stick to nlmeans_sc.q so NLMS and NLMS-SC share the same code.
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		QValue result = nlmeans.apply(imageCube,
				                      new QValue(params.halfSearchSize),
				                      new QValue(params.halfBlockSize),
				                      new QValue(params.h));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}