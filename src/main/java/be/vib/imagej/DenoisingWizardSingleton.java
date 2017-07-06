package be.vib.imagej;

// A Bill Pugh singleton for the denoising wizard.
public class DenoisingWizardSingleton
{
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
		
		Wizard wizard = new Wizard("EM Denoising", model);
		
		WizardPage pageInitialization = new WizardPageInitializeQuasar(wizard, "Initialization");
		WizardPage pageROI = new WizardPageROI(wizard, "Select Image and ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(wizard,"Choose Denoising Algorithm");
		WizardPage pageDenoise = new WizardPageDenoise(wizard, "Denoise");
		
		wizard.addPage(pageInitialization);
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);
		
		wizard.start();
		
		return wizard;
	}	
}
