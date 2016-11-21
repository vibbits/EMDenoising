package be.vib.imagej;

import java.awt.Dimension;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class WizardPageDenoise extends WizardPage
{
	private JProgressBar progressBar;
	
	public WizardPageDenoise(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		if (model.imagePlus.getNSlices() > 1)
			buildImageStackUI();
		else
			buildImageUI();
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

		SummaryPanel summaryPanel = new SummaryPanel();
		summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, summaryPanel.getMaximumSize().height));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(summaryPanel);
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
		
		SummaryPanel summaryPanel = new SummaryPanel();
		summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, summaryPanel.getMaximumSize().height));
		
		RangeSelectionPanel rangeSelectionPanel = new RangeSelectionPanel();
		rangeSelectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rangeSelectionPanel.getMaximumSize().height));
		
		rangeSelectionPanel.setAlignmentX(CENTER_ALIGNMENT);
		startButton.setAlignmentX(CENTER_ALIGNMENT);
		statusLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		add(summaryPanel);
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
	
	private static String html(String text)
	{
		return "<html>" + text + "</html>";  
	}
	
	private static String italic(String text)
	{
		return "<i>" + text + "</i>";  
	}
	
//	private static String color(String text, String color)
//	{
//		return "<font color=" + color + ">" + text + "</html>";  
//	}
	
	private class SummaryPanel extends JPanel
	{
		public SummaryPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Summary"));
			
			JLabel inputImageLabel = new JLabel("Original image:");
			JLabel denoisedImageLabel = new JLabel("Denoised image:");
			JLabel denoisingAlgorithmLabel = new JLabel("Denoising algorithm:");
			
			JLabel inputImage = new JLabel(html(italic(model.imagePlus.getTitle())));
			JLabel denoisedImage = new JLabel(html(italic("New image, original image will not be modified.")));
			JLabel denoisingAlgorithm = new JLabel(html(italic("Foo algorithm, param value, param value"))); // TODO - is there a Printable interface or so? to stringify the algorithm params
			
			GroupLayout layout = new GroupLayout(this);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				           .addComponent(inputImageLabel)
				           .addComponent(denoisedImageLabel)
			      		   .addComponent(denoisingAlgorithmLabel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
				           .addComponent(inputImage)
				           .addComponent(denoisedImage)
			      		   .addComponent(denoisingAlgorithm))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(inputImageLabel)
			    		   .addComponent(inputImage))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(denoisedImageLabel)
			    		   .addComponent(denoisedImage))
	 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(denoisingAlgorithmLabel)
			    		   .addComponent(denoisingAlgorithm))
			);		
			
			setLayout(layout);
		}
	}
	
	class RangeSelectionPanel extends JPanel
	{
		public RangeSelectionPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Slices to Denoise"));

			JRadioButton currentSliceRadioButton = new JRadioButton("Current slice");
			currentSliceRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.CURRENT_SLICE);

		    JRadioButton allSlicesRadioButton = new JRadioButton("All slices");
		    allSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.ALL_SLICES);
		    
		    JRadioButton rangeOfSlicesRadioButton = new JRadioButton("Range");
		    rangeOfSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
		    // TODO: input field for range
		    
		    currentSliceRadioButton.addActionListener(e -> {
	    		model.range = ImageRange.makeCurrentSliceRange(model.imagePlus);
		    });

		    allSlicesRadioButton.addActionListener(e -> {
	    		model.range = ImageRange.makeAllSlicesRange(model.imagePlus);
	    	});

		    rangeOfSlicesRadioButton.addActionListener(e -> {
		    	// TODO: get first and last from range input field
		    	int first = 1;
		    	int last = 1;
	    		model.range = ImageRange.makeNumericSliceRange(model.imagePlus, first, last);
	    	});

		    // Add radio buttons to group so they are mutually exclusive
		    ButtonGroup group = new ButtonGroup();
		    group.add(currentSliceRadioButton);
		    group.add(allSlicesRadioButton);
		    group.add(rangeOfSlicesRadioButton);
					
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(currentSliceRadioButton);
			add(allSlicesRadioButton);
			add(rangeOfSlicesRadioButton);

		}
	}

	@Override
	public void aboutToShowPanel()
	{
		// After denoising was complete, we may have gone back and returned to the denoising panel.
		// So some status messages or buttons may need to be updated.
		
		// FIXME - model.imagePlus may be different from when this page was initially build - 
		// we may have to update it (e.g. it could have been a single image initially, and an image stack now.)
	}
}
