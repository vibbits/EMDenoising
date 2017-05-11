package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class BLSGSMDenoiser extends Denoiser
{
	private final BLSGSMParams params;
	
	public BLSGSMDenoiser(BLSGSMParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction blsgsm = new QFunction("denoise_blsgsm(mat,int,int,scalar)");

		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue denoisedImageCube = blsgsm.apply(noisyImageCube,
							                    new QValue(BLSGSMParams.J),
							                    new QValue(BLSGSMParams.K),
							                    new QValue(params.sigma));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
