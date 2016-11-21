package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class BLSGSMDenoiser extends Denoiser
{
	final BLSGSMParams params;
	
	public BLSGSMDenoiser(LinearImage image, BLSGSMParams params)
	{
		super(image);
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{
		QFunction blsgsm = loadDenoiseFunction("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\blsgsm.q",
				                               "denoise_image_blsgsm(mat,scalar,string,int,int)");

		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
	    int J = 4; // number of scales
	    int K = 8; // number of orientations
		
		QValue result = blsgsm.apply(imageCube,
				                     new QValue(params.sigma),
				                     new QValue("dtcwt"),
				                     new QValue(J),
				                     new QValue(K));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();		
		
		return outputPixels;
	}
}
