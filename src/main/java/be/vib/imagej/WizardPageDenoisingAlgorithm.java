package be.vib.imagej;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import be.vib.bits.QExecutor;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class WizardPageDenoisingAlgorithm extends WizardPage 
{
	private JPanel algoParamsPanel;
		
	private ImagePanel origImagePanel;
	private ImagePanel denoisedImagePanel;
	
	private PreviewPanel previewPanel;
	
	private DenoisePreviewCache previewCache = new DenoisePreviewCache(100); // Assuming a 512x512 ROI and 8 bit/pixel grayscale previews, a full cache requires about 100 * (1 MB / 4) = 25 MB storage
	
    static final int maxPreviewSize = 256;
    
	private class Algorithm
	{
		String name;
		WizardModel.DenoisingAlgorithm algo;
		DenoiseParamsPanelBase panel;
		
		public Algorithm(String name, WizardModel.DenoisingAlgorithm algo, DenoiseParamsPanelBase panel)
		{
			this.name = name;
			this.algo = algo;
			this.panel = panel;
		}
	}

	public WizardPageDenoisingAlgorithm(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		
		final Algorithm[] algorithms = { 
				new Algorithm("Gaussian", WizardModel.DenoisingAlgorithm.GAUSSIAN, new GaussianParamsPanel(model.gaussianParams)),   	    
				new Algorithm("Bilateral", WizardModel.DenoisingAlgorithm.BILATERAL, new BilateralParamsPanel(model.bilateralParams)),   	    
				new Algorithm("Anisotropic Diffusion", WizardModel.DenoisingAlgorithm.ANISOTROPIC_DIFFUSION, new AnisotropicDiffusionParamsPanel(model.anisotropicDiffusionParams)), 
				new Algorithm("BLS-GSM", WizardModel.DenoisingAlgorithm.BLSGSM, new BLSGSMParamsPanel(model.blsgsmParams)),
				new Algorithm("Wavelet Thresholding", WizardModel.DenoisingAlgorithm.WAVELET_THRESHOLDING, new WaveletThresholdingParamsPanel(model.waveletThresholdingParams)),    	    
				new Algorithm("Non-local means", WizardModel.DenoisingAlgorithm.NLMS, new NonLocalMeansParamsPanel(model.nonLocalMeansParams)),
				new Algorithm("Non-local means SCD", WizardModel.DenoisingAlgorithm.NLMS_SCD, new NonLocalMeansSCDParamsPanel(model.nonLocalMeansSCDParams))
				};
		
		buildUI(algorithms);		
	}
	
	private void buildUI(Algorithm[] algorithms)
	{
		JPanel algoChoicePanel = createAlgorithmChoicePanel(algorithms);

		algoParamsPanel = createAlgorithmParametersPanel(algorithms);
		
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setLayout(new BoxLayout(algorithmPanel, BoxLayout.X_AXIS));
		algorithmPanel.add(algoChoicePanel);
		algorithmPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		algorithmPanel.add(algoParamsPanel);
		
		previewPanel = new PreviewPanel();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
		add(previewPanel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(algorithmPanel);
	}
	
	private JPanel createAlgorithmChoicePanel(Algorithm[] algorithms)
	{
		List<JRadioButton> buttons = new ArrayList<JRadioButton>();
		for (Algorithm a : algorithms)
		{
			buttons.add(createAlgorithmRadioButton(a.name, a.algo));
		}
		
	    // Add radio buttons to group so they are mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    for (JRadioButton b : buttons)
	    {
	    	group.add(b);
	    }
				
		JPanel algoChoicePanel = new JPanel();
		algoChoicePanel.setLayout(new BoxLayout(algoChoicePanel, BoxLayout.Y_AXIS));
		algoChoicePanel.setBorder(BorderFactory.createTitledBorder("Denoising Algorithm"));
		for (JRadioButton b : buttons)
		{
			algoChoicePanel.add(b);
		}
		algoChoicePanel.add(Box.createVerticalGlue());
		
		return algoChoicePanel;
	}
	
	private JPanel createAlgorithmParametersPanel(Algorithm[] algorithms)
	{		
		for (Algorithm a : algorithms)
		{
			a.panel.addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });	
		}
		
		CardLayout cardLayout = new CardLayout();
		JPanel panel = new JPanel(cardLayout);
		for (Algorithm a : algorithms)
		{
			panel.add(a.panel, a.algo.name());
		}
		
		cardLayout.show(panel, model.denoisingAlgorithm.name());
		return panel;
	}

	private JRadioButton createAlgorithmRadioButton(String text, WizardModel.DenoisingAlgorithm algorithm)
	{
	    JRadioButton button = new JRadioButton(text);
	    button.setSelected(model.denoisingAlgorithm == algorithm);
		
	    button.addActionListener(e -> {
	    	if (model.denoisingAlgorithm == algorithm) return;
    		((CardLayout)algoParamsPanel.getLayout()).show(algoParamsPanel, algorithm.name());
			model.denoisingAlgorithm = algorithm;
			recalculateDenoisedPreview();
	    });
	    
	    return button;
	}
	
	private class PreviewPanel extends JPanel
	{
		public PreviewPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Denoising Preview"));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			
			origImagePanel = new ImagePanel(WizardPageDenoisingAlgorithm.this);
			denoisedImagePanel = new ImagePanel(WizardPageDenoisingAlgorithm.this);
			
			JPanel origImagePane = addTitle(origImagePanel, "Original ROI");
			JPanel denoisedImagePane = addTitle(denoisedImagePanel, "Denoised ROI");
			
			add(Box.createHorizontalGlue());
			add(origImagePane);
			add(Box.createRigidArea(new Dimension(20,0)));
			add(denoisedImagePane);
			add(Box.createHorizontalGlue());
		}
	}
	
	private void recalculateDenoisedPreview()
	{
		// Note: we have to copy the cache key and value (the denoising parameters object and preview image object)
		// to ensure they are not modified after we stored them in the cache.
		DenoisePreviewCacheKey cacheKey = new DenoisePreviewCacheKey(model.denoisingAlgorithm, model.getDenoisingParams());
		BufferedImage image = previewCache.get(cacheKey);
		if (image != null)
		{
			// Our non-cached updates must be queued on the same task queue as the cached updates, so we use QExecutor.
			// Furthermore, GUI updates need to be performed on the Java EDT, so we use invokeLater().
			BufferedImage imageCopy = deepCopy(image);
			QExecutor.getInstance().execute(() -> { SwingUtilities.invokeLater( () -> { denoisedImagePanel.setImage(imageCopy); }); });
		}
		else
		{			
			Denoiser denoiser = model.getDenoiser();
			
			// Deep copy of the noisy input image (since the denoising happens asynchronously and we don't want surprises if the input image gets changed meanwhile...)
			denoiser.setImage(model.previewOrigROI.duplicate());		
			
			Function<BufferedImage, Void> cacheAndShow = (BufferedImage img) -> { previewCache.put(cacheKey, img);
											                                      denoisedImagePanel.setImage(img);
			                                                                      return null; };
			
			DenoisePreviewSwingWorker worker = new DenoisePreviewSwingWorker(denoiser, cacheAndShow);
			
			// Run the denoising preview on a separate worker thread and return here immediately.
			// Once denoising has completed, the worker will automatically update the denoising
			// preview image in the Java Event Dispatch Thread (EDT).
			worker.execute();
		}
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
	protected void aboutToShowPanel()
	{
		assert(model.imagePlus != null);
		
		// Always clear the cache, just in case the user switched to a different image or ROI.
		// IMPROVEME: We could be more precise. We now occasionally clear the cache when it would be nice if we hadn't.
		previewCache.clear();
		
		//assert(model.roi != null && model.roi.getBounds() != null && model.roi.getBounds().isEmpty() == false);
		
		System.out.println("WizardPageDenoisingAlgorithm.aboutToShowPanel: model=" + model + " imagePlus=" + model.imagePlus);
		
		Rectangle roi = null;
		if (model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty())
			roi = model.imagePlus.getRoi().getBounds();
		else 
			roi = new Rectangle(maxPreviewSize, maxPreviewSize);   // TODO: slightly better is probably to center this default ROI on the image, instead of the top left corner 
		
		Dimension size = bestPreviewSize(roi, maxPreviewSize);
		
		// Take a deep copy of the selected ROI of the image.
		// After this, the user changing or removing the ROI on the image
		// has no effect anymore until she navigates back to the WizardPageROI.
		// (In the future we may want to dynamically listen to ROI changes.
		// But what if the ROI disappears? Pick one ourselves and warn the user in the UI?)
		model.previewOrigROI = cropImage(model.imagePlus, roi);
		origImagePanel.setImage(model.previewOrigROI.getBufferedImage());   // TODO? should the panel listen to changes to model.previewOrigROI so it updates "automatically" ?

		origImagePanel.setPreferredSize(size);
		origImagePanel.invalidate();
		
		model.previewDenoisedROI = null;
		denoisedImagePanel.setImage(null);
		denoisedImagePanel.setText("Calculating...");
		
		denoisedImagePanel.setPreferredSize(size);
		denoisedImagePanel.invalidate();

		recalculateDenoisedPreview();
		
		// Ask layout manager to resize the dialog so it looks nice
		wizard.pack();
	}
	
	private static BufferedImage deepCopy(BufferedImage image)
	{
		// TODO: carefully chcek this code
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
	}
	
	private static Dimension bestPreviewSize(Rectangle roi, int maxSize)
	{
		assert(roi != null && roi.isEmpty() == false);
		
		int width = roi.width;
		int height = roi.height;
		int actualSize = Math.max(width, height);
		
		float scale = 1.0f;
		if (actualSize <= maxSize)
		{
			scale = 1.0f;
		}
		else
		{
			scale = (float)maxSize / (float)actualSize;		
		}	

		int w = (int)(roi.width * scale);
		int h = (int)(roi.height * scale);
		return new Dimension(w, h);
	}

	public static ImageProcessor cropImage(ImagePlus image, Rectangle roi) // roi == null is allowed; note: image.getRoi() is ignored (the user may change it + it may be too large for a preview)
	{
		int slice = image.getCurrentSlice();
		ImageStack stack = image.getStack();
		ImageProcessor imp = stack.getProcessor(slice);
		
		if (roi != null)
		{
			imp.setRoi(roi);
			return imp.crop();
		}
		else
		{
			return imp.duplicate();
		}
	}
}
