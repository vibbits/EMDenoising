package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class GaussianDenoiser extends Denoiser
{
	public GaussianDenoiser(GaussianParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{		
		QFunction gaussian = new QFunction("gaussian_filter(mat,scalar,int,string)");
		
		QValue noisyImageCube = ImageUtils.newCubeFromImage(image);
		
		float r = ImageUtils.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		GaussianParams params = (GaussianParams)this.params;

		QValue denoisedImageCube = gaussian.apply(noisyImageCube,
							                      new QValue(params.sigma),
							                      new QValue(0),
							                      new QValue("mirror"));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = ImageUtils.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
