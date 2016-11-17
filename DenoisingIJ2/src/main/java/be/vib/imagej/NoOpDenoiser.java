package be.vib.imagej;

class NoOpDenoiser extends Denoiser
{		
	public NoOpDenoiser(LinearImage image)
	{
		super(image);
	}
	
	@Override
	public byte[] call()
	{						
		return image.pixels.clone();
	}
}