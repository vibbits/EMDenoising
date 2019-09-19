package be.vib.imagej;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import be.vib.bits.QExecutor;
import ij.process.ImageProcessor;

public class WizardPageDenoisingAlgorithm extends WizardPage 
{
	// The saturatingExecutor is an executor that, at any point in time, is executing at most one task while holding at most one additional task in a queue.
	// When the queue already holds a task, a new task will replace the old one in the queue. This executor is useful to avoid buildup of unfinished Quasar work.
	private SaturatingExecutor saturatingExecutor = new SaturatingExecutor(1, 1);
	
	private JPanel algorithmParamsPanel;
		
	private PreviewPanel origPreviewPanel;
	private PreviewPanel denoisedPreviewPanel;
			
	private Map<Algorithm.Name, JRadioButton> buttonsMap;
	
	// We maintain a cache of 100 denoised results for different parameter settings.
	// Assuming a 512x512 ROI and 8 bit/pixel grayscale previews, a full cache requires
	// about 100 * (1 MB / 4) = 25 MB storage. This seems acceptable.
	private DenoisePreviewCache previewCache = new DenoisePreviewCache(100);
		
 	public WizardPageDenoisingAlgorithm(Wizard wizard, String name)
	{
		super(wizard, name);
		buildUI();
	}
	
