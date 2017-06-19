package be.vib.imagej;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
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
	
	private JPanel algoParamsPanel;
		
	private ImagePanel origImagePanel;
	private ImagePanel denoisedImagePanel;
	
	private PreviewPanel previewPanel;
	
	// We maintain a cache of 100 denoised results for different parameter settings.
	// Assuming a 512x512 ROI and 8 bit/pixel grayscale previews, a full cache requires
	// about 100 * (1 MB / 4) = 25 MB storage. This seems acceptable.
	private DenoisePreviewCache previewCache = new DenoisePreviewCache(100);
	
 	public WizardPageDenoisingAlgorithm(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		buildUI(model.getAlgorithms());		
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
		for (Algorithm algorithm : algorithms)
		{
			buttons.add(createAlgorithmRadioButton(algorithm));
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
			algorithm.getPanel().addEventListener((DenoiseParamsChangeEvent) -> { recalculateDenoisedPreview(); });	
		}
		
		CardLayout cardLayout = new CardLayout();
		JPanel panel = new JPanel(cardLayout);
		for (Algorithm algorithm : algorithms)
		{
			panel.add(algorithm.getPanel(), algorithm.getName().name());
		}
		
		cardLayout.show(panel, model.getAlgorithm().getName().name());
		return panel;
	}

	private JRadioButton createAlgorithmRadioButton(Algorithm algorithm)
	{
	    JRadioButton button = new JRadioButton(algorithm.getReadableName());
	    button.setSelected(algorithm.getName() == model.getAlgorithm().getName());
		
	    button.addActionListener(e -> {
	    	if (model.getAlgorithm().getName() == algorithm.getName()) return;
    		((CardLayout)algoParamsPanel.getLayout()).show(algoParamsPanel, algorithm.getName().name());
			model.setAlgorithm(algorithm.getName());
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
				BufferedImage cachedImage = previewCache.get(cacheKey);
				
				if (cachedImage != null)
				{
					BufferedImage imageCopy = ImageUtils.deepCopy(cachedImage); // copy image, to avoid it losing it if it gets ejected from the cache before it was set on the denoisedImagePanel (CHECKME: copy really needed?)
					SwingUtilities.invokeLater(() -> { denoisedImagePanel.setImage(imageCopy); }); 
				}
				else
				{
					SwingUtilities.invokeLater(() -> { denoisedImagePanel.setBusy(true); });
					
					ImageProcessor denoisedImageProcessor = QExecutor.getInstance().submit(denoiser).get();
					ImageUtils.CopyDisplayRange(image, denoisedImageProcessor);
					
					BufferedImage denoisedImage = denoisedImageProcessor.getBufferedImage();   // TODO: check what happens to quasar::exception_t if thrown from C++ during the denoiser task.
					
					SwingUtilities.invokeLater(() -> { previewCache.put(cacheKey, denoisedImage);
                                                       denoisedImagePanel.setImage(denoisedImage);
                                                       denoisedImagePanel.setBusy(false); });
				}
			}
			catch (InterruptedException | ExecutionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	private void recalculateDenoisedPreview()
	{
		// Run the denoising preview on a separate worker thread and return here immediately.
		// Once denoising has completed, the worker will automatically update the denoising
		// preview image in the Java Event Dispatch Thread (EDT).
		//                                                                      
		// Note: if Quasar is busy already, no more than 1 additional denoising task will be queued
		// and newer tasks will replace older queued tasks. This avoids building up a Quasar work backlog
		// but still guarantees that the denoised preview will correspond to the latest parameters chosen by the user.
		
		assert(SwingUtilities.isEventDispatchThread()); // so we can't do any time consuming work here
		
		DenoisingTask task = new DenoisingTask(model.getAlgorithm(), model.getNoisyPreview());
		saturatingExecutor.Submit(task);                                                                    	
	}

	private static JPanel addTitle(JPanel panel, String title)
	{
		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		panel.setAlignmentX(CENTER_ALIGNMENT);
		
		box.add(titleLabel);
		box.add(panel);
		box.add(Box.createVerticalStrut(10)); // title label introduces some space at the top, also leave some space at the bottom for visual symmetry
		
		return box;
	}
	
	@Override
	protected void aboutToShowPanel()
	{
		assert(model.getImage() != null);

		// Always clear the cache, just in case the user switched to a different image or ROI.
		// IMPROVEME: We could be more precise. We now occasionally clear the cache when it's not needed.
		previewCache.clear();
		
		BufferedImage noisyPreview = model.getNoisyPreview().getBufferedImage();
				
		origImagePanel.setImage(noisyPreview);
		denoisedImagePanel.setImage(noisyPreview);  // To avoid an ugly empty image, temporarily show the noisy preview until we've calculated the denoised one
		
		recalculateDenoisedPreview();
		
		// Ask layout manager to resize the dialog so it looks nice
		wizard.pack();
	}
	
}
