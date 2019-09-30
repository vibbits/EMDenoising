package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class BLSGSMDenoiser extends Denoiser
{
	public BLSGSMDenoiser(BLSGSMParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call()
	{		
		QFunction blsgsm = new QFunction("denoise_blsgsm(mat,int,scalar)");

		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
				
		BLSGSMParams params = (BLSGSMParams)this.params;
		
		QValue denoisedImageCube = blsgsm.apply(noisyImageCube,
							                    new QValue(params.scales),
							                    new QValue(params.sigma));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
