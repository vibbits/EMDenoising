package be.vib.imagej;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class WizardPageDenoise extends WizardPage
{
	private JProgressBar progressBar;
	private DenoiseSummaryPanel denoiseSummaryPanel;
	
	public WizardPageDenoise(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		buildUI();
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JButton startButton = new JButton("Start Denoising");
		JLabel statusLabel = new JLabel("Denoising...");
		
		progressBar = new JProgressBar();
		
		statusLabel.setVisible(false);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true); // show percentage progress as text in the progress bar

		startButton.addActionListener(e -> {
			startButton.setVisible(false);
			statusLabel.setVisible(true);

			progressBar.setVisible(true);
			progressBar.setMinimum(model.range.getFirst());
			progressBar.setMaximum(model.range.getLast());
			
			// run a background thread doing the denoising
		    denoise();
		});
		
		// FIXME: we need some way to cancel a long denoising tasks
		
		denoiseSummaryPanel = new DenoiseSummaryPanel(model);
		denoiseSummaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, denoiseSummaryPanel.getMaximumSize().height));
		
		RangeSelectionPanel rangeSelectionPanel = new RangeSelectionPanel(model);
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
	
    // DenoiseSlice is called from the Java EDT, so it needs to complete ASAP.
	// Off-load calculations to a separate thread and return immediately.
	private void denoise() 
	{
		System.out.println("Denoise " + model.range + " (Java thread: " + Thread.currentThread().getId() + ")");
				
		DenoiseSwingWorker worker = new DenoiseSwingWorker(model.getDenoiser(), model.imagePlus, model.range, progressBar);
		
		// Run the denoising preview on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the denoising
		// preview image in the Java Event Dispatch Thread (EDT).
		worker.execute();
	}
	
	@Override
	public void aboutToShowPanel()
	{
		// After denoising was complete, we may have gone back and returned to the denoising panel.
		// So some status messages or buttons may need to be updated.
		
		// FIXME - model.imagePlus may be different from when this page was initially build - 
		// we may have to update the wizard (e.g. it could have been a single image initially, and an image stack now.)
		
		denoiseSummaryPanel.updateText();
	}
}
