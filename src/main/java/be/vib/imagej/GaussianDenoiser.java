package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class GaussianDenoiser extends Denoiser
{
	private final GaussianParams params;
	
	public GaussianDenoiser(GaussianParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{
		QFunction gaussian = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\gaussian_filter.q",
				                                 "gaussian_filter(mat,scalar,int,string)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
		QValue result = gaussian.apply(imageCube,
				                       new QValue(params.sigma),
				                       new QValue(0),
				                       new QValue("mirror"));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();		
		
		return outputPixels;
	}
}
