package be.vib.imagej;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.RoiListener;

// Some design choices. They differ from the current (2016/11/30) implementation.
//
// While in the WizardPageROI page:
// - wizard tracks which image (window) ir currently active
// - user is free to change ROI and image window as much as she likes
// - image and roi checks are based on that active window;
//   if checks are ok, user can move to the WizardPageDenoisingAlgorithm
// - an roi must be selected (this is enforced to be able to "educate" the user in this first wizard panel).
//   If the user later (when we are in a subsequent wizard page) removes the ROI, we will automatically define a default one and display a short warning somewhere in the UI.
//
// When changing to the WizardPageDenoisingAlgorithm:
// - A reference to the currently active image is (already) stored in the WizardModel.
// - The ROI is *not* stored separately in the WizardModel. We will always get the latest one from the ImagePlus object.
//
// While in the WizardPageDenoisingAlgorithm:
// - Switching to a different image window has no effect. The plugin keeps using the image that was most recently active while the WizardPageROI was being shown.
// - Changes to the ROI on model.imagePlus *are* honored.
//   The WizardPageDenoisingAlgorithm tracks model.imagePlus ROI changes (and ignores ROI changes to all other images).
//   If the ROI on WizardModel.imagePlus is changed, the WizardPageDenoisingAlgorithm needs to update it GUI.
//   (Also recalculate a cropped section of the imagePlus, store in in WizardModel.previewOrigROI and recalculate WizardModel.previewDenoisedROI)
//
// While in the WizardPageDenoising:
// - ROI changes are irrelevant (only needed for the preview)
// - Changing the active image has no effect on the plugin, we only use the reference we stored in model.imagePlus while WizardPageROI was visible.
// - When moving back to WizardPageDenoisingAlgorithm (say, to denoise using a different algorithm or set of parameters)
//   the model.imagePlus is *not* updated to the currently active image. Only when the user moves back to WizardPageROI
//   is WizardModel.imagePlus updated to the currently active window.
//
// To clarify the fact that WizardPageROI tracks changes to both image and ROI, maybe it's title should reflect this too:
//    "Select image and ROI" instead of just "Select ROI"
//
// The EM denoising wizard should start up even without any image window open. It tracks images anyway.
// (Currently it uses @Parameter ImagePlus to enforce an open image, but this leads to an ugly and terse
// error message from ImageJ when no image is active.)
//
// Note: we may need to call ImagePlus.lockSilently() to keep the image alive while we are using it
//       (the user might (accidentally or not) close the image window).
//       see https://imagej.nih.gov/ij/source/ij/ImagePlus.java
//
// PROBLEMS:
// - if user switches between multiple open image windows, how can we detect his?

