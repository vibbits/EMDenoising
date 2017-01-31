package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class NonLocalMeansSCDDenoiser extends Denoiser
{
	private final NonLocalMeansSCDParams params;
	
	public NonLocalMeansSCDDenoiser(NonLocalMeansSCDParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{		
		QFunction nlmeansSCD = QuasarTools.loadDenoiseFunction("nlmeans_scd.q",
                                                               "deconv_nlmeans_sc(mat,mat,scalar,int,int,int,scalar,scalar,scalar,mat)");
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.blurKernelSigma)); 

		// FIXME: QFunctionJNI should support any number of parameters, since so does quasar_dsl.h's Function::operator()(...).	
		
		QValue denoisedImageCube = nlmeansSCD.apply(noisyImageCube,
							                        blurKernel,
							                        new QValue(params.lambda),
							                        new QValue(params.numIterations),
							                        new QValue(NonLocalMeansSCDParams.halfSearchSize),
							                        new QValue(NonLocalMeansSCDParams.halfBlockSize),
							                        new QValue(params.h),
							                        new QValue(params.sigma0),
							                        new QValue(NonLocalMeansSCDParams.alpha),
						                            new QValue(NonLocalMeansSCDParams.emCorrFilterInv));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}