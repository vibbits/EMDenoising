package be.vib.imagej;

import javax.swing.JLabel;

public class WizardPageInitializeQuasar extends WizardPage
{
	private boolean initialized = false;
	private JLabel statusLabel;
	
	public WizardPageInitializeQuasar(Wizard wizard, String name)
	{
		// Note: it seems this constructor is not run on the Java Event Dispatch Thread.

		super(wizard, name);
		buildUI();
	}
	
	private void buildUI()
	{
		statusLabel = new JLabel("Preparing the graphics card for denoising calculations...");
		add(statusLabel);
	}
	
	private void initializeQuasar()
	{
		Runnable onSuccess = () -> {
			initialized = true;
			statusLabel.setText("The graphics card is ready for denoising calculations.");
			wizard.updateButtons();
		};
		
		Runnable onFailure = () -> {
			initialized = false;
			statusLabel.setText("<html><center>The graphics card is not ready for denoising calculations. Failed to initialize Quasar.<br><br>" +
			                    "If you did not install Quasar yet, then please consult the plugin's installation instructions and do so first.<br><br>" + 
					            "If you did install Quasar and this problem persists, then please contact the plugin maintainer for help.</center></html>");
			wizard.updateButtons();
		};

		String engine = Preferences.getQuasarEngine();
		QuasarInitializationSwingWorker worker = new QuasarInitializationSwingWorker(engine, onSuccess, onFailure);
		worker.execute();		
	}
	
	@Override
	public void goingToNextPage() 
	{
		assert(initialized);
	}
	
	@Override
	public void goingToPreviousPage()
	{
		assert(false);
	}

	@Override
	public void arriveFromNextPage() 
	{
		assert(initialized);
	}
	
	@Override
	public void arriveFromPreviousPage()
	{
		if (!initialized)
		{
			initializeQuasar();
		}
	}	
	
	@Override
	public boolean canGoToNextPage()
	{
		return initialized;
	}
}
