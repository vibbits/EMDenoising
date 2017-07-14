package be.vib.imagej;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import be.vib.bits.QExecutor;
import ij.ImageListener;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.RoiListener;
import ij.process.ImageProcessor;

public class WizardPageROI extends WizardPage implements ImageListener, RoiListener
{		
	private JLabel imageLabel;
	private JComboBox<String> imagesCombo; // lists the open images in ImageJ, the one selected in this combo box will be denoised
	private DefaultComboBoxModel<String> imagesModel; // model for imagesCombo
	private ImagePlus image; // image currently chosen by the user for denoising, will be remembered in the WizardModel once we leave this wizard page
	
	private JLabel bitDepthLabel;
	private JLabel bitDepthInfoLabel; // the bit depth info of the image selected in imagesCombo

	private JLabel roiLabel;
	private JLabel roiInfoLabel;  // the ROI information for the image selected in imagesCombo
	
	private JLabel imageWarningLabel;
	private JLabel noRoiWarningLabel;
	private JLabel roiSizeWarningLabel;
	private JLabel bitDepthWarningLabel;
	
	private Component spacer;
		
	public WizardPageROI(Wizard wizard, String name)
	{
		super(wizard, name);
		
		buildUI();
	}

	private void buildUI()
	{		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		InfoPanel infoPanel = new InfoPanel();
		infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, infoPanel.getMaximumSize().height));

		add(infoPanel);
		add(Box.createVerticalGlue());
	}
	
	private class InfoPanel extends JPanel
	{
		public InfoPanel()
		{
			buildUI();			
		}
		
		private void buildUI()
		{
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Noisy Input Image"), new EmptyBorder(10, 10, 10, 10)));
			
			imageLabel = new JLabel("Image:");

			imagesModel = new DefaultComboBoxModel<String>();

			imagesCombo = new JComboBox<String>(imagesModel);
			imagesCombo.addActionListener(e -> {
				// We get here not only if the user interacts with the combo box directly, but also when images are opened/closed.
				// Also, the selected item string will be null if the combo box model (imagesModel) contains no images because there are no open image windows.
				String imageTitle = (String)imagesCombo.getSelectedItem();
				image = (imageTitle != null) ? ij.WindowManager.getImage(imageTitle) : null;

				// Update the UI
				handlePreviewChange();
			});				
			
			bitDepthLabel = new JLabel("Bit depth:");
			bitDepthInfoLabel = new JLabel();

			roiLabel = new JLabel("ROI:");
			roiInfoLabel = new JLabel();
			
			imageWarningLabel = new JLabel(htmlAttention("Please open the image or image stack that you want to denoise."));
			noRoiWarningLabel = new JLabel(htmlAttention("Please select a region of interest (ROI) on the image. The ROI will be used to preview the effect of the denoising algorithms."));
			roiSizeWarningLabel = new JLabel(htmlAttention("The region of interest (ROI) is too large. Please select an ROI that is no larger than " + WizardModel.maxPreviewSize + " x " + WizardModel.maxPreviewSize + " pixels."));
			bitDepthWarningLabel = new JLabel(htmlAttention("Please convert the image to grayscale 8 or 16 bit / pixel. Other bit depths are not supported."));
			
			spacer = Box.createRigidArea(new Dimension(0, 10));
			
			JPanel panel = new JPanel();   // will hold the info, but not the warning messages
			
			GroupLayout layout = new GroupLayout(panel);
			layout.setAutoCreateGaps(true);
			
            // Note: the PREFERRED_SIZE arguments below to addComponent(imagesCombo, ...) tell the GroupLayout
			// that we want the combo box to rescale with its contents. 

			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				           .addComponent(imageLabel)
				           .addComponent(bitDepthLabel)
			      		   .addComponent(roiLabel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
				           .addComponent(imagesCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				           .addComponent(bitDepthInfoLabel)
			      		   .addComponent(roiInfoLabel))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(imageLabel)
			    		   .addComponent(imagesCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(bitDepthLabel)
			    		   .addComponent(bitDepthInfoLabel))
	 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(roiLabel)
			    		   .addComponent(roiInfoLabel))
			);		
			
			panel.setLayout(layout);
			
			// The info items will be on top, and the possible warning message at the bottom.
			// At most one warning message is visible. Zero, one, two or three info items
			// will be shown, the other info items will be hidden (and a corresponding warning message made visible).
			
			imageWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			bitDepthWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			noRoiWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			roiSizeWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(panel);
			add(spacer);
			add(imageWarningLabel);
			add(bitDepthWarningLabel);
			add(noRoiWarningLabel);			
			add(roiSizeWarningLabel);			
		}
	}
	
	private boolean haveImage()
	{
		return image != null;
	}
	
	private boolean haveImageWithSupportedBitDepth()
	{
		return haveImage() && (image.getBitDepth() == 8 || image.getBitDepth() == 16);
	}
	
	private boolean imageHasWrongBitDepth()
	{
		return haveImage() && !(image.getBitDepth() == 8 || image.getBitDepth() == 16);
	}
	
	private boolean imageHasNoRoi()
	{
		return haveImage() && (image.getRoi() == null || image.getRoi().getBounds().isEmpty());
	}
	
	private boolean imageRoiTooLarge()
	{
		return haveImage() && image.getRoi() != null && !image.getRoi().getBounds().isEmpty() && (image.getRoi().getBounds().width > WizardModel.maxPreviewSize || image.getRoi().getBounds().height > WizardModel.maxPreviewSize);
	}
	
	private void updateInfo()
	{	
		boolean haveImages = imagesCombo.getItemCount() > 0;
		
		boolean haveSupportedImage = haveImageWithSupportedBitDepth();

		boolean showBitDepthInfo = haveSupportedImage;
		boolean showBitDepthWarning = imageHasWrongBitDepth();
		
		boolean showNoRoiWarning = haveSupportedImage && imageHasNoRoi();
		boolean showRoiSizeWarning = haveSupportedImage && imageRoiTooLarge();
		boolean showRoiInfo = haveSupportedImage && !showNoRoiWarning;
		
		if (showBitDepthInfo)
		{
			bitDepthInfoLabel.setText(image.getBitDepth() + " bit / pixel");
		}
		
		if (showRoiInfo)
		{
			Rectangle r = image.getRoi().getBounds();
			roiInfoLabel.setText(r.width + " x " + r.height + " pixels, top left corner at (" + r.x + ", " + r.y + ")");
		}	

		imageLabel.setVisible(haveImages);
		imagesCombo.setVisible(haveImages);
		imageWarningLabel.setVisible(!haveImages);
			
		bitDepthLabel.setVisible(showBitDepthInfo);
		bitDepthInfoLabel.setVisible(showBitDepthInfo);	
		bitDepthWarningLabel.setVisible(showBitDepthWarning);

		roiInfoLabel.setVisible(showRoiInfo);
		roiLabel.setVisible(showRoiInfo);
		noRoiWarningLabel.setVisible(showNoRoiWarning);
		roiSizeWarningLabel.setVisible(showRoiSizeWarning);
		
		spacer.setVisible(showBitDepthWarning || showNoRoiWarning || showRoiSizeWarning);
	}
	
	@Override
	public void imageOpened(ImagePlus imp) // not called on the EDT
	{
		assert(imp != null);
		
		// imageOpened() is called when user does:
		// - File > New
		// - File > Open
		// - File > Open Recent
		// - ...
		
		imagesModel.addElement(imp.getTitle());
		SwingUtilities.invokeLater(() -> { handlePreviewChange(); });
	}

	@Override
	public void imageClosed(ImagePlus imp) // not called on the EDT
	{
		assert(imp != null);
		
		imagesModel.removeElement(imp.getTitle());
		SwingUtilities.invokeLater(() -> { handlePreviewChange(); });
	}

	@Override
	public void imageUpdated(ImagePlus imp) // not called on the EDT
	{
		// imageUpdated() is called when the user
		// - changes the bit-depth or the type of the image (Fiji > Image > Type > ...)
		// - inverts the image (Fiji > Edit > Invert)
		// - changes brightness or contrast (Fiji > Image > Adjust > Brightness/Contrast...)
		// - moves to a different slice in an image stack
		// - opens a new image
		// - ...
		
		SwingUtilities.invokeLater(() -> { handlePreviewChange(); });
	}
	
	@Override
	public void roiModified(ImagePlus img, int id) // called on the EDT
	{
		// Note: - For a simple rectangular ROI we have img != null.
		//       - When creating a ROI that is the union of rectangles, imp != null while shift-dragging a second rectangle,
		//         but imp == null when we stop dragging and we are notified about the creation of this second rectangle.
		//         However, we never get a notificaion of the creation of the composition ROI consisting of the union
		//         of both rectangles?! As a workaround we call handlePreviewChange() via invokeLater(), so it gets
		//         executed after ImageJ eventually sets the composite ROI on the image. Our UI will then correctly
		//         show the bounding box of the union ROI.
		
		assert(SwingUtilities.isEventDispatchThread());
		
		if (img != null && img != image)
			return;  // We're not interested in ROI changes for an image that is not currently chosen for denoising.
						
		SwingUtilities.invokeLater(() -> { handlePreviewChange(); });
	}

	@Override
	public void goingToNextPage() 
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		// Stop listening to changes. This avoids that if the user closes the image window we also wipe
		// out the image from the model while it is still being used by the denoising wizard page. 
		ImagePlus.removeImageListener(this);
		ij.gui.Roi.removeRoiListener(this);
		
		WizardModel model = wizard.getModel();
				
		// Update model image
		assert(image != null);
		model.setImage(image);
		
		// Use the region of interest as a little preview image.
		ImageProcessor preview = ImageUtils.cropImage(model.getImage());
		model.setNoisyPreview(preview);

		// Set default denoising parameters (and ranges) based on an estimate
		// of the noise level of our input image.
		float noise = model.getNoiseEstimate();
		if (noise < 0)
		{
			noise = estimateNoise(model.getImage());
			model.setNoiseEstimate(noise);
			
			if (noise >= 0)
			{
				for (Algorithm algorithm : model.getAlgorithms())
					algorithm.setDefaultParameters(noise);
			}
		}	
	}
	
	@Override
	public void goingToPreviousPage()
	{
		// Going to back to the Quasar initialization page - nothing to be done
	}

	@Override
	public void arriveFromNextPage() 
	{
		setupPage();
	}
	
	@Override
	public void arriveFromPreviousPage()
	{
		setupPage();
	}	
	
	private void setupPage()
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		// Unlock whatever image we had in the model so far
		// since we're about to select a new one (possibly).
		// At least in this unlocked state the user can
		// modify (crop, select a different slice,...) the image stack.
		wizard.getModel().lockImage(false);

		// Guess likely image for denoising from a set of open images.
		// Do this now, before we update the combo box which changes the model.
		ImagePlus suggestedImage = getSuggestedImageForDenoising();

		// Populate combo box
		imagesModel.removeAllElements();
		for (ImagePlus image : getOpenImages())
			imagesModel.addElement(image.getTitle());
				
		// If there are already open images, then select the one currently used in the wizard if any,
		// otherwise the one that is currently active in ImageJ in the combo box.
		if (imagesCombo.getItemCount() > 0)
		{
			assert(suggestedImage != null);
			String imageTitle = suggestedImage.getTitle();
			imagesCombo.setSelectedItem(imageTitle);
			assert(imagesCombo.getSelectedItem().equals(imageTitle));  // assert that image was indeed in the list
		}
			
		// Update UI		
		handlePreviewChange();
		
		// Listen to changes
		ImagePlus.addImageListener(this);
		ij.gui.Roi.addRoiListener(this);
	}
	
	private static float estimateNoise(ImagePlus image)
	{
		float noise = -1.0f; // noise level unknown still
		
		try
		{				
			int slice = image.getCurrentSlice();
			ImageStack stack = image.getStack();
			ImageProcessor imp = stack.getProcessor(slice);
			
			noise = QExecutor.getInstance().submit(new NoiseEstimator(imp)).get();
			System.out.println("Estimated (normalized) std deviation of noise in " + image.getTitle() + " = " + noise);
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
		
		return noise;
	}

	// Guess the image that the user probably wants to denoise
	// (preferably the image in our model)
	// or null if no image is currently open.
	private ImagePlus getSuggestedImageForDenoising()
	{
		ImagePlus[] openImages = getOpenImages();	
		
		WizardModel model = wizard.getModel();

		if (model.getImage() != null && Arrays.asList(openImages).contains(model.getImage()))
			return model.getImage();
		else
			return ij.WindowManager.getCurrentImage();		
	}
	
	private void handlePreviewChange()  // must be called on the EDT
	{
		updateInfo();
		wizard.updateButtons();
	}
	
	// Prints the names of all open images. For debugging.
	private static void printOpenImages()
	{
		System.out.println("Open images: ");
		for (ImagePlus image : getOpenImages())
		{
			System.out.println("   " + image.getTitle());
		}	
	}

	// Returns an array with the title strings of all open images.
	private static ImagePlus[] getOpenImages()
	{
		int numImages = ij.WindowManager.getImageCount();

		ImagePlus[] images = new ImagePlus[numImages];
		
		for (int i = 0; i < numImages; i++)
		{
			images[i] = ij.WindowManager.getImage(i + 1);
		}
		return images;
	}
	
	// Wraps an ordinary text string in HTML for rendering it as red text. 
	private static String htmlAttention(String s)
	{
		return "<html><font color=red>" + s + "</font></html>";
	}
	
	@Override
	public boolean canGoToNextPage()
	{		
		return haveImage() &&
			   !imageHasWrongBitDepth() &&
			   !imageHasNoRoi() &&
			   !imageRoiTooLarge();
	}
}
