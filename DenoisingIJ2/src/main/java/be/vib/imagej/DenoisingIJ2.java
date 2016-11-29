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

	// If no image is active, the Fiji plugin framework will automatically
	// issue a terse error message.     // FIXME: this is not a very friendly message - remove the @Parameter and move handling of no image to the WizardPageROI instead (setting model.range then needs to move there too).
    @Parameter
    private ImagePlus imp;
    
    private WizardModel model;
    
    private Wizard wizard;
	
	static
	{		
		System.out.println("About to load JavaQuasarBridge dynamic library");
		System.loadLibrary("JavaQuasarBridge"); // loads JavaQuasarBridge.dll (on Windows)
		System.out.println("JavaQuasarBridge loaded.");
	}
	
	private Wizard createWizard(WizardModel model)
	{
		Wizard wizard = new Wizard("EM Denoising wizard");
		
		WizardPage pageROI = new WizardPageROI(wizard, model, "Select ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(wizard, model, "Select Denoising Algorithm");
		WizardPage pageDenoise = new WizardPageDenoise(wizard, model, "Denoise");
		
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);
		
		return wizard;
	}
	
	@Override
	public void run() 
	{
		System.out.println("plugin run() begin");
		
		// FIXME: since there can be only one Quasar host running at any time,
		//        we must avoid creating the denoising wizard more than once.
		//        So if one is already open, we cannot start another one. 
		//        For now: do not allow more than one denoising wizard.

		// A little experiment with the macro recorder 
		// TODO: read http://imagej.net/PlugIn_Design_Guidelines
		if (Recorder.record)
		{
			String command = "// This is the EM Denoising plugin trying out the macro recorder...\n";
			Recorder.recordString(command);
		}
		
		log.info("DenoisingIJ2.run() called.");		
        log.info("Image: " + imp.getTitle());
        log.info("Height:" + imp.getHeight() + " width:" + imp.getWidth() + " channels:" + imp.getNChannels() + " slices:" + imp.getNSlices() + " frames:" + imp.getNFrames());
        log.info("Bytes per pixel:" + imp.getBytesPerPixel());
        log.info("ROI: " + imp.getRoi());

//		if (imp.getBytesPerPixel() != 1)
//		{
//			IJ.showMessage("For now only 8-bit images are supported. This image has " + imp.getBytesPerPixel() + " bytes per pixel. Please change the bit depth to 8 bits per pixel. In Fiji: Image > Type > 8-bit.");
//			// TODO: support 16-bit images, they are common for EM
//			return;
//		}
		
		model = new WizardModel();
		model.imagePlus = imp;
		model.range = ImageRange.makeAllSlicesRange(model.imagePlus);
		// TODO: we have to be careful here: the user may close the image window after the wizard was opened.
		//       It looks like that means the ImagePlus becomes invalid. Correct??
		//       How do we handle that situation? Maybe at any time when the image disappears, move the wizard page to the first page where it asks for image, 8 bit, ROI selected.
		
		wizard = createWizard(model);
		wizard.pack();
		wizard.setVisible(true); // triggers creation of QHost, so need to go before *anything* else that uses the JavaQuasarBridge,
		
		// After displaying the denoising wizard the ImageJ plugin run() method finishes immediately,
		// but the wizard is still visible and active.
		System.out.println("plugin run() end");
	}
	
}
	