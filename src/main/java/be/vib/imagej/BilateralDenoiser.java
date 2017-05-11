package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class BilateralDenoiser extends Denoiser
{
	private final BilateralParams params;
	
	public BilateralDenoiser(BilateralParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction bilateralFilter = new QFunction("bilateral_filter_denoise(cube,cube,int,scalar)");  // Fast histogram based O(1) bilateral filter
		// FIXME: the O(1) implementation does not currently support 16 bit/pixel images.
		
		QFunction zeros = new QFunction("zeros(...)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		// Construct an empty result image. It will be filled in by bilateralFilter().
		QValue denoisedImageCube = zeros.apply(noisyImageCube.size());
		
		bilateralFilter.apply(noisyImageCube,
				              denoisedImageCube,
				              new QValue(params.r),
				              new QValue(-params.h));  // params.h is actually the negative of h (to avoid negative values in the user interface)
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
