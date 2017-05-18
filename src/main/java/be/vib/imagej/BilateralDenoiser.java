package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
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
		QFunction bilateralFilter = new QFunction("bilateral_filter_denoise(cube,cube,int,scalar)");  // Fast histogram based bilateral filter
		
		QFunction zeros = new QFunction("zeros(...)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		float r = QuasarTools.bitRange(image);
		
		// The histogram based bilateral filter code in Quasar expects pixel values in the [0, 255] range.
		// If we have a 16-bit image, we scale the values down to 8-bit for now - this probably loses precision.
		// TODO: natively support 16 bit images the Quasar bilateral filter implementation
		if (r != 255)
		{
			QUtils.inplaceMultiply(noisyImageCube, 255.0f / r);
		}

		// Construct an empty result image. It will be filled in by bilateralFilter().
		QValue denoisedImageCube = zeros.apply(noisyImageCube.size());
		
		bilateralFilter.apply(noisyImageCube,
				              denoisedImageCube,
				              new QValue(params.r),
				              new QValue(-params.h));  // params.h is actually the negative of h (to avoid negative values in the user interface)
		
		noisyImageCube.dispose();
		
		if (r != 255)
		{
			QUtils.inplaceMultiply(denoisedImageCube, r / 255.0f);
		}

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
