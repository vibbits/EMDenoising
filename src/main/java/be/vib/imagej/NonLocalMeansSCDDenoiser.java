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
		if (params.decorrelation)
		{
			return params.deconvolution ? nonLocalMeansCD() : nonLocalMeansC();						
		}
		else
		{
			return params.deconvolution ? nonLocalMeansD() : nonLocalMeans();			
		}
	}
	
	public ImageProcessor nonLocalMeans() throws NoSuchFileException
	{				
		QFunction nlmeans = new QFunction("denoise_nlmeans(mat,int,int,scalar)");

		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue denoisedImageCube = nlmeans.apply(noisyImageCube,
							                     new QValue(params.halfSearchSize),
							                     new QValue(params.halfBlockSize),
							                     new QValue(params.h));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansD() throws NoSuchFileException
	{		
		QFunction nlmeansD = new QFunction("deconv_nlmeans(mat,mat,scalar,int,int,int,scalar)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSigma)); 
		
		QValue denoisedImageCube = nlmeansD.apply(noisyImageCube,
  						                          blurKernel,
							                      new QValue(params.deconvolutionParams.lambda),  // FIXME: see nlmeans.q (lambda depends on decorrelate or not)
							                      new QValue(params.deconvolutionParams.numIterations),
							                      new QValue(params.halfSearchSize),
							                      new QValue(params.halfBlockSize),
							                      new QValue(params.h));
		
		noisyImageCube.dispose();
		blurKernel.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansCD() throws NoSuchFileException
	{		
		QFunction nlmeansCD = new QFunction("deconv_nlmeans_c(mat,mat,scalar,int,int,int,scalar,mat)");
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansSCDParams.DeconvolutionParams.blurKernelSigma)); 
		
		QValue corrFilterInv = new QValue(NonLocalMeansSCDParams.emCorrFilterInv);
		
		QValue denoisedImageCube = nlmeansCD.apply(noisyImageCube,
							                       blurKernel,
							                       new QValue(params.deconvolutionParams.lambda),   // FIXME: see nlmeans.q (lambda depends on decorrelate or not)
							                       new QValue(params.deconvolutionParams.numIterations),
							                       new QValue(params.halfSearchSize),
							                       new QValue(params.halfBlockSize),
							                       new QValue(params.h),
						                           corrFilterInv);
		
		noisyImageCube.dispose();
		blurKernel.dispose();
		corrFilterInv.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansC() throws NoSuchFileException
	{		
		QFunction nlmeansSC = new QFunction("denoise_nlmeans_c(mat,int,int,scalar,mat)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue corrFilterInv = new QValue(NonLocalMeansSCDParams.emCorrFilterInv);
		
		QValue denoisedImageCube = nlmeansSC.apply(noisyImageCube,
							   				       new QValue(params.halfSearchSize),
											       new QValue(params.halfBlockSize),
											       new QValue(params.h),
											       corrFilterInv);
		
		noisyImageCube.dispose();
		corrFilterInv.dispose();
		
		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);
		
		denoisedImageCube.dispose();
		
		return denoisedImage;
	}
}