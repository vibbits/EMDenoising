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
		QFunction blsgsm = new QFunction("denoise_image_blsgsm(mat,scalar,string,int,int)");

		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue denoisedImageCube = blsgsm.apply(noisyImageCube,
				                     new QValue(params.sigma),
				                     new QValue(BLSGSMParams.sparsityTrf),
				                     new QValue(BLSGSMParams.J),
				                     new QValue(BLSGSMParams.K));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