public class WizardPageROI extends WizardPage implements ImageListener, RoiListener
{		
	private JLabel bitDepthInfoLabel;                 // the bit depth info of the image selected in imagesCombo
	private JLabel roiInfoLabel;                      // the ROI information for the image selected in imagesCombo
	private JLabel noImageWarningLabel;               // label asking the user to open an image (only shown if no images are open, otherwise imagesCombo is shown)
	private JComboBox<String> imagesCombo;            // lists the open images in ImageJ, the one selected in this combo box will be denoised
	private DefaultComboBoxModel<String> imagesModel; // model for imagesCombo
	
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
	}
	
	private class InfoPanel extends JPanel
	{
		public InfoPanel()
		{
			setBorder(BorderFactory.createTitledBorder("Noisy Input Image"));
			
			JLabel imageLabel = new JLabel("Image:");
			JLabel bitDepthLabel = new JLabel("Bit depth:");
			JLabel roiLabel = new JLabel("ROI:");
			
			bitDepthInfoLabel = new JLabel();
			roiInfoLabel = new JLabel();
			noImageWarningLabel = new JLabel(htmlAttention("not available, please open an image"));
			
			imagesModel = new DefaultComboBoxModel<String>(getOpenImages());

			imagesCombo = new JComboBox<String>(imagesModel);
			imagesCombo.setMaximumSize(imagesCombo.getPreferredSize());
			imagesCombo.addActionListener(e -> {
				String image = (String)imagesCombo.getSelectedItem();
				// TODO: what does this return if the combo box does not contain any elements?
				// TODO: is it possible to have nothing selected, even when there are elements in the combo box?
				System.out.println("Images combo: selected item = " + image);
				model.imagePlus = ij.WindowManager.getImage(image);
				
				updateImageInfo();
				updateBitDepthInfo();
				updateRoiInfo();
				wizard.updateButtons();
			});

			// If there are already open images, then select the one that is currently active in ImageJ
			// in the combo box.
			if (imagesCombo.getItemCount() > 0)
			{
				String imageTitle = ij.WindowManager.getCurrentImage().getTitle();
				imagesCombo.setSelectedItem(imageTitle);
				assert(imagesCombo.getSelectedItem().equals(imageTitle));  // assert that image was indeed in the list
			}
			
			JPanel imagesPanel = new JPanel();
			imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.LINE_AXIS));  // Use BoxLayout instead of default FlowLayout so the panel doesn't expand unnecessarily
			imagesPanel.add(imagesCombo);
			imagesPanel.add(noImageWarningLabel);
			
			// FIXME? The very first panel in the wizard does not get the AboutToShowPanel()
			//        (because the wizard only calls it when the next/prev buttons are pressed...)
			//        We probably want to fix that. When it is fixed, the initialization code below
			//        will be called automatically there and can be removed.
			updateImageInfo();
			updateBitDepthInfo();
			updateRoiInfo();
			
			GroupLayout layout = new GroupLayout(this);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			
			layout.setHorizontalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
				           .addComponent(imageLabel)
				           .addComponent(bitDepthLabel)
			      		   .addComponent(roiLabel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
				           .addComponent(imagesPanel)
				           .addComponent(bitDepthInfoLabel)
			      		   .addComponent(roiInfoLabel))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(imageLabel)
			    		   .addComponent(imagesPanel))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(bitDepthLabel)
			    		   .addComponent(bitDepthInfoLabel))
	 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(roiLabel)
			    		   .addComponent(roiInfoLabel))
			);		
			
			setLayout(layout);
		}
	}
	
	private void updateImageInfo()
	{		
		boolean hasImages = imagesCombo.getItemCount() > 0;
		
		imagesCombo.setVisible(hasImages);
		noImageWarningLabel.setVisible(!hasImages);
	}
	
	private void updateBitDepthInfo()
	{				
		if (model.imagePlus == null)
		{
			bitDepthInfoLabel.setText("-");
		}
		else
		{
			if (model.imagePlus.getBitDepth() == 8)
			{
				bitDepthInfoLabel.setText("8 bit / pixel");
			}
			else
			{
				bitDepthInfoLabel.setText(htmlAttention(model.imagePlus.getBitDepth() + " bit / pixel, please convert to 8 bit / pixel"));
			}
		}
	}

	private void updateRoiInfo()
	{
		if (model.imagePlus == null || model.imagePlus.getBitDepth() != 8)
		{
			roiInfoLabel.setText("-");
		}
		else if (model.imagePlus.getRoi() == null || model.imagePlus.getRoi().getBounds().isEmpty())
		{
			roiInfoLabel.setText(htmlAttention("not available, please select an ROI on the image"));
		}	
		else
		{
			Rectangle r = model.imagePlus.getRoi().getBounds();
			roiInfoLabel.setText("x=" + r.x + ", y=" + r.y + ", width=" + r.width + ", height=" + r.height);
		}		
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
			updateImageInfo();
			updateBitDepthInfo();
			updateRoiInfo();
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
			updateImageInfo();
			updateBitDepthInfo();
			updateRoiInfo();
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
			updateImageInfo();     // CHECKME: maybe not needed
			updateBitDepthInfo();  // e.g. in case the user changed the bit depth of the image
			updateRoiInfo();       // e.g. if image was fine and an ROI was selected, and user then changes image bit depth to <> 8, we want do change the ROI to "-" instead
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
						
		updateRoiInfo();
		wizard.updateButtons();
	}
	
	@Override
	protected void aboutToShowPanel() // called on the EDT
	{
		assert(SwingUtilities.isEventDispatchThread());

		printOpenImages();
		
		System.out.println("combo pref size=" + imagesCombo.getPreferredSize());
		System.out.println("warning label pref size=" + noImageWarningLabel.getPreferredSize());
		
		System.out.println("combo pref max size=" + imagesCombo.getMaximumSize());
		System.out.println("warning label max size=" + noImageWarningLabel.getMaximumSize());
		
		updateImageInfo();
		updateBitDepthInfo();
		updateRoiInfo();
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
			   //(model.imagePlus.getBitDepth() == 8) &&
			   (model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty());
	}
}
