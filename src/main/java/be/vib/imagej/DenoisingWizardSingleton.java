package be.vib.imagej;

import java.io.IOException;
import java.nio.file.Files;
import be.vib.bits.JavaQuasarBridge;

// A Bill Pugh singleton for the denoising wizard.
public class DenoisingWizardSingleton
{
    private static String tempFolder;
	
	static
	{		
		try
		{
			System.out.println("About to load JavaQuasarBridge dynamic library");
			tempFolder = Files.createTempDirectory("vib_em_denoising_").toString();
			boolean useEmbeddedQuasar = false;  // FIXME: should be true once we have the Quasar runtime distributions and embed them in the jar
			JavaQuasarBridge.loadLibrary(tempFolder, useEmbeddedQuasar);
			System.out.println("JavaQuasarBridge dynamic library loaded.");
		}
		catch (ClassNotFoundException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private DenoisingWizardSingleton()
	{
	}
	
	public static Wizard getInstance()
	{
		return SingletonHelper.INSTANCE;
	}

	private static class SingletonHelper
	{
		private static final Wizard INSTANCE = createWizard();
	}
	
	private static Wizard createWizard()
	{
		WizardModel model = new WizardModel();
		
		Wizard wizard = new Wizard("EM Denoising");
		
		WizardPage pageInitialization = new WizardPageInitializeQuasar(wizard, model, "Initialization", tempFolder);
		WizardPage pageROI = new WizardPageROI(wizard, model, "Select Image and ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(wizard, model, "Choose Denoising Algorithm");  // Later: "Choose Denoising Protocol" ?
		WizardPage pageDenoise = new WizardPageDenoise(wizard, model, "Denoise");
		
		wizard.addPage(pageInitialization);
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);
		
		wizard.start();
		
		return wizard;
	}	
}
