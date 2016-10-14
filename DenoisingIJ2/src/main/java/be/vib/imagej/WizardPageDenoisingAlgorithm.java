package be.vib.imagej;
import java.util.Arrays;
import java.util.function.*;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class WizardPageDenoisingAlgorithm extends WizardPage 
{
	private JPanel algoParamsPanel;
	private CardLayout algoParamsCardLayout;
	
	private NumberFormat integerFormat;
	private NumberFormat floatFormat;
	
	private ImagePanel origImagePanel;
	private ImagePanel denoisedImagePanel;
	
    private static final int maxPreviewSize = 256;
	
	public WizardPageDenoisingAlgorithm(WizardModel model, String name)
	{
		super(model, name);
		setupFormatters();			
		buildUI();
	}

	private void setupFormatters()
	{
		integerFormat = NumberFormat.getNumberInstance();
		
		floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
	}
	
	private void buildUI()
	{
		JRadioButton nlmeansButton = new JRadioButton("Non-local means");
		nlmeansButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.NLMS);

	    JRadioButton bm3dButton = new JRadioButton("BM3D");
	    bm3dButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.BM3D);
	    
	    nlmeansButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.NLMS;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
	    });

	    bm3dButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.BM3D;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
    	});

	    // Add radio buttons to group so they are mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(nlmeansButton);
	    group.add(bm3dButton);
				
		JPanel algoChoicePanel = new JPanel();
		algoChoicePanel.setLayout(new BoxLayout(algoChoicePanel, BoxLayout.Y_AXIS));
		algoChoicePanel.setBorder(BorderFactory.createTitledBorder("Algorithm"));
		algoChoicePanel.add(nlmeansButton);
		algoChoicePanel.add(bm3dButton);
		algoChoicePanel.add(Box.createVerticalGlue());
		
		algoParamsCardLayout = new CardLayout();
		algoParamsPanel = new JPanel(algoParamsCardLayout);
		algoParamsPanel.add(new NonlocalMeansParamsPanel(), WizardModel.DenoisingAlgorithm.NLMS.name());
		algoParamsPanel.add(new BM3DParamsPanel(), WizardModel.DenoisingAlgorithm.BM3D.name());
		algoParamsCardLayout.show(algoParamsPanel, model.denoisingAlgorithm.name());
		
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setLayout(new BoxLayout(algorithmPanel, BoxLayout.X_AXIS));
		algorithmPanel.add(algoChoicePanel);
		algorithmPanel.add(algoParamsPanel);
		
		JPanel previewPanel = new PreviewPanel();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
		add(previewPanel);
		add(algorithmPanel);
	}
	
	class NonlocalMeansParamsPanel extends JPanel
	{
		private JSpinner searchWindowSpinner;
		private JSlider searchWindowSlider;
		
		private JSpinner halfBlockSizeSpinner;
		private JSlider halfBlockSizeSlider;
		
		private JSlider sigmaSlider;
		
		public NonlocalMeansParamsPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Non-local means"));
			
			final float sigmaMin = WizardModel.NonLocalMeansParams.sigmaMin;
			final float sigmaMax = WizardModel.NonLocalMeansParams.sigmaMax;
			
			Function<Float, Integer> toSlider = sigma -> (int)(100 * (sigma - sigmaMin) / (sigmaMax - sigmaMin));
			Function<Integer, Float> fromSlider = s -> sigmaMin + (sigmaMax - sigmaMin) * s / 100.0f;

			JLabel sigmaLabel = new JLabel("Sigma:");
			JFormattedTextField sigmaField = new JFormattedTextField(floatFormat);
			sigmaField.setValue(new Float(model.nonLocalMeansParams.sigma));
			sigmaField.setColumns(5);
			sigmaField.addPropertyChangeListener("value", e -> {
				float newValue = ((Number)sigmaField.getValue()).floatValue();
				if (newValue == model.nonLocalMeansParams.sigma) return;
				model.nonLocalMeansParams.sigma = newValue;
				System.out.println("textfield updated model, sigma now:" + model.nonLocalMeansParams.sigma);
				sigmaSlider.setValue(toSlider.apply(model.nonLocalMeansParams.sigma));
				recalculateDenoisedPreview();
			});
			
			sigmaSlider = new JSlider(0, 100, toSlider.apply(model.nonLocalMeansParams.sigma));
			sigmaSlider.addChangeListener(e -> {
		    	int newValue = ((Number)sigmaSlider.getValue()).intValue();
		    	float newSigma = fromSlider.apply(newValue);
		    	if (newSigma == model.nonLocalMeansParams.sigma) return;
		    	model.nonLocalMeansParams.sigma = newSigma;
				System.out.println("slider updated model, sigma now:" + model.nonLocalMeansParams.sigma);
				sigmaField.setValue(new Float(model.nonLocalMeansParams.sigma));
				recalculateDenoisedPreview();
		    });

			final int searchWindowMin = WizardModel.NonLocalMeansParams.searchWindowMin;
			final int searchWindowMax = WizardModel.NonLocalMeansParams.searchWindowMax;
			
			JLabel searchWindowLabel = new JLabel("Search window:");
			
			SpinnerModel searchWindowSpinnerModel = new SpinnerNumberModel(model.nonLocalMeansParams.searchWindow, searchWindowMin, searchWindowMax, 1);
			searchWindowSpinner = new JSpinner(searchWindowSpinnerModel);
			searchWindowSpinner.addChangeListener(e -> {
		    	int newValue = ((Number)searchWindowSpinner.getValue()).intValue();
		    	if (newValue == model.nonLocalMeansParams.searchWindow) return;
		    	model.nonLocalMeansParams.searchWindow = newValue;
				System.out.println("spinner updated model, searchWindow now:" + model.nonLocalMeansParams.searchWindow);
				searchWindowSlider.setValue(newValue);
				recalculateDenoisedPreview();
		    });
	
			searchWindowSlider = new JSlider(searchWindowMin, searchWindowMax, model.nonLocalMeansParams.searchWindow);		
			searchWindowSlider.addChangeListener(e -> {
		    	int newValue = ((Number)searchWindowSlider.getValue()).intValue();
		    	if (newValue == model.nonLocalMeansParams.searchWindow) return;
		    	model.nonLocalMeansParams.searchWindow = newValue;
				System.out.println("slider updated model, searchWindow now:" + model.nonLocalMeansParams.searchWindow);
				searchWindowSpinner.setValue(newValue);
				recalculateDenoisedPreview();
		    });
			
			JLabel halfBlockSizeLabel = new JLabel("Half block size:");
	
			final int halfBlockSizeMin = WizardModel.NonLocalMeansParams.halfBlockSizeMin;
			final int halfBlockSizeMax = WizardModel.NonLocalMeansParams.halfBlockSizeMax;
	
			SpinnerModel halfBlockSizeSpinnerModel = new SpinnerNumberModel(model.nonLocalMeansParams.halfBlockSize, halfBlockSizeMin, halfBlockSizeMax, 1);
			halfBlockSizeSpinner = new JSpinner(halfBlockSizeSpinnerModel);
			halfBlockSizeSpinner.addChangeListener(e -> {
		    	int newValue = ((Number)halfBlockSizeSpinner.getValue()).intValue();
		    	if (newValue == model.nonLocalMeansParams.halfBlockSize) return;
		    	model.nonLocalMeansParams.halfBlockSize = newValue;
				System.out.println("spinner updated model, halfBlockSize now:" + model.nonLocalMeansParams.halfBlockSize);
				halfBlockSizeSlider.setValue(newValue);
				recalculateDenoisedPreview();
		    });
			
			halfBlockSizeSlider = new JSlider(halfBlockSizeMin, halfBlockSizeMax, model.nonLocalMeansParams.halfBlockSize);
			halfBlockSizeSlider.addChangeListener(e -> {
		    	int newValue = ((Number)halfBlockSizeSlider.getValue()).intValue();
		    	if (newValue == model.nonLocalMeansParams.halfBlockSize) return;
		    	model.nonLocalMeansParams.halfBlockSize = newValue;
				System.out.println("slider updated model, halfBlockSize now:" + model.nonLocalMeansParams.halfBlockSize);
				halfBlockSizeSpinner.setValue(newValue);
				recalculateDenoisedPreview();
		    });
			
			GroupLayout layout = new GroupLayout(this);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				           .addComponent(sigmaLabel)
				           .addComponent(searchWindowLabel)
			      		   .addComponent(halfBlockSizeLabel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				           .addComponent(sigmaField)
				           .addComponent(searchWindowSpinner)
			      		   .addComponent(halfBlockSizeSpinner))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			    		   .addComponent(sigmaSlider)
			               .addComponent(searchWindowSlider)
			               .addComponent(halfBlockSizeSlider))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    				  .addComponent(sigmaLabel)
			    				  .addComponent(sigmaField))
				           .addComponent(sigmaSlider))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    				  .addComponent(searchWindowLabel)
			    				  .addComponent(searchWindowSpinner))
				           .addComponent(searchWindowSlider))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		  		.addComponent(halfBlockSizeLabel)
			    		  		.addComponent(halfBlockSizeSpinner))
			    		  .addComponent(halfBlockSizeSlider))
			);		
			
			setLayout(layout);
		}
	}
	
	class BM3DParamsPanel extends JPanel
	{
		public BM3DParamsPanel()
		{
			setBorder(BorderFactory.createTitledBorder("BM3D"));
			
			JLabel magicValueLabel = new JLabel("Magic Value:");
			JFormattedTextField magicValueField = new JFormattedTextField(integerFormat);
			magicValueField.setValue(new Integer(model.bm3dParams.magicValue));
			magicValueField.setColumns(5);
			magicValueField.addPropertyChangeListener("value", e -> { model.bm3dParams.magicValue = ((Number)magicValueField.getValue()).intValue();
			                                                          System.out.println("model updated, magic value now:" + model.bm3dParams.magicValue);
			                                                          recalculateDenoisedPreview(); });
			
			// TODO: the user can still enter a real value in the magicValueField. It then remains a real value in the UI,
			//       even though the model contains an integer number. This is not ideal.
			
			JLabel luckyNumberLabel = new JLabel("Lucky Number:");
			JFormattedTextField luckyNumberField = new JFormattedTextField(floatFormat);
			luckyNumberField.setColumns(5);
			luckyNumberField.setValue(new Float(model.bm3dParams.luckyNumber));
			luckyNumberField.addPropertyChangeListener("value", e -> { model.bm3dParams.luckyNumber = ((Number)luckyNumberField.getValue()).floatValue();
	                                                                  System.out.println("model updated, lucky number now:" + model.bm3dParams.luckyNumber);
	                                                                  recalculateDenoisedPreview(); });
			
			GroupLayout layout = new GroupLayout(this);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
					
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				           .addComponent(magicValueLabel)
			      		   .addComponent(luckyNumberLabel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
				           .addComponent(magicValueField)
			      		   .addComponent(luckyNumberField))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(magicValueLabel)
				           .addComponent(magicValueField))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				           .addComponent(luckyNumberLabel)
				           .addComponent(luckyNumberField))
			);		
			
			setLayout(layout);
		}
	}
	
	class PreviewPanel extends JPanel
	{
		public PreviewPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Denoising Preview"));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			origImagePanel = new ImagePanel();
			denoisedImagePanel = new ImagePanel();
			
			JPanel origImagePane = addTitle(origImagePanel, "Original ROI");
			JPanel denoisedImagePane = addTitle(denoisedImagePanel, "Denoised ROI");
			
			add(Box.createHorizontalGlue());
			add(origImagePane);
			add(Box.createRigidArea(new Dimension(20,0)));
			add(denoisedImagePane);
			add(Box.createHorizontalGlue());
		}
	}
	
	private void recalculateOrigPreview()
	{
		model.previewOrigROI = cropImage(model.imagePlus, model.roi);
		origImagePanel.setImage(model.previewOrigROI.getBufferedImage(), maxPreviewSize);
	}

	private void recalculateDenoisedPreview()
	{		
		System.out.println("recalculateDenoisedPreview (Java thread: " + Thread.currentThread().getId() + ")");
		
		model.previewDenoisedROI = model.previewOrigROI.duplicate();

		final int width = model.previewOrigROI.getWidth();
		final int height = model.previewOrigROI.getHeight();
		
		Object pixelsObject = model.previewOrigROI.getPixels();
		assert(pixelsObject instanceof byte[]);
		byte[] inputPixels = (byte[])pixelsObject; 
		
		byte[] outputPixels = null;
		switch (model.denoisingAlgorithm)
		{
			case NLMS:
				outputPixels = QuasarInterface.quasarNlmeans(width, height, inputPixels, (float)model.nonLocalMeansParams.sigma, model.nonLocalMeansParams.searchWindow, model.nonLocalMeansParams.halfBlockSize, 0, 0);
				break;
			default:
				outputPixels = Arrays.copyOf(inputPixels, inputPixels.length);
				break;
		}
		
		model.previewDenoisedROI = new ByteProcessor(width, height, outputPixels);
		
		denoisedImagePanel.setImage(model.previewDenoisedROI.getBufferedImage(), maxPreviewSize);
	}
	
	private static JPanel addTitle(JPanel p, String title)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		p.setAlignmentX(CENTER_ALIGNMENT);
		
		panel.add(titleLabel);
		panel.add(p);
		panel.add(Box.createVerticalStrut(10)); // title label introduces some space at the top, also leave some space at the bottom for symmetry
		
		return panel;
	}
	
	@Override
	public void aboutToShowPanel()
	{
		// ROI may have changed, update the previews
		recalculateOrigPreview();
		recalculateDenoisedPreview();
	}
	
	private ImageProcessor cropImage(ImagePlus image, Rectangle roi)
	{
		int slice = model.imagePlus.getCurrentSlice();
		ImageStack stack = image.getStack();
		ImageProcessor imp = stack.getProcessor(slice);
		
		if (model.roi != null)
		{
			imp.setRoi(model.roi);
			return imp.crop();
		}
		else
		{
			return imp.duplicate();
		}
	}
	
	/*

//		Instant start = Instant.now();
//		
//		byte[] inputPixels = get8BitGrayscalePixels(imp);
//		
//		byte[] outputPixels = quasarNlmeans(imp.getWidth(), imp.getHeight(), inputPixels, 25.0f, 11, 3, 0, 0);
//		
//		if (outputPixels != null)
//		{
//			// Show denoised result
//			ImagePlus newImage = make8BitGrayscaleImage(imp.getWidth(), imp.getHeight(), outputPixels, "NLMS Denoised " + imp.getTitle());
//			newImage.show();
//
//			// Log some timing statistics
//			Instant end = Instant.now();
//			long durationMs = Duration.between(start, end).toMillis();
//			long numPixels = imp.getWidth() * imp.getHeight();
//			log.info("Denoising time: " + durationMs + " ms " +
//			         "(" + (double)numPixels / (double)durationMs + " kpix/s)");
//		}
//		else
//		{
//			IJ.showMessage("Non-local means denoising failed.");
//		}
//		
//		quasarRelease();
		
		log.info("EM denoising plugin done.");
	}
	
	private static byte[] get8BitGrayscalePixels(ImagePlus image)
	{
		ImageStack stack = image.getStack();
		assert(stack.getSize() == 1);
		
		Object pixelsObject = stack.getProcessor(1).getPixels();
		assert(pixelsObject instanceof byte[]);
		
		return (byte[])pixelsObject;
	}
	
	private static ImagePlus make8BitGrayscaleImage(int width, int height, byte[] pixels, String title)
	{
		ImageProcessor imageProcessor = new ByteProcessor(width, height, pixels); // ByteProcessor handles grayscale images
		
		ImageStack stack = new ImageStack(width, height);
		stack.addSlice("", imageProcessor);
		
		ImagePlus image = new ImagePlus(title, stack);
		return image;
	}
	

	 */
	
}
