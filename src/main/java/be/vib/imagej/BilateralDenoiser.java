package be.vib.imagej;

import be.vib.bits.QFunction;
//import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

public class BilateralDenoiser extends Denoiser
{
	private final BilateralParams params;
	
	public BilateralDenoiser(BilateralParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call()
	{
		QFunction applyBilateralFilter = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\bilateral_filter.q",
				                                             "apply_bilateral_filter(mat,cube,int,int)");
		
//		System.out.println("bilateral_filter.qlib loaded");
//		System.out.println("compute_bilateral_filter exists? " + QHost.functionExists("compute_bilateral_filter"));
//		System.out.println("apply_bilateral_filter exists? " + QHost.functionExists("apply_bilateral_filter"));

		QFunction computeBilateralFilter = new QFunction("compute_bilateral_filter(cube,int,int,scalar,scalar,scalar,scalar)");
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		
		QValue bf = computeBilateralFilter.apply(imageCube,
				                                 new QValue(BilateralParams.nx),
				                                 new QValue(BilateralParams.ny),
				                                 new QValue(params.alpha),
				                                 new QValue(params.beta),
				                                 new QValue(BilateralParams.euclDist),
				                                 new QValue(BilateralParams.normalize));

		QValue result = applyBilateralFilter.apply(imageCube,
    				                               bf,
				                                   new QValue(BilateralParams.nx),
				                                   new QValue(BilateralParams.ny));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}
