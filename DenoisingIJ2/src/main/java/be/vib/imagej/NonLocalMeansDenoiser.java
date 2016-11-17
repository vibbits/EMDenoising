package be.vib.imagej;

import be.vib.bits.QFunction;
import be.vib.bits.QHost;
import be.vib.bits.QUtils;
import be.vib.bits.QValue;

class NonLocalMeansDenoiser extends Denoiser
{
	final NonLocalMeansParams params;
	
	public NonLocalMeansDenoiser(LinearImage image, NonLocalMeansParams params)
	{
		super(image);
		this.params = params;
	}
	
	@Override
	public byte[] call()
	{		
		if (!QHost.functionExists("denoise_nlmeans"))
		{
			// Lazy loading of the source module for this denoising function.
			// Once it is loaded it will persist in the Quasar host
			// even beyond the lifetime of this NonLocalMeansDenoiser object.
			QHost.loadSourceModule("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\nlmeans_denoising_stillimages.q");
		}
		
		assert(QHost.functionExists("denoise_nlmeans"));
		
		QFunction nlmeans = new QFunction("denoise_nlmeans(cube,scalar,int,ivec2,int,int)");

		int[] halfBlockSize = { params.halfBlockSize, params.halfBlockSize };
		QValue imageCube = QUtils.newCubeFromGrayscaleArray(image.width, image.height, image.pixels);
		QValue result = nlmeans.apply(imageCube,
				                      new QValue(params.sigma),
				                      new QValue(params.searchWindow),
				                      new QValue(halfBlockSize),
				                      new QValue(0),
				                      new QValue(0));
		
		byte[] outputPixels = QUtils.newGrayscaleArrayFromCube(image.width, image.height, result);
		
		result.delete();
		imageCube.delete();		
		
		return outputPixels;
	}
}