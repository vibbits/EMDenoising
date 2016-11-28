package be.vib.imagej;

import java.awt.Dimension;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
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
			// FIXME: need to denoise the full stack, not just the current slice
			// FIXME: we probably need to do the denoising in a worker thread - but must be careful which thread because of Quasar
		    DenoiseSlice(model.imagePlus.getCurrentSlice());
		    statusLabel.setText("Denoising done."); });
		
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
	
	private void DenoiseSlice(int slice)
	{
		// TODO: DRY - similar to denoising the preview
		System.out.println("DenoiseSlice " + slice + " (Java thread: " + Thread.currentThread().getId() + ")");
		
		Instant startTime = Instant.now();
		
		ImageProcessor image = model.imagePlus.getStack().getProcessor(slice);
		
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		Object pixelsObject = image.getPixels();
		assert(pixelsObject instanceof byte[]);
		byte[] inputPixels = (byte[])pixelsObject; 
		
		byte[] outputPixels = null;
		switch (model.denoisingAlgorithm)
		{
			case NLMS:
				// TODO
				// outputPixels = QuasarInterface.quasarNlmeans(width, height, inputPixels, (float)model.nonLocalMeansParams.sigma, model.nonLocalMeansParams.searchWindow, model.nonLocalMeansParams.halfBlockSize, 0, 0);
				break;
			default:
				outputPixels = Arrays.copyOf(inputPixels, inputPixels.length);
				break;
		}
		
		ByteProcessor denoisedSlice = new ByteProcessor(width, height, outputPixels);
		
		ImageStack denoisedStack = new ImageStack(width, height);
		denoisedStack.addSlice("", denoisedSlice);
		
		ImagePlus denoisedImagePlus = new ImagePlus("Denoised " + model.imagePlus.getTitle(), denoisedStack);
		
		// Log some timing statistics
		Instant endTime = Instant.now();
		long durationMs = Duration.between(startTime, endTime).toMillis();
		long numPixels = width * height;
		System.out.println("Slice denoising time: " + durationMs + " ms " +
		         "(" + (double)numPixels / (double)durationMs + " kpix/s)");
		
		// Display denoised image
		denoisedImagePlus.show();
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
