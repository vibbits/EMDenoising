package be.vib.imagej;

// The preferences can be set as JVM defines on the ImageJ command line.
// For example, to choose the CPU compute engine instead of the CUDA default,
// start ImageJ like this:

public class Preferences
{
	// Returns the Quasar compute engine that will be used to initialize the Quasar host.
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