	private void buildUI()
	{
		Algorithm[] algorithms = wizard.getModel().getAlgorithms();
		
		JPanel algoChoicePanel = createAlgorithmChoicePanel(algorithms);

		algorithmParamsPanel = createAlgorithmParametersPanel(algorithms);
		
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setLayout(new BoxLayout(algorithmPanel, BoxLayout.X_AXIS));
		algorithmPanel.add(algoChoicePanel);
		algorithmPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		algorithmPanel.add(algorithmParamsPanel);
		
		origPreviewPanel = new PreviewPanel("Original ROI", wizard.getPreferences());
		denoisedPreviewPanel = new PreviewPanel("Denoised ROI", wizard.getPreferences());
		PreviewsPanel previewsPanel = new PreviewsPanel(origPreviewPanel, denoisedPreviewPanel);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
		add(previewsPanel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(algorithmPanel);
	}
	
	private JPanel createAlgorithmChoicePanel(Algorithm[] algorithms)
	{
		List<JRadioButton>buttons = new ArrayList<JRadioButton>();
		buttonsMap = new HashMap<Algorithm.Name, JRadioButton>();
		for (Algorithm algorithm : algorithms)
		{
			JRadioButton button = createAlgorithmRadioButton(algorithm); 
			buttons.add(button);
			buttonsMap.put(algorithm.getName(), button);
		}
		
	    // Add radio buttons to group so they are mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    for (JRadioButton button : buttons)
	    {
	    	group.add(button);
	    }
				
		JPanel algoChoicePanel = new JPanel();
		algoChoicePanel.setLayout(new BoxLayout(algoChoicePanel, BoxLayout.Y_AXIS));
		algoChoicePanel.setBorder(BorderFactory.createTitledBorder("Denoising Algorithm"));
		for (JRadioButton button : buttons)
		{
			algoChoicePanel.add(button);
		}
		algoChoicePanel.add(Box.createVerticalGlue());
		
		return algoChoicePanel;
	}
	
	private JPanel createAlgorithmParametersPanel(Algorithm[] algorithms)
	{		
		for (Algorithm algorithm : algorithms)
		{
			algorithm.getPanel().addEventListener((DenoiseParamsChangeEvent) -> { updateDenoisedPreview(); });	
		}
		
		CardLayout cardLayout = new CardLayout();
		JPanel panel = new JPanel(cardLayout);
		for (Algorithm algorithm : algorithms)
		{
			panel.add(algorithm.getPanel(), algorithm.getName().toString());
		}
		
		cardLayout.show(panel, wizard.getModel().getAlgorithm().getName().toString());
		return panel;
	}

	private JRadioButton createAlgorithmRadioButton(Algorithm algorithm)
	{
	    JRadioButton button = new JRadioButton(algorithm.getReadableName());
		
	    button.addActionListener(e -> {
	    	WizardModel model = wizard.getModel();
	    	if (model.getAlgorithm().getName() == algorithm.getName()) return;
    		((CardLayout)algorithmParamsPanel.getLayout()).show(algorithmParamsPanel, algorithm.getName().toString());
			model.setAlgorithm(algorithm.getName());
			updateDenoisedPreview();
	    });
	    
	    return button;
	}
	
	private class PreviewsPanel extends JPanel
	{	
		public PreviewsPanel(PreviewPanel origImagePanel, PreviewPanel denoisedImagePanel)
		{
			setBorder(BorderFactory.createTitledBorder("Denoising Preview"));
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			add(Box.createHorizontalGlue());
			add(Box.createHorizontalStrut(5));
			add(origImagePanel);
			add(Box.createHorizontalStrut(20));
			add(denoisedImagePanel);
			add(Box.createHorizontalStrut(5));
			add(Box.createHorizontalGlue());
		}
	}
	
	private class DenoisingTask implements Runnable
	{
		private Algorithm algorithm;
		private ImageProcessor image;
				
		public DenoisingTask(Algorithm algorithm, ImageProcessor image)
		{
			this.algorithm = algorithm;
			this.image = image.duplicate(); // deep copy

			// Note: we deep copy the noisy input image (since the denoising happens asynchronously
			// and we don't want surprises if the input image gets changed meanwhile...)
			// CHECKME: is this really needed?
		}
		
		@Override
		public void run()
		{
			// Note: this is not executed on the Java EDT.

			Denoiser denoiser = algorithm.getDenoiserCopy();
			denoiser.setImage(image);
			
			try
			{
				// Note: we have to copy the cache key and value (the denoising parameters object and preview image object)
				// to ensure they are not modified after we stored them in the cache.
				DenoisePreviewCacheKey cacheKey = new DenoisePreviewCacheKey(algorithm);
				DenoisePreviewCacheValue cached = previewCache.get(cacheKey);
				
				if (cached != null)
				{
					BufferedImage cachedImage = cached.denoisedPreview;
					float blurEstimate = cached.blurEstimate;
					float noiseEstimate = cached.noiseEstimate;

					BufferedImage imageCopy = ImageUtils.deepCopy(cachedImage); // copy image, to avoid it losing it if it gets ejected from the cache before it was set on the denoisedImagePanel (CHECKME: copy really needed?)
					SwingUtilities.invokeLater(() -> { denoisedPreviewPanel.setImage(imageCopy);
					                                   denoisedPreviewPanel.setBlurEstimate(blurEstimate);
                                                       denoisedPreviewPanel.setNoiseEstimate(noiseEstimate); }); 
				}
				else
				{
					SwingUtilities.invokeLater(() -> { denoisedPreviewPanel.imagePanel.setBusy(true); });
					
					// Denoise the preview
					ImageProcessor denoisedImageProcessor = QExecutor.getInstance().submit(denoiser).get();
					ImageUtils.CopyDisplayRange(image, denoisedImageProcessor);
					BufferedImage denoisedImage = denoisedImageProcessor.getBufferedImage();
					
					// Estimate noise in the denoised preview.
					float noiseEstimate = QExecutor.getInstance().submit(new NoiseEstimator(denoisedImageProcessor)).get();
					
					// Estimate blur in the denoised preview.
					float blurEstimate = QExecutor.getInstance().submit(new BlurEstimator(denoisedImageProcessor)).get();
					
					// Note: right now we always estimate noise and blur, even if the user decided not to show it in the user interface.
					// So perhaps we should not calculate it in that case? It would make the caching mechanism a bit more complex though.
					
					// Cache image and noise and blur estimates.
					DenoisePreviewCacheValue cacheValue = new DenoisePreviewCacheValue(denoisedImage, noiseEstimate, blurEstimate);
					previewCache.put(cacheKey, cacheValue);
                    
					// Update UI
					SwingUtilities.invokeLater(() -> { denoisedPreviewPanel.imagePanel.setBusy(false); 
					                                   denoisedPreviewPanel.setImage(cacheValue.denoisedPreview);
                                                       denoisedPreviewPanel.setBlurEstimate(blurEstimate);
                                                       denoisedPreviewPanel.setNoiseEstimate(noiseEstimate); });
				}
			}
			catch (InterruptedException | ExecutionException e)
			{
				e.printStackTrace();
			}			
		}
	}
	
	private void updateNoisyPreview()
	{
		// Calculate an estimate for the amount of noise and blur in the user-selected region-of-interest
		// on the original (=noisy) image.
		
		try
		{	
			ImageProcessor noisyImageProcessor = wizard.getModel().getNoisyPreview();
			float noiseEstimate = QExecutor.getInstance().submit(new NoiseEstimator(noisyImageProcessor)).get();
			float blurEstimate = QExecutor.getInstance().submit(new BlurEstimator(noisyImageProcessor)).get();
			origPreviewPanel.setNoiseEstimate(noiseEstimate);
			origPreviewPanel.setBlurEstimate(blurEstimate);
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}			
	}
	
	private void updateDenoisedPreview()
	{
		// Denoise the user-selected region-of-interest.
		
		assert(SwingUtilities.isEventDispatchThread()); // so we can't do any time consuming work here

		// Run the denoising preview on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the denoising
		// preview image in the Java Event Dispatch Thread (EDT).
		//                                                                      
		// Note: if Quasar is busy already, no more than 1 additional denoising task will be queued
		// and newer tasks will replace older queued tasks. This avoids building up a Quasar work backlog
		// but still guarantees that the denoised preview will correspond to the latest parameters chosen by the user.
		
		WizardModel model = wizard.getModel();
		DenoisingTask task = new DenoisingTask(model.getAlgorithm(), model.getNoisyPreview());
		saturatingExecutor.Submit(task);                                                                    	
	}
	
	@Override
	public void goingToNextPage() 
	{
		// Nothing to be done.
		// Model contains required denoising parameters for use in next page.
	}
	
	@Override
	public void goingToPreviousPage()
	{
		// Nothing to be done
	}

	@Override
	public void arriveFromNextPage() 
	{
		// Nothing to be done
	}
	
	@Override
	public void arriveFromPreviousPage()
	{
		WizardModel model = wizard.getModel();
		assert(model.getImage() != null);
				
		// Always clear the cache, just in case the user switched to a different image or ROI.
		previewCache.clear();
		
		JRadioButton button = buttonsMap.get(model.getAlgorithm().getName());
		button.setSelected(true);
		((CardLayout)algorithmParamsPanel.getLayout()).show(algorithmParamsPanel, model.getAlgorithm().getName().toString());

		BufferedImage noisyPreview = model.getNoisyPreview().getBufferedImage();		
		origPreviewPanel.setImage(noisyPreview);
		denoisedPreviewPanel.setImage(noisyPreview);  // To avoid an ugly empty image, temporarily show the noisy preview until we've calculated the denoised one
		
		updateNoisyPreview();
		updateDenoisedPreview();
		
		// Ask layout manager to resize the dialog so it looks nice
		wizard.pack();
	}	
}
