package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
//import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

class NonLocalMeansSCDenoiser extends Denoiser
{
	private final NonLocalMeansSCParams params;
	
	public NonLocalMeansSCDenoiser(NonLocalMeansSCParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call() throws NoSuchFileException
	{		
		QFunction nlmeansSC = loadDenoiseFunction("nlmeans_scd.q",  // FIXME use _sc.qlib?
                                                  "denoise_nlmeans_sc(mat,int,int,scalar,scalar,scalar,mat)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		QValue result = nlmeansSC.apply(imageCube,
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
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}