package be.vib.imagej;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;

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

import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.RoiListener;

public class WizardPageROI extends WizardPage implements ImageListener, RoiListener
{		
	private JLabel imageLabel;
	private JComboBox<String> imagesCombo; // lists the open images in ImageJ, the one selected in this combo box will be denoised
	private DefaultComboBoxModel<String> imagesModel; // model for imagesCombo
	
	private JLabel bitDepthLabel;
	private JLabel bitDepthInfoLabel; // the bit depth info of the image selected in imagesCombo

	private JLabel roiLabel;
	private JLabel roiInfoLabel;  // the ROI information for the image selected in imagesCombo
	
	private JLabel imageWarningLabel;
	private JLabel noRoiWarningLabel;
	private JLabel roiSizeWarningLabel;
	private JLabel bitDepthWarningLabel;
	
	private Component spacer;
		
	public WizardPageROI(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		
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

				// Unlock the previous image, if needed.
				if (model.getImage() != null && model.getImage().isLocked())
					model.getImage().unlock();
				
				// Keep a reference to the open window (if any) in our model.
				model.setImage(imageTitle != null ? ij.WindowManager.getImage(imageTitle) : null);

				// Update the UI
				updateInfo();
				wizard.updateButtons();
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
			
			updateInfo();
			wizard.updateButtons();
		}
	}
	
	private boolean haveImage()
	{
		return model.getImage() != null;
	}
	
	private boolean haveImageWithSupportedBitDepth()
	{
		return haveImage() && (model.getImage().getBitDepth() == 8 || model.getImage().getBitDepth() == 16);
	}
	
	private boolean imageHasWrongBitDepth()
	{
		return haveImage() && !(model.getImage().getBitDepth() == 8 || model.getImage().getBitDepth() == 16);
	}
	
	private boolean imageHasNoRoi()
	{
		return haveImage() && (model.getImage().getRoi() == null || model.getImage().getRoi().getBounds().isEmpty());
	}
	
	private boolean imageRoiTooLarge()
	{
		return haveImage() && model.getImage().getRoi() != null && !model.getImage().getRoi().getBounds().isEmpty() && (model.getImage().getRoi().getBounds().width > WizardModel.maxPreviewSize || model.getImage().getRoi().getBounds().height > WizardModel.maxPreviewSize);
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
			bitDepthInfoLabel.setText(model.getImage().getBitDepth() + " bit / pixel");
		}
		
		if (showRoiInfo)
		{
			Rectangle r = model.getImage().getRoi().getBounds();
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
		// - ...?
		
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
		// - changes the bit-depth or the type of the image via Fiji > Image > Type > ...
		// - moves to a different slice in an image stack
		// - opens a new image
		// - ...
		
		SwingUtilities.invokeLater(() -> { handlePreviewChange(); });
	}
	
	@Override
	public void roiModified(ImagePlus imp, int id) // called on the EDT
	{
		assert(SwingUtilities.isEventDispatchThread());
		assert(imp != null);
		
		if (imp != model.getImage())
			return;  // We're not interested in ROI changes for an image that is not currently chosen for denoising.
						
		handlePreviewChange();
	}
	
	@Override
	protected void aboutToHidePanel()
	{
		assert(SwingUtilities.isEventDispatchThread());

		// Lock the image (stack) so that if the user closes the image window,
		// the underlying image slices remain in memory.
		if (!model.getImage().isLocked())
			model.getImage().lock();
		
		// Stop listening to changes. This avoids that if the user closes the image window we also wipe out the image from the model while it is still being used by the denoising wizard page. 
		ImagePlus.removeImageListener(this);
		ij.gui.Roi.removeRoiListener(this);
	}
	
	private ImagePlus getSuggestedImageForDenoising()
	{
		ImagePlus[] openImages = getOpenImages();
		
		if (model.getImage() != null && Arrays.asList(openImages).contains(model.getImage()))
			return model.getImage();
		else
			return ij.WindowManager.getCurrentImage();		
	}
	
	@Override
	protected void aboutToShowPanel()
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		// Populate combo box
		// printOpenImages();
		imagesModel.removeAllElements();
		for (ImagePlus image : getOpenImages())
			imagesModel.addElement(image.getTitle());
				
		// If there are already open images, then select the one currently used in the wizard if any,
		// otherwise the one that is currently active in ImageJ in the combo box.
		if (imagesCombo.getItemCount() > 0)
		{
			ImagePlus image = getSuggestedImageForDenoising();
			assert(image != null);
			System.out.println("suggested for denoising: " + image);
			String imageTitle = image.getTitle();
			imagesCombo.setSelectedItem(imageTitle);
			assert(imagesCombo.getSelectedItem().equals(imageTitle));  // assert that image was indeed in the list
		}
			
		// Update UI		
		updateInfo();
		
		// Listen to changes
		ImagePlus.addImageListener(this);
		ij.gui.Roi.addRoiListener(this);
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
	protected boolean canGoToNextPage()
	{		
		return haveImage() &&
			   !imageHasWrongBitDepth() &&
			   !imageHasNoRoi() &&
			   !imageRoiTooLarge();
	}
}
