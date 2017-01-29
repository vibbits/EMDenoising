package be.vib.imagej;

import ij.process.ByteProcessor;

class NoOpDenoiser extends Denoiser
{		
	public NoOpDenoiser()
	{
		super();
	}
	
	@Override
	public ByteProcessor call()
	{						
		return (ByteProcessor)image.duplicate();
	}
}