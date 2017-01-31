package be.vib.imagej;

import ij.process.ImageProcessor;

class NoOpDenoiser extends Denoiser
{		
	public NoOpDenoiser()
	{
		super();
	}
	
	@Override
	public ImageProcessor call()
	{						
		return image.duplicate();
	}
}