package be.vib.imagej;

public class QuasarInterface
{
	public native static boolean quasarInit(String deviceName);  // deviceName is "cpu" or "cuda" or ...
	public native static void quasarRelease();
	
	public native static boolean quasarLoadSource(String filepath);  // load .q file with given full path name
	public native static boolean quasarLoadBinary(String filepath); // load .q file with given full path name
	
	public native static byte[] quasarNlmeans(int width, int height, byte[] inputPixels, float sigma, int searchWindow, int halfBlockSize,  int vectorBasedFilter, int kltPostProcessing);
	// patches are (probably) 2*halfBlockSize+1 pixels large (horizontally and vertically)
	// patches are displaced over [-searchWindow, searchWindow] when searching a patch's neighborhood
}
