package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

public class BilateralDenoiser extends Denoiser
{
	private final BilateralParams params;
	
	public BilateralDenoiser(BilateralParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public LinearImage call()
	{
		QFunction applyBilateralFilter = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\bilateral_filter.q",
				                                             "apply_bilateral_filter(mat,cube,int,int)");
		
		// System.out.println("bilateral_filter.q loaded");
		
		QFunction computeBilateralFilter = new QFunction("compute_bilateral_filter(cube,int,int,scalar,scalar,scalar,scalar)");
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
		// System.out.println("before computeBilateralFilter.apply");

		QValue bf = computeBilateralFilter.apply(imageCube,
				                                 new QValue(BilateralParams.nx),
				                                 new QValue(BilateralParams.ny),
				                                 new QValue(params.alpha),
				                                 new QValue(params.beta),
				                                 new QValue(BilateralParams.euclDist),
				                                 new QValue(BilateralParams.normalize));
		
		// System.out.println("after computeBilateralFilter.apply");

		// System.out.println("before applyBilateralFilter.apply");

		QValue result = applyBilateralFilter.apply(imageCube,
    				                               bf,
				                                   new QValue(BilateralParams.nx),
				                                   new QValue(BilateralParams.ny));
		
		// System.out.println("after applyBilateralFilter.apply");

		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new LinearImage(image.width, image.height, outputPixels);
	}
}
