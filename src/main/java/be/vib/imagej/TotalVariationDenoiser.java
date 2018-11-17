package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class TotalVariationDenoiser extends Denoiser
{
	public TotalVariationDenoiser(TotalVariationParams params)
	{
		super(params);
	}

	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction total_variation_denoise = new QFunction("total_variation_denoise(mat,scalar,int,scalar)");
		
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		TotalVariationParams params = (TotalVariationParams)this.params;
		
		QValue denoisedImageCube = total_variation_denoise.apply(noisyImageCube,
                                                                 new QValue(params.lambda),
				                                                 new QValue(params.numIterations),
				                                                 new QValue(TotalVariationParams.alpha));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}