package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class WaveletThresholdingDenoiser extends Denoiser
{	
	public WaveletThresholdingDenoiser(WaveletThresholdingParams params)
	{
		super(params);
	}
	
	@Override
	public ImageProcessor call()
	{		
		QFunction waveletThresholding = new QFunction("wav_denoise(mat,int,mat,mat,string,scalar)");
				
		final boolean byteRange = false;  // normalize pixel values to/from [0,1] before/after denoising
		QValue noisyImageCube = normalizer.normalize(image, byteRange);
		
		WaveletThresholdingParams params = (WaveletThresholdingParams)this.params;

		QValue w1 = QValue.readhostVariable("filtercoeff_farras");          // wavelet for the first scale (a 2x10 matrix)
		QValue w2 = QValue.readhostVariable("filtercoeff_selcw").at(3, 1);  // wavelet for the other scales (a 2x12 matrix)

		QValue denoisedImageCube = waveletThresholding.apply(noisyImageCube,
							                                 new QValue(WaveletThresholdingParams.J),
							                                 w1,
							                                 w2,
							                                 new QValue(WaveletThresholdingParams.thresholdType),
							                                 new QValue(params.threshold));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = normalizer.denormalize(image, denoisedImageCube, byteRange);
		
		denoisedImageCube.dispose();

		return denoisedImage;
	}
	
	@Override
	public int imageTileSize()
	{
		// wavelet_thresholding.q seems to be a bit memory hungry - use smaller tiles for now
		return 512;
	}
}