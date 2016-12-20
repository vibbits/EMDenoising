package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class BilateralDenoiser extends Denoiser
{
	private final BilateralParams params;
	
	public BilateralDenoiser(BilateralParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{
		QFunction applyBilateralFilter = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\bilateral_filter.q",
				                                             "apply_bilateral_filter(mat,cube,int,int)");
		QFunction computeBilateralFilter = new QFunction("compute_bilateral_filter(cube,int,int,scalar,scalar,scalar,scalar)");
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
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
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.dispose();
		imageCube.dispose();		
		
		return outputPixels;
	}
}
