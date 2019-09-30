package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class GaussianDenoiser extends Denoiser
{
	public GaussianDenoiser(GaussianParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call()
	{		
		QFunction gaussian = new QFunction("gaussian_filter(mat,scalar,int,string)");
		
		final boolean byteRange = false; // normalize to/from [0,1]
		
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
		
		GaussianParams params = (GaussianParams)this.params;

		QValue denoisedImageCube = gaussian.apply(noisyImageCube,
							                      new QValue(params.sigma),
							                      new QValue(0),
							                      new QValue("mirror"));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);
		
		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
