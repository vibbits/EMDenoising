package be.vib.imagej;

import javax.swing.JLabel;

public class WizardPageInitializeQuasar extends WizardPage
{
	private boolean initialized = false;
	private JLabel statusLabel;
	private String algorithmsFolder;
	
	public WizardPageInitializeQuasar(Wizard wizard, WizardModel model, String name, String algorithmsFolder)
	{
		// Note: it seems this constructor is not run in the Java Event Dispatch Thread.

		super(wizard, model, name);
		this.algorithmsFolder = algorithmsFolder;
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
			statusLabel.setText("The graphics card is ready for denoising calculations!");
			wizard.updateButtons();
		};

		String engine = Preferences.getQuasarEngine();
		boolean loadCompiler = false;
		QuasarInitializationSwingWorker worker = new QuasarInitializationSwingWorker(engine, algorithmsFolder, loadCompiler, whenDone);
		worker.execute();		
	}
	
	@Override
	protected void aboutToShowPanel()
	{
		if (!initialized)
		{
			initializeQuasar();
		}
	}
	
	@Override
	protected boolean canGoToNextPage()
	{
		return initialized;
	}
}
