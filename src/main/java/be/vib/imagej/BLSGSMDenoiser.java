package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

class BLSGSMDenoiser extends Denoiser
{
	private final BLSGSMParams params;
	
	public BLSGSMDenoiser(BLSGSMParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call()
	{
		QFunction blsgsm = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\blsgsm.q",
				                               "denoise_image_blsgsm(mat,scalar,string,int,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		
		QValue result = blsgsm.apply(imageCube,
				                     new QValue(params.sigma),
				                     new QValue(BLSGSMParams.sparsityTrf),
				                     new QValue(BLSGSMParams.J),
				                     new QValue(BLSGSMParams.K));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}
