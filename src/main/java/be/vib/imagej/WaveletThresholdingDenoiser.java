package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

class WaveletThresholdingDenoiser extends Denoiser
{
	private final WaveletThresholdingParams params;
	
	public WaveletThresholdingDenoiser(WaveletThresholdingParams params)
	{
		super();
		this.params = params;
	}
	
	@Override
	public ImageProcessor call() throws NoSuchFileException
	{		
		QFunction waveletThresholding = QuasarTools.loadDenoiseFunction("wavelet_thresholding.q",
                                                                        "wav_denoise(mat,scalar,int,mat,mat,string,scalar)");
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue w1 = QValue.readhostVariable("filtercoeff_farras");          // wavelet for the first scale (a 2x10 matrix)
		QValue w2 = QValue.readhostVariable("filtercoeff_selcw").at(3, 1);  // wavelet for the other scales (a 2x12 matrix)

		QValue denoisedImageCube = waveletThresholding.apply(noisyImageCube,
							                                 new QValue(WaveletThresholdingParams.sigma),
							                                 new QValue(WaveletThresholdingParams.J),
							                                 w1,
							                                 w2,
							                                 new QValue(WaveletThresholdingParams.thresholding),
							                                 new QValue(params.alpha));
		
		noisyImageCube.dispose();

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

		denoisedImageCube.dispose();

		return denoisedImage;
	}
}