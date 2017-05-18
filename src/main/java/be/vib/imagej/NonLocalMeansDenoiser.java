package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class NonLocalMeansDenoiser extends Denoiser
{
	private final NonLocalMeansParams params;
	
	public NonLocalMeansDenoiser(NonLocalMeansParams params)
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
		
		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		QValue denoisedImageCube = nlmeans.apply(noisyImageCube,
							                     new QValue(params.halfSearchSize),
							                     new QValue(params.halfBlockSize),
							                     new QValue(params.h));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansD() throws NoSuchFileException
	{		
		QFunction nlmeansD = new QFunction("deconv_nlmeans(mat,mat,scalar,int,int,int,scalar)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSigma)); 
		
		QValue denoisedImageCube = nlmeansD.apply(noisyImageCube,
  						                          blurKernel,
							                      new QValue(params.deconvolutionParams.lambda),  // FIXME: see nlmeans.q (lambda depends on decorrelate or not)
							                      new QValue(params.deconvolutionParams.numIterations),
							                      new QValue(params.halfSearchSize),
							                      new QValue(params.halfBlockSize),
							                      new QValue(params.h));
		
		noisyImageCube.dispose();
		blurKernel.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansCD() throws NoSuchFileException
	{		
		QFunction nlmeansCD = new QFunction("deconv_nlmeans_c(mat,mat,scalar,int,int,int,scalar,mat)");
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);

		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSigma)); 
		
		QValue corrFilterInv = new QValue(NonLocalMeansParams.emCorrFilterInv);
		
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

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansC() throws NoSuchFileException
	{		
		QFunction nlmeansSC = new QFunction("denoise_nlmeans_c(mat,int,int,scalar,mat)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		QValue corrFilterInv = new QValue(NonLocalMeansParams.emCorrFilterInv);
		
		QValue denoisedImageCube = nlmeansSC.apply(noisyImageCube,
							   				       new QValue(params.halfSearchSize),
											       new QValue(params.halfBlockSize),
											       new QValue(params.h),
											       corrFilterInv);
		
		noisyImageCube.dispose();
		corrFilterInv.dispose();
		
		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);
		
		denoisedImageCube.dispose();
		
		return denoisedImage;
	}
}