package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class NonLocalMeansSCDenoiser extends Denoiser
{
	private final NonLocalMeansSCParams params;
	
	public NonLocalMeansSCDenoiser(NonLocalMeansSCParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{		
		QFunction nlmeansSC = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\nlmeans_sc.q",
                                                  "denoise_nlmeans_sc(mat,int,int,scalar,scalar,scalar,mat)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		QValue result = nlmeansSC.apply(imageCube,
				                        new QValue(NonLocalMeansSCParams.halfSearchSize),
				                        new QValue(NonLocalMeansSCParams.halfBlockSize),
				                        new QValue(params.h),
				                        new QValue(NonLocalMeansSCParams.sigma0),
				                        new QValue(NonLocalMeansSCParams.alpha),
				                        new QValue(NonLocalMeansSCParams.emCorrFilterInv));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.dispose();
		imageCube.dispose();		
		
		return outputPixels;
	}
}