package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
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
		QFunction nlmeans = QuasarTools.loadDenoiseFunction("nlmeans_scd.qlib",
                                                            "denoise_nlmeans(mat,int,int,scalar)");
		
		// The files nlmeans_denoising_stillimages.q and nlmeans_sc.q both contain implementation of the NLMS filter, but their API is slightly different.
		// The nlmeans_denoising_stillimages implementation is more general, but we stick to nlmeans_sc.q so NLMS and NLMS-SC share the same code.
		
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
}