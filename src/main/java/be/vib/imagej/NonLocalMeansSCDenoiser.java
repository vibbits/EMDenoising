package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class NonLocalMeansSCDenoiser extends Denoiser
{
	private final NonLocalMeansSCParams params;
	
	public NonLocalMeansSCDenoiser(NonLocalMeansSCParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{		
		QFunction nlmeansSC = QuasarTools.loadDenoiseFunction("nlmeans_scd.q",
                                                              "denoise_nlmeans_sc(mat,int,int,scalar,scalar,scalar,mat)");

		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue denoisedImageCube = nlmeansSC.apply(noisyImageCube,
							                       new QValue(NonLocalMeansSCParams.halfSearchSize),
							                       new QValue(NonLocalMeansSCParams.halfBlockSize),
							                       new QValue(params.h),
							                       new QValue(NonLocalMeansSCParams.sigma0),
							                       new QValue(NonLocalMeansSCParams.alpha),
							                       new QValue(NonLocalMeansSCParams.emCorrFilterInv));
		
//		QFunction print = new QFunction("print(...)");
//		System.out.println("print exists? " + QHost.functionExists("print"));
//		print.apply(new QValue(NonLocalMeansSCParams.emCorrFilterInv));
//
//		QFunction imwrite = new QFunction("imwrite(string,cube)");
//		System.out.println("imwrite exists? " + QHost.functionExists("imwrite"));
//		imwrite.apply(new QValue("E:\\out.tif"), result);
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}