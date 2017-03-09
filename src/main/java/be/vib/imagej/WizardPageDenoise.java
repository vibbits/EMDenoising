package be.vib.imagej;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class WizardPageDenoise extends WizardPage
{
	private DenoiseSummaryPanel denoiseSummaryPanel;
	private JButton startButton;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private RangeSelectionPanel rangeSelectionPanel;
	private boolean busyDenoising = false;
	
	public WizardPageDenoise(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		buildUI();
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		startButton = new JButton("Start Denoising");
		statusLabel = new JLabel();
		
		progressBar = new JProgressBar();
		
		statusLabel.setVisible(false);
		progressBar.setVisible(false);

		startButton.addActionListener(e -> {
		    denoise();
		});
		
		// FIXME: we need some way to cancel a long denoising tasks
		
		denoiseSummaryPanel = new DenoiseSummaryPanel(model);
		denoiseSummaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, denoiseSummaryPanel.getMaximumSize().height));
		
		rangeSelectionPanel = new RangeSelectionPanel(model);
		rangeSelectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rangeSelectionPanel.getMaximumSize().height));
		
		rangeSelectionPanel.setAlignmentX(CENTER_ALIGNMENT);
		startButton.setAlignmentX(CENTER_ALIGNMENT);
		statusLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		add(denoiseSummaryPanel);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(rangeSelectionPanel);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(startButton);
		add(progressBar);
		add(statusLabel);
	}
	
    // denoise() is executed on the Java EDT, so it needs to complete ASAP.
	// Off-load calculations to a separate thread and return immediately.
	private void denoise() 
	{
		System.out.println("Denoise " + model.getRange() + " (Java thread: " + Thread.currentThread().getId() + ")");
		
		busyDenoising = true;
		wizard.updateButtons();  // disable the Finish and Back buttons while we're busy denoising

		startButton.setVisible(false);
		
		statusLabel.setText("Denoising...");
		statusLabel.setVisible(true);
		
		progressBar.setMinimum(0);    // progress will be mapped by DenoiseSwingWorker to a value in [0, 100]
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true); // show percentage progress as text in the progress bar
		progressBar.setVisible(true);
		
		Runnable whenDone = () -> {
			busyDenoising = false;
			statusLabel.setText("Denoising done.");
			progressBar.setVisible(false);
			wizard.updateButtons();
		};
			
		// FIXME: pass model.getAlgorithm() alone, not xxx.getDenoiser() and xxx.getName()
		DenoiseSwingWorker worker = new DenoiseSwingWorker(model.getAlgorithm().getDenoiser(), model.getImage(), model.getRange(), model.getAlgorithm().getReadableName(), progressBar, whenDone);
		
		// Run the denoising on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the user interface
		// and show the denoised image in a new ImageJ window.
		worker.execute();
	}
	
	@Override
	protected void aboutToShowPanel()
	{
		// After denoising was complete, we may have gone back, chosen another image or image stack, 
		// and returned to the denoising panel. So some status messages or buttons may need to be updated.
		
		busyDenoising = false;
		
		rangeSelectionPanel.updateRange();
		
		denoiseSummaryPanel.updateText();

		startButton.setVisible(true);
		statusLabel.setVisible(false);
		progressBar.setVisible(false);
		
		wizard.pack();
	}

	@Override
	protected boolean canFinish()
	{
		return !busyDenoising;
	}
	
	@Override
	protected boolean canGoToPreviousPage()
	{
		return !busyDenoising;
	}

}
