package be.vib.imagej;

class NoOpDenoiser extends Denoiser
{		
	public NoOpDenoiser()
	{
		super();
	}
	
	@Override
	public byte[] call()
	{						
		return image.pixels.clone();
	}
}