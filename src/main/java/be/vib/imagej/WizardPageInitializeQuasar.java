package be.vib.imagej;

import javax.swing.JLabel;

public class WizardPageInitializeQuasar extends WizardPage
{
	private boolean initialized = false;
	private JLabel statusLabel;
	
	public WizardPageInitializeQuasar(Wizard wizard, WizardModel model, String name)
	{
		// Note: it seems this constructor is not run in the Java Event Dispatch Thread.

		super(wizard, model, name);
		buildUI();
	}
	
	private void buildUI()
	{
		statusLabel = new JLabel("Preparing the graphics card for denoising calculations...");
		add(statusLabel);
	}
	
	private void initializeQuasar()
	{
		Runnable whenDone = () -> {
			initialized = true;
			statusLabel.setText("The graphics card is ready for denoising calculations.");
			wizard.updateButtons();
		};

		String engine = Preferences.getQuasarEngine();
		QuasarInitializationSwingWorker worker = new QuasarInitializationSwingWorker(engine, whenDone);
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
