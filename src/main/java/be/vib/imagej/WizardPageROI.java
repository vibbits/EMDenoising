package be.vib.imagej;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

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

// TODO: this class needs cleaning up (we probably want just a single warning label whose text changes, etc.)

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
	private JLabel roiWarningLabel;
	private JLabel bitDepthWarningLabel;
	
	private Component spacer;
		
	public WizardPageROI(Wizard wizard, WizardModel model, String name)
	{
		super(wizard, model, name);
		
		buildUI();
		
		ImagePlus.addImageListener(this);
		ij.gui.Roi.addRoiListener(this);
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
			
			// FIXME? The very first panel in the wizard does not get the AboutToShowPanel()
			//        (because the wizard only calls it when the next/prev buttons are pressed...)
			//        We probably want to fix that. When it is fixed, possibly some explicit initialization code
			//       here would get called automatically (?).

			// If there are already open images, then select the one that is currently active in ImageJ
			// in the combo box.
			if (imagesCombo.getItemCount() > 0)
			{
				String imageTitle = ij.WindowManager.getCurrentImage().getTitle();
				imagesCombo.setSelectedItem(imageTitle);
				assert(imagesCombo.getSelectedItem().equals(imageTitle));  // assert that image was indeed in the list
			}
			
			updateInfo();
		}
		
		private void buildUI()
		{
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Noisy Input Image"), new EmptyBorder(10, 10, 10, 10)));
			
			imageLabel = new JLabel("Image:");

			imagesModel = new DefaultComboBoxModel<String>(getOpenImages());

			imagesCombo = new JComboBox<String>(imagesModel);
			imagesCombo.addActionListener(e -> {
				String image = (String)imagesCombo.getSelectedItem();
				// CHECKME: what does this return if the combo box does not contain any elements?
				// CHECKME: is it possible to have nothing selected, even when there are elements in the combo box?
				System.out.println("Images combo: selected item = " + image);
				model.imagePlus = ij.WindowManager.getImage(image);
				
				updateInfo();
				wizard.updateButtons();
			});

			
			// FIXME: the combo box with the open images is too wide, try to give it the size corresponding to the largest image name (but no larger than the max. window size).
			
			
			bitDepthLabel = new JLabel("Bit depth:");
			bitDepthInfoLabel = new JLabel();

			roiLabel = new JLabel("ROI:");
			roiInfoLabel = new JLabel();
			
			imageWarningLabel = new JLabel(htmlAttention("Please open the image or image stack that you want to denoise."));
			roiWarningLabel = new JLabel(htmlAttention("Please select a region of interest (ROI) on the image. The ROI will be used to preview the effect of the denoising algorithms."));
			bitDepthWarningLabel = new JLabel(htmlAttention("Please convert the image to 8 or 16 bit / pixel. Other bit depths are not supported."));
			     // TODO: Mention the current bit depth in the warning message
			
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
			roiWarningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(panel);
			add(spacer);
			add(imageWarningLabel);
			add(bitDepthWarningLabel);
			add(roiWarningLabel);			
		}
	}
	
	private void updateInfo()
	{		
		boolean haveImages = imagesCombo.getItemCount() > 0;
		
		boolean haveImage = (model.imagePlus != null);
		boolean showBitDepthWarning = haveImage && !(model.imagePlus.getBitDepth() == 8 || model.imagePlus.getBitDepth() == 16);
		boolean showBitDepthInfo = haveImage && (model.imagePlus.getBitDepth() == 8 || model.imagePlus.getBitDepth() == 16);
		
		boolean haveSupportedImage = haveImage && (model.imagePlus.getBitDepth() == 8 || model.imagePlus.getBitDepth() == 16);
		boolean showRoiWarning = haveSupportedImage && (model.imagePlus.getRoi() == null || model.imagePlus.getRoi().getBounds().isEmpty());
		boolean showRoiInfo = haveSupportedImage && !(model.imagePlus.getRoi() == null || model.imagePlus.getRoi().getBounds().isEmpty());
		
		if (showBitDepthInfo)
		{
			bitDepthInfoLabel.setText(model.imagePlus.getBitDepth() + " bit / pixel");
		}
		
		if (showRoiInfo)
		{
			Rectangle r = model.imagePlus.getRoi().getBounds();
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
		roiWarningLabel.setVisible(showRoiWarning);
		
		spacer.setVisible(showBitDepthWarning || showRoiWarning);
	}
	
	@Override
	public void imageOpened(ImagePlus imp) // not called on the EDT
	{
		assert(imp != null);
		
		// Called when user does:
		// - File > New
		// - File > Open
		// - File > Open Recent
		// - ...?
		
		System.out.println(">>> imageOpened " + (imp != null ? imp.getTitle() : "null") + " EDT? " + SwingUtilities.isEventDispatchThread());
		
		printOpenImages();
		imagesModel.addElement(imp.getTitle());

		SwingUtilities.invokeLater(() -> {
			updateInfo();
			wizard.updateButtons();
		});
	}

	@Override
	public void imageClosed(ImagePlus imp) // not called on the EDT
	{
		assert(imp != null);
		
		System.out.println(">>> imageClosed " + (imp != null ? imp.getTitle() : "null") + " EDT? " + SwingUtilities.isEventDispatchThread());

		printOpenImages();
		
		imagesModel.removeElement(imp.getTitle());

		SwingUtilities.invokeLater(() -> {
			updateInfo();
			wizard.updateButtons();
		});
	}

	@Override
	public void imageUpdated(ImagePlus imp) // not called on the EDT
	{
		// Note: imageUpdated() is called on these occasions:
		//       - when the user changes the bit-depth or the type of the image via Fiji > Image > Type > ...
		//       - when the user moves to a different slice in an image stack
		//       - when the user opens a new image
		//       - ...
		
		System.out.println(">>> imageUpdated " + (imp != null ? imp.getTitle() : "null"));

		printOpenImages();
		
		// Note: imageUpdated() is not called from the Java EDT.
		// That's why we use invokeLater() to make sure we update the wizard GUI on the EDT.		
		SwingUtilities.invokeLater(() -> {
			updateInfo();
			wizard.updateButtons();
		});
	}
	
	@Override
	public void roiModified(ImagePlus imp, int id) // called on the EDT
	{
		assert(SwingUtilities.isEventDispatchThread());
		
//		System.out.println("roiModified " + (imp != null ? imp.getTitle() : "null") + " id:" + id);
		assert(imp != null);
		
		if (imp != model.imagePlus)
			return;  // We're not interested in ROI changes for an image that the user did not select for denoising
						
		updateInfo();
		wizard.updateButtons();
	}
	
	@Override
	protected void aboutToShowPanel() // called on the EDT
	{
		assert(SwingUtilities.isEventDispatchThread());

		printOpenImages();		
		updateInfo();
		//wizard.updateButtons();
	}
	
	private static void printOpenImages()
	{
		System.out.println("Open images: ");
		for (String image : getOpenImages())
		{
			System.out.println("   " + image);
		}	
	}

	private static String[] getOpenImages()
	{
		int numImages = ij.WindowManager.getImageCount();

		String[] images = new String[numImages];
		
		for (int i = 0; i < numImages; i++)
		{
			ImagePlus imp = ij.WindowManager.getImage(i + 1);
			images[i] = imp.getTitle();
		}
		return images;
	}
	
	private static String htmlAttention(String s)
	{
		return "<html><font color=red>" + s + "</font></html>";
	}
	
	@Override
	protected boolean canGoToNextPage()
	{		
		return (model.imagePlus != null) &&
			   (model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty());
	}
}
