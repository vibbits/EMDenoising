package be.vib.imagej;

import java.nio.file.NoSuchFileException;

import be.vib.bits.QFunction;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;
import ij.process.ImageProcessor;

public class WaveletThresholdingDenoiser extends Denoiser
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
		QFunction waveletThresholding = new QFunction("wav_denoise(mat,int,mat,mat,string,scalar)");
				
		QValue noisyImageCube = QuasarTools.newCubeFromImage(image);
		
		QValue w1 = QValue.readhostVariable("filtercoeff_farras");          // wavelet for the first scale (a 2x10 matrix)
		QValue w2 = QValue.readhostVariable("filtercoeff_selcw").at(3, 1);  // wavelet for the other scales (a 2x12 matrix)

		float r = QuasarTools.bitRange(image);
		
		QUtils.inplaceDivide(noisyImageCube, r);  // scale pixels values from [0, 255] or [0, 65535] down to [0, 1]

		QValue denoisedImageCube = waveletThresholding.apply(noisyImageCube,
							                                 new QValue(WaveletThresholdingParams.J),
							                                 w1,
							                                 w2,
							                                 new QValue(WaveletThresholdingParams.thresholdType),
							                                 new QValue(params.threshold));
		
		noisyImageCube.dispose();

		QUtils.inplaceMultiply(denoisedImageCube, r); // scale pixels values back to [0, 255] or [0, 65535]

		ImageProcessor denoisedImage = QuasarTools.newImageFromCube(image, denoisedImageCube);

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