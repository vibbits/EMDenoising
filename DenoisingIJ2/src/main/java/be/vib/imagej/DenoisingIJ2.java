package be.vib.imagej;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.plugin.frame.Recorder;

// Starting Fiji:
// e:\Fiji.app\ImageJ-win64.exe --console --class-path e:\Fiji.app\plugins\vib_denoising_ij2-1.0.0-amd64-Windows-msvc-jni.nar
// (the --console triggers and error but is needed anyway to be able to see the stderr output from the C++ side)
//
// Building:
//    mvn
// This will compile and copy the .jar and .nar into the Fiji installation. It will *not* copy any other dependencies alone (which is intentional).
//
// We do not set the "imagej.app.directory" property in our pom.xml because otherwise when running "mvn" the "copy-jars" phase
// of the "imagej-maven-plugin" will copy our dependencies (such as scijava-common.jar) into the Fiji distribution.
// This may break the installation if the versions we are copying into the installation are incompatible with some other jars.
// (See also http://imagej.1557.x6.nabble.com/Eclipse-Maven-workflow-depencies-copied-to-Fiji-app-directory-td5005804.html)
//
// Be careful: after updating Quasar, we probably have to copy the corresponding .h and .cpp files into our source folders
// to avoid that compile time and runtime versions of Quasar are different. (Or is that irrelevant?)
//
// TODO:
// - Let Maven pick up the Quasar include files from the Quasar installation instead of the copies we placed in src\main\include. Also let it pick up quasar_host.cpp
//   from the Quasar installation instead of our copy.
//
// Preliminary timings on my VIB laptop:
// 2013_11_28_arabidopsis_root_0086.tiff (3896x3896 pixels, 8 bit/pixel), NLMS(sigma 25, searchwin=11, halfblocksize=3, novect, noklt)
// Times including moving data from Java over JNI to C++ and Quasar (and to GPU) and all the way back.
//   cpu: Denoising time: 320383 ms (47.377 kpix/s)
//   cuda: Denoising time: 13860 ms (1095.153 kpix/s) = 23x faster than Quasar utilising the cpu
//
// Compile .q to .qlib
// E:\git\DenoisingIJ2Repository\DenoisingIJ2\src\main\resources\quasar>"e:\Program Files\Quasar\Quasar.exe" --make_lib --optimize --gpu nlmeans_denoising_stillimages.q


@Plugin(type = Command.class, menuPath = "Plugins>EM Denoising")
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;

	// If no image is active, the Fiji plugin framework will automatically
	// issue an error message.
    @Parameter
    private ImagePlus imp;
    
    private WizardModel model;
    
    private Wizard wizard;
	
	static
	{
        NarSystem.loadLibrary();
		System.out.println("Native library loaded");
	}
	
	private Wizard createWizard(WizardModel model)
	{
		WizardPage pageROI = new WizardPageROI(model, "Select ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(model, "Select Denoising Algorithm");
		WizardPage pageDenoise = new WizardPageDenoise(model, "Denoise");
		
		Wizard wizard = new Wizard("EM Denoising wizard");
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);
		
		return wizard;
	}
	
	@Override
	public void run() 
	{
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
		//imp.c
		model.range = ImageRange.makeCurrentSliceRange(model.imagePlus);
		// TODO: we have to be careful here: the user may close the image window after the wizard was opened. It looks like that means the ImagePlus becomes invalid. Correct??
		
		wizard = createWizard(model);
		wizard.setVisible(true);
	}
	
}
	