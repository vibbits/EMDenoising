package be.vib.imagej;

class NoOpDenoiser extends Denoiser
{		
	public NoOpDenoiser()
	{
		super();
	}
	
	@Override
	public LinearImage call()
	{						
		return new LinearImage(image.width, image.height, image.pixels.clone());
	}
}