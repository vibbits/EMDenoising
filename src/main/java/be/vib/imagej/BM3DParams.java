package be.vib.imagej;

public class BM3DParams  // TODO
{
	public int magicValue;
	public float luckyNumber;
	
	public BM3DParams()
	{
		magicValue = 42;
		luckyNumber = 13.0f;
	}
	
	public BM3DParams(BM3DParams other)
	{
		this.magicValue = other.magicValue;
		this.luckyNumber = other.luckyNumber;
	}
}