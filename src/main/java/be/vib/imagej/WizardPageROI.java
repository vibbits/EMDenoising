package be.vib.imagej;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.RoiListener;


public class WizardPageROI extends WizardPage implements ImageListener, RoiListener
{		
	private JLabel imageInfoLabel;
	private JLabel bitDepthInfoLabel;
	private JLabel roiInfoLabel;
	
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
			
			imageInfoLabel = new JLabel();
			bitDepthInfoLabel = new JLabel();
			roiInfoLabel = new JLabel();
			
			updateImageInfo();
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
				           .addComponent(imageInfoLabel)
				           .addComponent(bitDepthInfoLabel)
			      		   .addComponent(roiInfoLabel))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(imageLabel)
			    		   .addComponent(imageInfoLabel))
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
		System.out.println("updateImageInfo EDT? " + SwingUtilities.isEventDispatchThread());
		if (model.imagePlus == null)
		{
			imageInfoLabel.setText(htmlAttention("not available"));
			bitDepthInfoLabel.setText(htmlAttention("not available"));
		}
		else
		{
			imageInfoLabel.setText(model.imagePlus.getTitle());
			bitDepthInfoLabel.setText(model.imagePlus.getBitDepth() == 8 ? model.imagePlus.getBitDepth() + " bit / pixel"
					                                                     : htmlAttention(model.imagePlus.getBitDepth() + " bit / pixel, please convert to 8 bit / pixel"));
		}
	}

	private void updateRoiInfo()
	{
		System.out.println("updateRoiInfo EDT? " + SwingUtilities.isEventDispatchThread());
		if (model.imagePlus != null && model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty())
		{
			Rectangle r = model.imagePlus.getRoi().getBounds();
			roiInfoLabel.setText("x=" + r.x + ", y=" + r.y + ", width=" + r.width + ", height=" + r.height);
		}
		else
		{
			roiInfoLabel.setText(htmlAttention("not available, please select an ROI on the image"));
		}		
	}
	
	@Override
	public void imageOpened(ImagePlus imp)
	{
		System.out.println(">>> imageOpened " + (imp != null ? imp.getTitle() : "null") + " EDT? " + SwingUtilities.isEventDispatchThread());
		// Nothing to be done - model change handled by imageUpdated().
		// (In fact, if an image is already open (or always?), imageUpdated() is called before imageOpened().)
	}


	@Override
	public void imageClosed(ImagePlus imp)
	{
		System.out.println(">>> imageClosed " + (imp != null ? imp.getTitle() : "null") + " EDT? " + SwingUtilities.isEventDispatchThread());
		if (imp == model.imagePlus)
		{
			model.imagePlus = null;   // CHECKME: needed? Or also handled by imageUpdated() ?
			// TODO: move wizard back to this page!
		}
	}

	@Override
	public void imageUpdated(ImagePlus imp)
	{
		// This is not called from the Java EDT, so direct calls to Swing widgets will *not* happen
		// immediately. That's why we use invokeLater() to do the imageInfo and RoiInfo updates on the EDT.

		System.out.println(">>> imageUpdated " + (imp != null ? imp.getTitle() : "null" + " EDT? " + SwingUtilities.isEventDispatchThread()));

		// Note: imageUpdated() also get called when the user changes the bit-depth or the type of the image via Fiji > Image > Type > ...
		
		if (model.imagePlus != imp)
			model.imagePlus = imp;
		
		SwingUtilities.invokeLater(() -> {
			updateImageInfo();
			updateRoiInfo();
			wizard.updateButtons();
		});
	}

	@Override
	public void roiModified(ImagePlus imp, int id)
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		System.out.println("roiModified " + (imp != null ? imp.getTitle() : "null") + " id:" + id + " EDT? " + SwingUtilities.isEventDispatchThread() + " -> updateRoiInfo()");
		if (imp == null)
			return;

		model.roi = imp.getRoi() != null ? imp.getRoi().getBounds() : null;
		updateRoiInfo();
		wizard.updateButtons();

	}
	
	private static String htmlAttention(String s)
	{
		return "<html><font color=red>" + s + "</font></html>";
	}
	
	@Override
	protected boolean canGoToNextPage()
	{		
		return (model.imagePlus != null) &&
			   (model.imagePlus.getBitDepth() == 8) &&
			   (model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty());
	}
}
