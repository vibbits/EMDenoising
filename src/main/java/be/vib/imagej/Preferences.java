package be.vib.imagej;

// The preferences can be set as JVM defines on the ImageJ command line.
// For example, to choose the CPU compute engine instead of the CUDA default,
// start ImageJ like this:
//
//    e:\Fiji.app\ImageJ-win64.exe --console -Dvib.emrestoration.quasar.engine=cpu

public class Preferences
{
	static public String getQuasarEngine()
	{
		String engine = System.getProperty("vib.emrestoration.quasar.engine");
		if (engine == null)
		{
			engine = "cuda";
		}
		return engine;
	}

}
