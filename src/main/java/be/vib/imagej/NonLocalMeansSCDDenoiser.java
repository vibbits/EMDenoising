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
		return params.deconvolution ? nonLocalMeansSCD() : nonLocalMeansSC();
	}
	
	private ImageProcessor nonLocalMeansSCD() throws NoSuchFileException
	{		
		QFunction nlmeansSCD = QuasarTools.loadDenoiseFunction("nlmeans_scd.q",
                                                               "deconv_nlmeans_sc(mat,mat,scalar,int,int,int,scalar,scalar,scalar,mat)"); // TODO: rename to denoise_nlmeans_scd, the deconv prefix is easy to miss
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSigma)); 

		// FIXME: QFunctionJNI should support any number of parameters, since so does quasar_dsl.h's Function::operator()(...).	
		
		QValue corrFilterInv = new QValue(NonLocalMeansSCDParams.emCorrFilterInv);
		
		QValue denoisedImageCube = nlmeansSCD.apply(noisyImageCube,
							                        blurKernel,
							                        new QValue(params.deconvolutionParams.lambda),
							                        new QValue(params.deconvolutionParams.numIterations),
							                        new QValue(NonLocalMeansSCDParams.halfSearchSize),
							                        new QValue(NonLocalMeansSCDParams.halfBlockSize),
							                        new QValue(params.h),
							                        new QValue(params.sigma0),
							                        new QValue(NonLocalMeansSCDParams.alpha),
						                            corrFilterInv);
		
		noisyImageCube.dispose();
		blurKernel.dispose();
		corrFilterInv.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansSC() throws NoSuchFileException
	{		
		QFunction nlmeansSC = QuasarTools.loadDenoiseFunction("nlmeans_scd.q",
                                                              "denoise_nlmeans_sc(mat,int,int,scalar,scalar,scalar,mat)");

		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue corrFilterInv = new QValue(NonLocalMeansSCDParams.emCorrFilterInv);
		
		QValue denoisedImageCube = nlmeansSC.apply(noisyImageCube,
							   				       new QValue(NonLocalMeansSCDParams.halfSearchSize),
											       new QValue(NonLocalMeansSCDParams.halfBlockSize),
											       new QValue(params.h),
											       new QValue(params.sigma0),
											       new QValue(NonLocalMeansSCDParams.alpha),
											       corrFilterInv);
		
		noisyImageCube.dispose();
		corrFilterInv.dispose();
		
		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);
		
		denoisedImageCube.dispose();
		
		return denoisedImage;
	}
}