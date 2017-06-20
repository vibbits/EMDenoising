package be.vib.imagej;

// A Bill Pugh singleton for the denoising wizard.
public class DenoisingWizardSingleton
{
    private static String tempFolder;
	
	static
	{
		tempFolder = QuasarTools.loadQuasarBridge();
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
