package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class NonLocalMeansSCDDenoiser extends Denoiser
{
	final NonLocalMeansSCDParams params;
	
	public NonLocalMeansSCDDenoiser(LinearImage image, NonLocalMeansSCDParams params)
	{
		super(image);
		this.params = params;
	}
	
	
	
	@Override
	public byte[] call()
	{		
		QFunction nlmeansSCD = loadDenoiseFunction("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\nlmeans_scd.q",
                                                   "deconv_nlmeans_sc(mat,mat,scalar,int,int,int,scalar,scalar,scalar,mat)");
				
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.blurKernelSigma)); 

		// FIXME: QFunction.apply() does not support more than 8 arguments, and we're providing 10!
		//        (Is there a way to pass QFunction.apply a QValues[] instead? Does the Quasar C++ support this?)
		QValue result = nlmeansSCD.apply(imageCube,
				                         blurKernel,
				                         new QValue(params.lambda),
				                         new QValue(params.numIterations),
				                         new QValue(NonLocalMeansSCDParams.halfSearchSize),
				                         new QValue(NonLocalMeansSCDParams.halfBlockSize),
				                         new QValue(params.h),
				                         new QValue(NonLocalMeansSCDParams.sigma0),
				                         new QValue(NonLocalMeansSCDParams.alpha),
				                         new QValue(NonLocalMeansSCDParams.emCorrFilterInv));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();		
		
		return outputPixels;
	}
}