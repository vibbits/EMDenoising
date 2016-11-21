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


@Plugin(type = Command.class, menuPath = "Plugins>EM Denoising")
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;

	// If no image is active, the Fiji plugin framework will automatically
	// issue a terse error message.
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
		model.range = ImageRange.makeCurrentSliceRange(model.imagePlus);
		// TODO: we have to be careful here: the user may close the image window after the wizard was opened. It looks like that means the ImagePlus becomes invalid. Correct??
		
		wizard = createWizard(model);
		wizard.pack();
		wizard.setVisible(true); // triggers creation of QHost, so need to go before *anything* else that uses the JavaQuasarBridge,
		
		System.out.println("plugin run() end");
	}
	
}
	