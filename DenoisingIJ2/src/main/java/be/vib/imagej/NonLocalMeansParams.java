package be.vib.imagej;

public class NonLocalMeansParams
{
	public float sigma;
	public int searchWindow;
	public int halfBlockSize;
	
	// TODO: pick sensible parameter ranges
	//       (some values probably depend on one another though, like sigma and block size...)
	
	public static final float sigmaMin = 0.01f;
	public static final float sigmaMax = 100.0f;
	
	public static final int searchWindowMin = 1;
	public static final int searchWindowMax = 20;
	
	public static final int halfBlockSizeMin = 1;
	public static final int halfBlockSizeMax = 10;

	public NonLocalMeansParams()
	{
		sigma = 25.0f;
		searchWindow = 11;
		halfBlockSize = 3;
	}
	
	public NonLocalMeansParams(NonLocalMeansParams other)
	{
		this.sigma = other.sigma;
		this.searchWindow = other.searchWindow;
		this.halfBlockSize = other.halfBlockSize;
	}
}