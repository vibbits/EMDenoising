package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class BLSGSMDenoiser extends Denoiser
{
	private final BLSGSMParams params;
	
	public BLSGSMDenoiser(BLSGSMParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public LinearImage call()
	{
		QFunction blsgsm = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\blsgsm.q",
				                               "denoise_image_blsgsm(mat,scalar,string,int,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
		QValue result = blsgsm.apply(imageCube,
				                     new QValue(params.sigma),
				                     new QValue(BLSGSMParams.sparsityTrf),
				                     new QValue(BLSGSMParams.J),
				                     new QValue(BLSGSMParams.K));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new LinearImage(image.width, image.height, outputPixels);
	}
}
