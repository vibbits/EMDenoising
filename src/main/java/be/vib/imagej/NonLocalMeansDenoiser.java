package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class NonLocalMeansDenoiser extends Denoiser
{	
	public NonLocalMeansDenoiser(NonLocalMeansParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call()
	{	
		NonLocalMeansParams params = (NonLocalMeansParams)this.params;

		if (params.decorrelation)
		{
			assert(false);  // decorrelation is not offered to the user yet
			return params.deconvolution ? nonLocalMeansCD() : nonLocalMeansC();						
		}
		else
		{
			return params.deconvolution ? nonLocalMeansD() : nonLocalMeans();			
		}
	}
	
	public ImageProcessor nonLocalMeans()
	{				
		QFunction nlmeans = new QFunction("denoise_nlmeans(mat,int,int,scalar)");

		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);

		NonLocalMeansParams params = (NonLocalMeansParams)this.params;

		QValue denoisedImageCube = nlmeans.apply(noisyImageCube,
							                     new QValue(params.halfSearchSize),
							                     new QValue(params.halfBlockSize),
							                     new QValue(params.h));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansD()
	{		
		QFunction nlmeansD = new QFunction("deconv_nlmeans(mat,mat,scalar,int,int,int,scalar)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSigma)); 
		
		NonLocalMeansParams params = (NonLocalMeansParams)this.params;

		QValue denoisedImageCube = nlmeansD.apply(noisyImageCube,
  						                          blurKernel,
							                      new QValue(params.deconvolutionParams.lambda),
							                      new QValue(params.deconvolutionParams.numIterations),
							                      new QValue(params.halfSearchSize),
							                      new QValue(params.halfBlockSize),
							                      new QValue(params.h));
		
		noisyImageCube.dispose();
		blurKernel.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
		
	private ImageProcessor nonLocalMeansCD()
	{		
		assert(false); // decorrelation is not currently supported
		
		QFunction nlmeansCD = new QFunction("deconv_nlmeans_c(mat,mat,scalar,int,int,int,scalar,mat)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);

		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		QValue blurKernel = fgaussian.apply(new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSize), new QValue(NonLocalMeansParams.DeconvolutionParams.blurKernelSigma)); 
		
		QValue corrFilterInv = new QValue(NonLocalMeansParams.emCorrFilterInv);
		
		NonLocalMeansParams params = (NonLocalMeansParams)this.params;

		QValue denoisedImageCube = nlmeansCD.apply(noisyImageCube,
							                       blurKernel,
							                       new QValue(params.deconvolutionParams.lambda),
							                       new QValue(params.deconvolutionParams.numIterations),
							                       new QValue(params.halfSearchSize),
							                       new QValue(params.halfBlockSize),
							                       new QValue(params.h),
						                           corrFilterInv);
		
		noisyImageCube.dispose();
		blurKernel.dispose();
		corrFilterInv.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	private ImageProcessor nonLocalMeansC()
	{		
		assert(false); // decorrelation is not currently supported
		
		QFunction nlmeansSC = new QFunction("denoise_nlmeans_c(mat,int,int,scalar,mat)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);

		QValue corrFilterInv = new QValue(NonLocalMeansParams.emCorrFilterInv);
		
		NonLocalMeansParams params = (NonLocalMeansParams)this.params;

		QValue denoisedImageCube = nlmeansSC.apply(noisyImageCube,
							   				       new QValue(params.halfSearchSize),
											       new QValue(params.halfBlockSize),
											       new QValue(params.h),
											       corrFilterInv);
		
		noisyImageCube.dispose();
		corrFilterInv.dispose();
		
		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);
		
		denoisedImageCube.dispose();
		
		return denoisedImage;
	}
}