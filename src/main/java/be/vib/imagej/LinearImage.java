package be.vib.imagej;

// An 8 bit per pixel grayscale image represented as a linear array. Pixels are stored in row major order.
class LinearImage
{
	public int width;
	public int height;
	public byte[] pixels;
	
	public LinearImage(int width, int height, byte[] pixels)
	{
		this.width = width;
		this.height = height;
		this.pixels = pixels;
	}
}