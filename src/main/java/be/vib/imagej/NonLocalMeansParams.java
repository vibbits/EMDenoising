package be.vib.imagej;

public class NonLocalMeansParams
{
	public float h;
	public int halfSearchSize;
	public int halfBlockSize;
	
	public static final float hMin = 0.01f;
	public static final float hMax = 200.0f;
	
	public static final int halfSearchSizeMin = 1;
	public static final int halfSearchSizeMax = 20;
	
	public static final int halfBlockSizeMin = 1;
	public static final int halfBlockSizeMax = 10;

	public NonLocalMeansParams()
	{
		h = 140.0f;
		halfSearchSize = 5;
		halfBlockSize = 4;
	}
	
	public NonLocalMeansParams(NonLocalMeansParams other)
	{
		this.h = other.h;
		this.halfSearchSize = other.halfSearchSize;
		this.halfBlockSize = other.halfBlockSize;
	}
	
	@Override
	public String toString()
	{
		return "h " + h + "; half search size " + halfSearchSize + "; half block size " + halfBlockSize;
	}
}