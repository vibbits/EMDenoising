package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ByteProcessor;

public class GaussianDenoiser extends Denoiser
{
	private final GaussianParams params;
	
	public GaussianDenoiser(GaussianParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ByteProcessor call()
	{
		QFunction gaussian = loadDenoiseFunction("E:\\git\\bits\\bioimaging\\EMDenoising\\src\\main\\resources\\quasar\\gaussian_filter.q",
				                                 "gaussian_filter(mat,scalar,int,string)");
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.getWidth(), image.getHeight(), (byte[])image.getPixels());
		
		QValue result = gaussian.apply(imageCube,
				                       new QValue(params.sigma),
				                       new QValue(0),
				                       new QValue("mirror"));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.getWidth(), image.getHeight(), result);
		
		result.dispose();
		imageCube.dispose();		
		
		return new ByteProcessor(image.getWidth(), image.getHeight(), outputPixels);
	}
}
