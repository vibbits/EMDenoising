package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class GaussianDenoiser extends Denoiser
{
	private final GaussianParams params;
	
	public GaussianDenoiser(GaussianParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{
		QFunction gaussian = QuasarTools.loadDenoiseFunction("gaussian_filter.q",
				                                             "gaussian_filter(mat,scalar,int,string)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue denoisedImageCube = gaussian.apply(noisyImageCube,
							                      new QValue(params.sigma),
							                      new QValue(0),
							                      new QValue("mirror"));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
