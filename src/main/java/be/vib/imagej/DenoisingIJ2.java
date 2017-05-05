package be.vib.imagej;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.plugin.frame.Recorder;

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
		System.out.println("DenoisingIJ2.run() begin");
		log.info("VIB Electron Microscopy Image Restoration plugin v1.0.0");

		// A little experiment with the macro recorder 
		// TODO: read http://imagej.net/PlugIn_Design_Guidelines
		if (Recorder.record)
		{
			String command = "// This is the EM Denoising plugin trying out the macro recorder...\n";
			Recorder.recordString(command);
		}
		
		Wizard wizard = DenoisingWizardSingleton.getInstance();
		wizard.pack();
		wizard.moveToMiddleOfScreen();
		wizard.setVisible(true);
		
		// After displaying the denoising wizard the ImageJ plugin run() method finishes immediately,
		// but the wizard is still visible and active.
		System.out.println("DenoisingIJ2.run() end");
	}
}
	