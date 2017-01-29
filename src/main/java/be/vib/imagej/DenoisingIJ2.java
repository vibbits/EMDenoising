package be.vib.imagej;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.frame.Recorder;

// Windows 10
// ----------
// Installation:
//    copy JavaQuasarBridge.jar   to e:\Fiji.app\plugins
//    copy JavaQuasarBridge.dll   to e:\Fiji.app\lib\win64
//    copy VIBDenoising-0.0.1.jar to e:\Fiji.app\plugins
// Running:
//    e:\Fiji.app\ImageJ-win64.exe
// (or "e:\Fiji.app\ImageJ-win64.exe --console" to see output in a text console)

// Aside: to compile .q to .qlib
//    "e:\Program Files\Quasar\Quasar.exe" --make_lib --optimize --gpu nlmeans_denoising_stillimages.q


@Plugin(type = Command.class, menuPath = "VIB>EM Denoising") // "Plugins>EM Denoising" is a more standard location for the plugin, but harder too access...
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;
    
    private Wizard wizard;
	
	static
	{		
		System.out.println("About to load JavaQuasarBridge dynamic library");
		System.loadLibrary("JavaQuasarBridge"); // loads JavaQuasarBridge.dll (on Windows)
		System.out.println("JavaQuasarBridge dynamic library loaded.");
	}
	
	private Wizard createWizard()
	{
		WizardModel model = new WizardModel();
		
		Wizard wizard = new Wizard("EM Denoising wizard");
		
		WizardPage pageInitialization = new WizardPageInitializeQuasar(wizard, model, "Initialization");
		WizardPage pageROI = new WizardPageROI(wizard, model, "Select Image and ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(wizard, model, "Choose Denoising Algorithm");  // Later: "Choose Denoising Protocol" ?
		WizardPage pageDenoise = new WizardPageDenoise(wizard, model, "Denoise");
		
		wizard.addPage(pageInitialization);
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);
		
		return wizard;
	}
	
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
		
		// FIXME: if there is already an EM Denoising wizard open,
		//        then do not start a new one, but instead make sure the exising wizard
		//        window is moved to the front.
		// PROBLEM: how de we detect this? Every invocation of EM Denoising from the Fiji menu
		//          will create a new DenoisingIJ2 instance.
		// SOLUTION: Wizard needs to be a singleton
				
		wizard = createWizard();
		wizard.pack();
		wizard.setVisible(true); // triggers creation of QHost, so need to go before *anything* else that uses the JavaQuasarBridge,
		
		// After displaying the denoising wizard the ImageJ plugin run() method finishes immediately,
		// but the wizard is still visible and active.
		System.out.println("DenoisingIJ2.run() end");
	}
	
}
	