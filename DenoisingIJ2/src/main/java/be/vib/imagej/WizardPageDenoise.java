package be.vib.imagej;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ij.process.ImageProcessor;

public class WizardPageDenoise extends WizardPage
{
	private JProgressBar progressBar;
	private DenoiseSummaryPanel denoiseSummaryPanel;
	
	public WizardPageDenoise(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		if (model.imagePlus.getNSlices() > 1)
			buildImageStackUI();
		else
			buildImageUI();
		// TODO: Extract the two possible panels, one for denoising a single slice and one for denoising a full stack 
	}
	
	private void buildImageUI()
	{
		JButton startButton = new JButton("Start Denoising");
		JLabel statusLabel = new JLabel("Denoising...");
		statusLabel.setVisible(false);

		startButton.addActionListener(e -> {
			startButton.setVisible(false);
			statusLabel.setVisible(true);
			DenoiseSlice(model.imagePlus.getCurrentSlice());
			statusLabel.setText("Denoising done."); });

		denoiseSummaryPanel = new DenoiseSummaryPanel(model);
		denoiseSummaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, denoiseSummaryPanel.getMaximumSize().height));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(denoiseSummaryPanel);
		add(Box.createRigidArea(new Dimension(0, 20)));
		add(startButton);
		add(statusLabel);
	}
	
	private void buildImageStackUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JButton startButton = new JButton("Start Denoising");
		JLabel statusLabel = new JLabel("Denoising...");
		
		progressBar = new JProgressBar(model.range.getFirst(), model.range.getLast());
		
		statusLabel.setVisible(false);
		progressBar.setVisible(false);

		startButton.addActionListener(e -> {
			progressBar.setVisible(true);
			startButton.setVisible(false);
			statusLabel.setVisible(true);
			// FIXME: we may need to denoise the full stack, not just the current slice
		    DenoiseSlice(model.imagePlus.getCurrentSlice());
		    /*statusLabel.setText("Denoising done.");*/ });
		
		DenoiseSummaryPanel denoiseSummaryPanel = new DenoiseSummaryPanel(model);
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
	private void DenoiseSlice(int slice) 
	{
		System.out.println("DenoiseSlice " + slice + " (Java thread: " + Thread.currentThread().getId() + ")");
		ImageProcessor image = model.imagePlus.getStack().getProcessor(slice);
		
		DenoiseSwingWorker worker = new DenoiseSwingWorker(model.getDenoiser(image));
		
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
		// we may have to update it (e.g. it could have been a single image initially, and an image stack now.)
		
		denoiseSummaryPanel.updateText();
	}
}
