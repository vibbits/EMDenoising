package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class BLSGSMDenoiser extends Denoiser
{
	public BLSGSMDenoiser(BLSGSMParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction blsgsm = new QFunction("denoise_blsgsm(mat,int,int,scalar)");

		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]
				
		BLSGSMParams params = (BLSGSMParams)this.params;
		
		QValue denoisedImageCube = blsgsm.apply(noisyImageCube,
							                    new QValue(params.scales),
							                    new QValue(params.sigma));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
