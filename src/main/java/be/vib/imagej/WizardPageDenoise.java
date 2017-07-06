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
	
	public WizardPageDenoise(Wizard wizard, String name)
	{
		super(wizard, name);
		buildUI();
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		startButton = new JButton("Start Denoising");
		cancelButton = new JButton("Cancel Denoising");
		
		statusLabel = new JLabel();
		
		progressBar = new JProgressBar();
		
		setButtonsReadyToDenoise();

		startButton.addActionListener(e -> {
			ImageRange range = rangeSelectionPanel.getRange();
			wizard.getModel().setRange(range);
		    denoise();
		});
		
		cancelButton.addActionListener(e -> {
		    worker.cancel(false);
		});
		
		denoiseSummaryPanel = new DenoiseSummaryPanel(wizard.getModel());
		denoiseSummaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, denoiseSummaryPanel.getMaximumSize().height));
		
		rangeSelectionPanel = new RangeSelectionPanel(wizard.getModel(), startButton);
		rangeSelectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rangeSelectionPanel.getPreferredSize().height));
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
		add(Box.createVerticalGlue());
	}
	
    // denoise() is executed on the Java EDT, so it needs to complete ASAP.
	// Off-load calculations to a separate thread and return immediately.
	private void denoise() 
	{		
		busyDenoising = true;
		wizard.updateButtons();  // disable the Back button while we're busy denoising

		startButton.setVisible(false);
		cancelButton.setVisible(true);
		
		rangeSelectionPanel.setEnabled(false);
		
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
			rangeSelectionPanel.setEnabled(true);
			statusLabel.setText(worker.isCancelled() ? "Denoising cancelled": "Denoising done");
			progressBar.setVisible(false);
			wizard.updateButtons();
		};
			
		WizardModel model = wizard.getModel();

		worker = new DenoiseSwingWorker(model.getAlgorithm(), model.getImage(), model.getRange(), progressBar, whenDone);
		
		// Run the denoising on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the user interface
		// and show the denoised image in a new ImageJ window.
		worker.execute();
	}
	
	// TODO? should we forbid closing the wizard (via the x button) when Quasar is busy denoising?
	
	@Override
	public void goingToNextPage() 
	{
		// We're the last page in the wizard.
		assert(false);
	}
	
	@Override
	public void goingToPreviousPage()
	{
		assert(canGoToPreviousPage());
		
		// Nothing to be done
	}

	@Override
	public void arriveFromNextPage() 
	{
		// We're the last page in the wizard.
		assert(false);
	}
	
	@Override
	public void arriveFromPreviousPage()
	{
		// After denoising was complete, we may have gone back, chosen another image or image stack, 
		// and returned to the denoising panel. So some status messages or buttons may need to be updated.
		
		busyDenoising = false;
		
		rangeSelectionPanel.aboutToShow();
		
		denoiseSummaryPanel.updateText();

		setButtonsReadyToDenoise();
		
		wizard.pack();
	}	

	@Override
	public boolean canGoToPreviousPage()
	{
		return !busyDenoising;
	}

	@Override
	public void handleImageRangeChangeEvent(ImageRangeChangeEvent e)
	{
		assert(busyDenoising == false);
		setButtonsReadyToDenoise();
	}
	
	private void setButtonsReadyToDenoise()
	{
		startButton.setVisible(true);
		cancelButton.setVisible(false);
		progressBar.setVisible(false);
		statusLabel.setVisible(false);
	}
}
