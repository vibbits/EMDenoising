package be.vib.imagej;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// Windows 10
// ----------
// Installation:
//    copy JavaQuasarBridge.jar to e:\Fiji.app\plugins
//    copy EM_Denoising-0.0.1.jar to e:\Fiji.app\plugins
//
// Prerequisites:
//    Fiji
//    Quasar (with a license, for now - we will eventually embed the Quasar runtime in the JavaQuasarBridge jar)
//
// Running:
//    e:\Fiji.app\ImageJ-win64.exe
// (or "e:\Fiji.app\ImageJ-win64.exe --console" to see output in a text console)
//
// Aside: to compile a .q to .qlib
//    "e:\Program Files\Quasar\Quasar.exe" --make_lib (--optimize) --gpu nlmeans_denoising_stillimages.q


@Plugin(type = Command.class, menuPath = "Plugins>EM Denoising")
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;
	
	@Override
	public void run() 
	{
		log.info("VIB Electron Microscopy Image Restoration plugin v1.0.0");

		// A little experiment with the macro recorder 
		// See e.g. http://imagej.net/PlugIn_Design_Guidelines
//		if (Recorder.record)
//		{
//			Recorder.recordString("// Start the machinery for GPU-acceleration\n");			
//			Recorder.recordString("call('be.vib.imagej.DenoisingIJ2.startQuasar', 'cuda');\n");
//			
//			Recorder.recordString("// Test\n");			
//			Recorder.recordString("call('be.vib.imagej.DenoisingIJ2.testMethod2', '2', '3');\n");
//		}
		
		Wizard wizard = DenoisingWizardSingleton.getInstance();
		wizard.pack();
		wizard.moveToMiddleOfScreen();
		wizard.setVisible(true);
		
		// After displaying the denoising wizard the ImageJ plugin run() method finishes immediately,
		// but the wizard is still visible and active.
	}
	
//	public static String startQuasar(String engine)
//	{
//		System.out.println("startQuasar");
//		try
//		{
//			QuasarTools.startQuasar(engine, false);
//			System.out.println("  startQuasar OK");
//			return "1";
//		}
//		catch (InterruptedException | ExecutionException e)
//		{
//			System.out.println("  startQuasar exception");
//			return "0";
//		}
//	}
//
//	public static String testMethod2(String arg1, String arg2)
//	{
//		System.out.println("testMethod2 received argument arg1=" + arg1 + " arg2="+ arg2 + "and will return the string 123.");
//		return "123";
//	}
}
	