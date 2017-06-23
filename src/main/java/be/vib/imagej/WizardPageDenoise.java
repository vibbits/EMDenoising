package be.vib.imagej;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class WizardPageDenoise extends WizardPage implements ImageRangeChangeEventListener
{
	private DenoiseSummaryPanel denoiseSummaryPanel;
	private JButton startButton;
	private JButton cancelButton;
	private JLabel statusLabel;
	private JProgressBar progressBar;
	private RangeSelectionPanel rangeSelectionPanel;
	private boolean busyDenoising = false;
	private DenoiseSwingWorker worker;
	
	public WizardPageDenoise(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		buildUI();
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		startButton = new JButton("Start Denoising");
		cancelButton = new JButton("Cancel Denoising");
		
		statusLabel = new JLabel();
		
		progressBar = new JProgressBar();
		
		startButton.setVisible(true);
		cancelButton.setVisible(false);
		statusLabel.setVisible(false);
		progressBar.setVisible(false);

		startButton.addActionListener(e -> {
		    denoise();
		});
		
		cancelButton.addActionListener(e -> {
		    System.out.println("User asked to cancel denoising.");
		    worker.cancel(false);
		});
		
		denoiseSummaryPanel = new DenoiseSummaryPanel(model);
		denoiseSummaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, denoiseSummaryPanel.getMaximumSize().height));
		
		rangeSelectionPanel = new RangeSelectionPanel(model);
		rangeSelectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rangeSelectionPanel.getMaximumSize().height));
		rangeSelectionPanel.addEventListener(this);
		
		rangeSelectionPanel.setAlignmentX(CENTER_ALIGNMENT);
		startButton.setAlignmentX(CENTER_ALIGNMENT);
		cancelButton.setAlignmentX(CENTER_ALIGNMENT);
		statusLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		add(denoiseSummaryPanel);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(rangeSelectionPanel);
		add(Box.createRigidArea(new Dimension(0, 10)));
		add(startButton);
		add(progressBar);
		add(statusLabel);
		add(Box.createRigidArea(new Dimension(0, 20)));
		add(cancelButton);
	}
	
    // denoise() is executed on the Java EDT, so it needs to complete ASAP.
	// Off-load calculations to a separate thread and return immediately.
	private void denoise() 
	{		
		busyDenoising = true;
		wizard.updateButtons();  // disable the Back button while we're busy denoising

		startButton.setVisible(false);
		cancelButton.setVisible(true);
		
		rangeSelectionPanel.enable(false);
		
		statusLabel.setText("Denoising...");
		statusLabel.setVisible(true);
		
		progressBar.setMinimum(0);    // progress will be mapped by DenoiseSwingWorker to a value in [0, 100]
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true); // show percentage progress as text in the progress bar
		progressBar.setVisible(true);
		
		Runnable whenDone = () -> {
			busyDenoising = false;
			cancelButton.setVisible(false);
			rangeSelectionPanel.enable(true);
			statusLabel.setText(worker.isCancelled() ? "Denoising cancelled": "Denoising done");
			progressBar.setVisible(false);
			wizard.updateButtons();
		};
			
		worker = new DenoiseSwingWorker(model.getAlgorithm(), model.getImage(), model.getRange(), progressBar, whenDone);
		
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
		cancelButton.setVisible(false);
		statusLabel.setVisible(false);
		progressBar.setVisible(false);
		
		wizard.pack();
	}
	
	@Override
	protected boolean canGoToPreviousPage()
	{
		return !busyDenoising;
	}

	@Override
	public void handleImageRangeChangeEvent(ImageRangeChangeEvent e)
	{
		assert(busyDenoising == false);
		startButton.setVisible(true);
		cancelButton.setVisible(false);
		progressBar.setVisible(false);
		statusLabel.setVisible(false);
	}
}
