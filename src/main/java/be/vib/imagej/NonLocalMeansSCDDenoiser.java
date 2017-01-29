package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

class NonLocalMeansSCDDenoiser extends Denoiser
{
	private final NonLocalMeansSCDParams params;
	
	public NonLocalMeansSCDDenoiser(NonLocalMeansSCDParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call()
	{		
		QFunction nlmeansSCD = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\nlmeans_scd.q",
                                                   "deconv_nlmeans_sc(mat,mat,scalar,int,int,int,scalar,scalar,scalar,mat)");
				
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.blurKernelSigma)); 

		// FIXME: QFunctionJNI should support any number of parameters, since so does quasar_dsl.h's Function::operator()(...).	
		
		QValue result = nlmeansSCD.apply(imageCube,
				                         blurKernel,
				                         new QValue(params.lambda),
				                         new QValue(params.numIterations),
				                         new QValue(NonLocalMeansSCDParams.halfSearchSize),
				                         new QValue(NonLocalMeansSCDParams.halfBlockSize),
				                         new QValue(params.h),
				                         new QValue(params.sigma0),
				                         new QValue(NonLocalMeansSCDParams.alpha),
				                         new QValue(NonLocalMeansSCDParams.emCorrFilterInv));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}