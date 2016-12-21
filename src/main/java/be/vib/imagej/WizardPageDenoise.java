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
	private boolean doneDenoising = false;
	
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
		progressBar.setStringPainted(true); // show percentage progress as text in the progress bar

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
		System.out.println("Denoise " + model.range + " (Java thread: " + Thread.currentThread().getId() + ")");
		
		startButton.setVisible(false);
		
		statusLabel.setText("Denoising...");
		statusLabel.setVisible(true);

		progressBar.setVisible(model.imagePlus.getNSlices() > 1);  // the progress bar only updates after each slice, so it is silly to show it if we only have a single slice
		progressBar.setMinimum(model.range.getFirst());
		progressBar.setMaximum(model.range.getLast());
		
		Runnable whenDone = () -> {
			doneDenoising = true;
			statusLabel.setText("Denoising done.");
			progressBar.setVisible(false);
			wizard.updateButtons();
		};
				
		DenoiseSwingWorker worker = new DenoiseSwingWorker(model.getDenoiser(), model.imagePlus, model.range, progressBar, whenDone);
		
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
		
		doneDenoising = false;
		
		rangeSelectionPanel.updateRange();
		
		denoiseSummaryPanel.updateText();

		startButton.setVisible(true);
		statusLabel.setVisible(false);
		progressBar.setVisible(false);
	}

	@Override
	protected boolean canFinish()
	{
		return doneDenoising;
	}

}
