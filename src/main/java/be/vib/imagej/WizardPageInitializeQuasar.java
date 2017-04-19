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
	
		// As long as this function does not return, the wizard will not appear either.
		// This is undesirable since Quasar initialization takes a couple of seconds and we
		// want to inform the user of what is happening.
		// So we run the initialization in a separate thread as a SwingWorker.
		
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
	protected boolean canGoToNextPage()
	{
		return initialized;
	}
}
