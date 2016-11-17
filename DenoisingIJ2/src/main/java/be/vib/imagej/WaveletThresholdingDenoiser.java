package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;
import be.vib.bits.QRange;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class WaveletThresholdingDenoiser extends Denoiser
{
	final WaveletThresholdingParams params;
	
	public WaveletThresholdingDenoiser(LinearImage image, WaveletThresholdingParams params)
	{
		super(image);
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{		
		if (!QHost.functionExists("wav_denoise"))
		{
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this WaveletThresholdingDenoiser object.
			QHost.loadSourceModule("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\wavelet_thresholding.q");
		}
		
		assert(QHost.functionExists("wav_denoise"));
		
		QFunction waveletThresholding = new QFunction("wav_denoise(mat,scalar,int,mat,mat,string,scalar)");
		
		QFunction print = new QFunction("print(...)");
		
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		
		QValue w1 = QValue.readhostVariable("filtercoeff_farras");          // wavelet for the first scale (a 2x10 matrix)
		QValue w2 = QValue.readhostVariable("filtercoeff_selcw").at(3, 1);  // wavelet for the other scales (a 2x12 matrix)
		
//		QFunction imwrite = new QFunction("imwrite(string,mat)");
//		System.out.println("imwrite mat exists? " + QHost.functionExists("imwrite"));
//		imwrite.apply(new QValue("e:\\imagecube.tif"), imageCube);
//		System.out.println("saved image cube");
		
		// FIXME: the wavelet thresholding code does not support non-power of two size images!
		
		QValue result = waveletThresholding.apply(imageCube,
				                                  new QValue(20.0f),  // = sigma // TODO: should this be a parameter the user can choose? is related to alpha
				                                  new QValue(6),      // = J = number of scales
				                                  w1,
				                                  w2,
				                                  new QValue("soft"), // use soft thresholding ("hard" is for hard thresholding)
				                                  new QValue(params.alpha));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();
		
		return outputPixels;
	}
}