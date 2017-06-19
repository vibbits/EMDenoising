package be.vib.imagej;

//import java.nio.file.Files;
//import java.nio.file.NoSuchFileException;
//import java.nio.file.Path;
//import java.nio.file.Paths;

// The preferences can be set as JVM defines on the ImageJ command line.
// For example, to choose the CPU compute engine instead of the CUDA default,
// start ImageJ like this:
//
//    e:\Fiji.app\ImageJ-win64.exe --console -Dvib.emrestoration.quasar.engine=cpu
//
// OBSOLETE:
// Additionally, it is possible to specify the location of the .q or .qlib
// for the various denoising algorithms by setting vib.emrestoration.quasar.resources
// to the folder that contains them. By default this is the plugins\Quasar subfolder
// of the Fiji installation.

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

	// Returns the folder with the Quasar source code for the different denoising algorithms.
//	static public Path getQuasarResourcesPath() throws NoSuchFileException
//	{
//		Path path = null;
//		
//		String folder = System.getProperty("vib.emrestoration.quasar.resources");
//		if (folder == null)
//		{
//			path = Paths.get(ij.Prefs.getImageJDir(), "plugins", "Quasar");
//		}
//		else
//		{
//			path = Paths.get(folder);
//		}
//		
//		if (!Files.exists(path))
//		{
//			throw new NoSuchFileException(path.toString());
//		}
//		
//		return path;		
//	}

}
