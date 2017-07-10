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
		Wizard wizard = new Wizard("EM Denoising", new WizardModel());

		WizardPage[] pages = { new WizardPageInitializeQuasar(wizard, "Initialization"),
		                       new WizardPageROI(wizard, "Select Image and ROI"),
		                       new WizardPageDenoisingAlgorithm(wizard, "Choose Denoising Algorithm"),
	                           new WizardPageDenoise(wizard, "Denoise") };
		
		wizard.build(pages);
		wizard.pack();
		wizard.moveToMiddleOfScreen();
				
		return wizard;
	}	
}
