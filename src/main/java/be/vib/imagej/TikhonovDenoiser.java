package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class TikhonovDenoiser extends Denoiser
{
	public TikhonovDenoiser(TikhonovParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		TikhonovParams params = (TikhonovParams)this.params;
		
		return params.deconvolution ? tikhonovDenoisingWithDeconvolution() : tikhonovDenoising();						
	}

	public ImageProcessor tikhonovDenoising() throws NoSuchFileException
	{
		QFunction tikhonov_denoise = new QFunction("tikhonov_denoise(mat,scalar,int)");
		
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		TikhonovParams params = (TikhonovParams)this.params;
		
		QValue denoisedImageCube = tikhonov_denoise.apply(noisyImageCube,
										    	          new QValue(params.lambda1),
				                                          new QValue(params.numIterations));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}

	public ImageProcessor tikhonovDenoisingWithDeconvolution() throws NoSuchFileException
	{
		QFunction tikhonov_denoise_deconvolution = new QFunction("tikhonov_denoise_dec(mat,mat,scalar,int)");
		
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		TikhonovParams params = (TikhonovParams)this.params;
		
		QFunction fgaussian = new QFunction("fgaussian(int,scalar)");
		
		QValue blurKernel = fgaussian.apply(new QValue(TikhonovParams.blurKernelSize), new QValue(params.sigma)); 
		
		QValue denoisedImageCube = tikhonov_denoise_deconvolution.apply(noisyImageCube,
												                        blurKernel,
											                            new QValue(params.lambda2),
				                                                        new QValue(params.numIterations));
		
		noisyImageCube.dispose();
		blurKernel.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}