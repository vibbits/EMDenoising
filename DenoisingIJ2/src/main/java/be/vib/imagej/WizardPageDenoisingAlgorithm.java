package be.vib.imagej;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class WizardPageDenoisingAlgorithm extends WizardPage 
{
	private JPanel algoParamsPanel;
	private CardLayout algoParamsCardLayout;
		
	private ImagePanel origImagePanel;
	private ImagePanel denoisedImagePanel;
	
    static final int maxPreviewSize = 256;
    
	public WizardPageDenoisingAlgorithm(WizardModel model, String name)
	{
		super(model, name);
		buildUI();		
	}
	
	private void buildUI()
	{
	    JRadioButton gaussianButton = new JRadioButton("Gaussian");
	    gaussianButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.GAUSSIAN);
	    	    
	    JRadioButton diffusionButton = new JRadioButton("Anisotropic Diffusion");
	    diffusionButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.ANISOTROPIC_DIFFUSION);
	    
		JRadioButton nlmeansButton = new JRadioButton("Non-local means");
		nlmeansButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.NLMS);

	    JRadioButton waveletButton = new JRadioButton("Wavelet Thresholding");
	    waveletButton.setSelected(model.denoisingAlgorithm == WizardModel.DenoisingAlgorithm.WAVELET_THRESHOLDING);
	    	    
	    nlmeansButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.NLMS;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
	    });

	    diffusionButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.ANISOTROPIC_DIFFUSION;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
    	});

	    gaussianButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.GAUSSIAN;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
    	});
	    
	    waveletButton.addActionListener(e -> {
    		WizardModel.DenoisingAlgorithm algorithm = WizardModel.DenoisingAlgorithm.WAVELET_THRESHOLDING;
    		algoParamsCardLayout.show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
    	});
	    
	    // Add radio buttons to group so they are mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(nlmeansButton);
	    group.add(diffusionButton);
	    group.add(gaussianButton);
	    group.add(waveletButton);
				
		JPanel algoChoicePanel = new JPanel();
		algoChoicePanel.setLayout(new BoxLayout(algoChoicePanel, BoxLayout.Y_AXIS));
		algoChoicePanel.setBorder(BorderFactory.createTitledBorder("Denoising Algorithm"));
		algoChoicePanel.add(gaussianButton);
		algoChoicePanel.add(diffusionButton);
		algoChoicePanel.add(nlmeansButton);
		algoChoicePanel.add(waveletButton);
		algoChoicePanel.add(Box.createVerticalGlue());
		
		NonLocalMeansParamsPanel nonLocalMeansParamsPanel = new NonLocalMeansParamsPanel(model.nonLocalMeansParams);
		AnisotropicDiffusionParamsPanel anisotropicDiffusionParamsPanel = new AnisotropicDiffusionParamsPanel(model.anisotropicDiffusionParams);
		GaussianParamsPanel gaussianParamsPanel = new GaussianParamsPanel(model.gaussianParams);
		WaveletThresholdingParamsPanel waveletThresholdingParamsPanel = new WaveletThresholdingParamsPanel(model.waveletThresholdingParams);
		
		nonLocalMeansParamsPanel.addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });		
		anisotropicDiffusionParamsPanel.addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });
		gaussianParamsPanel.addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });
		waveletThresholdingParamsPanel.addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });

		algoParamsCardLayout = new CardLayout();
		algoParamsPanel = new JPanel(algoParamsCardLayout);
		algoParamsPanel.add(nonLocalMeansParamsPanel, WizardModel.DenoisingAlgorithm.NLMS.name());
		algoParamsPanel.add(anisotropicDiffusionParamsPanel, WizardModel.DenoisingAlgorithm.ANISOTROPIC_DIFFUSION.name());
		algoParamsPanel.add(gaussianParamsPanel, WizardModel.DenoisingAlgorithm.GAUSSIAN.name());
		algoParamsPanel.add(waveletThresholdingParamsPanel, WizardModel.DenoisingAlgorithm.WAVELET_THRESHOLDING.name());
		
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

	private class PreviewPanel extends JPanel
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
	
	private byte[] getPixelsCopy(ImageProcessor image)
	{
		Object pixelsObject = image.duplicate().getPixels();
		assert(pixelsObject instanceof byte[]);
		return (byte[])pixelsObject; 		
	}
	
	private Denoiser newDenoiser()
	{
		// Make an image denoiser. Since it will be used as a task that will be executed asynchronously,
		// we take a snapshot (deep copy) of the input image as well as the denoising
		// parameters as they are at this point in time.
		LinearImage image = new LinearImage(model.previewOrigROI.getWidth(), model.previewOrigROI.getHeight(), getPixelsCopy(model.previewOrigROI));
		
		switch (model.denoisingAlgorithm)
		{
			case NLMS:
				return new NonLocalMeansDenoiser(image, new NonLocalMeansParams(model.nonLocalMeansParams));
			case GAUSSIAN:
				return new GaussianDenoiser(image, new GaussianParams(model.gaussianParams));
			case WAVELET_THRESHOLDING:
				return new WaveletThresholdingDenoiser(image, new WaveletThresholdingParams(model.waveletThresholdingParams));
			case ANISOTROPIC_DIFFUSION:
				return new AnisotropicDiffusionDenoiser(image, new AnisotropicDiffusionParams(model.anisotropicDiffusionParams));
			default:
				return new NoOpDenoiser(image);
		}
	}

	private void recalculateDenoisedPreview()
	{		
		System.out.println("recalculateDenoisedPreview (Java thread: " + Thread.currentThread().getId() + ")");
		
		DenoiseSwingWorker worker = new DenoiseSwingWorker(newDenoiser(), model.previewDenoisedROI, denoisedImagePanel);
		
		// Run the denoising preview on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the denoising
		// preview image in the Java Event Dispatch Thread (EDT).
		worker.execute();

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
}
