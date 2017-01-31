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
		QFunction applyBilateralFilter = QuasarTools.loadDenoiseFunction("bilateral_filter.q",
				                                                         "apply_bilateral_filter(mat,cube,int,int)");

		QFunction computeBilateralFilter = new QFunction("compute_bilateral_filter(cube,int,int,scalar,scalar,scalar,scalar)");
		
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue bf = computeBilateralFilter.apply(noisyImageCube,
				                                 new QValue(BilateralParams.nx),
				                                 new QValue(BilateralParams.ny),
				                                 new QValue(params.alpha),
				                                 new QValue(params.beta),
				                                 new QValue(BilateralParams.euclDist),
				                                 new QValue(BilateralParams.normalize));

		QValue denoisedImageCube = applyBilateralFilter.apply(noisyImageCube,
    				                                          bf,
				                                              new QValue(BilateralParams.nx),
				                                              new QValue(BilateralParams.ny));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}
