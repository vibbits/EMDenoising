package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class TikhonovDenoiser extends Denoiser
{
	public TikhonovDenoiser(TikhonovParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call()
	{
		TikhonovParams params = (TikhonovParams)this.params;
		
		return params.deconvolution ? tikhonovDenoisingWithDeconvolution() : tikhonovDenoising();						
	}

	public ImageProcessor tikhonovDenoising()
	{
		QFunction tikhonov_denoise = new QFunction("tikhonov_denoise(mat,scalar,int)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
		
		TikhonovParams params = (TikhonovParams)this.params;
		
		QValue denoisedImageCube = tikhonov_denoise.apply(noisyImageCube,
										    	          new QValue(params.lambda1),
				                                          new QValue(params.numIterations));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);
		
		denoisedImageCube.dispose();

		return denoisedImage;
	}

	public ImageProcessor tikhonovDenoisingWithDeconvolution()
	{
		QFunction tikhonov_denoise_deconvolution = new QFunction("tikhonov_denoise_dec(mat,mat,scalar,int)");
		
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
		
		TikhonovParams params = (TikhonovParams)this.params;
		
		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		
		QValue blurKernel = fgaussian.apply(new QValue(TikhonovParams.blurKernelSize), new QValue(params.sigma)); 
		
		QValue denoisedImageCube = tikhonov_denoise_deconvolution.apply(noisyImageCube,
												                        blurKernel,
											                            new QValue(params.lambda2),
				                                                        new QValue(params.numIterations));
		
		noisyImageCube.dispose();
		blurKernel.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}