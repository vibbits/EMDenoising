package be.vib.imagej;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ij.ImagePlus;
import ij.ImageListener;
import ij.gui.RoiListener;


public class WizardPageROI extends WizardPage implements ImageListener, RoiListener
{		
	private JLabel image;
	private JLabel bitDepth;
	private JLabel roi;
	
	public WizardPageROI(WizardModel model, String name)
	{
		super(model, name);
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
			
			image = new JLabel();
			bitDepth = new JLabel();
			roi = new JLabel();
			
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
				           .addComponent(image)
				           .addComponent(bitDepth)
			      		   .addComponent(roi))
			);
			
			layout.setVerticalGroup(
			   layout.createSequentialGroup()
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(imageLabel)
			    		   .addComponent(image))
			      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(bitDepthLabel)
			    		   .addComponent(bitDepth))
	 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			    		   .addComponent(roiLabel)
			    		   .addComponent(roi))
			);		
			
			setLayout(layout);
		}
	}
	
	private void updateImageInfo()
	{
//		System.out.println("updateImageInfo EDT? " + SwingUtilities.isEventDispatchThread());
		if (model.imagePlus == null)
		{
			image.setText(htmlAttention("not available"));
			bitDepth.setText(htmlAttention("not available"));
		}
		else
		{
			image.setText(model.imagePlus.getTitle());
			bitDepth.setText(model.imagePlus.getBitDepth() == 8 ? model.imagePlus.getBitDepth() + " bit / pixel" : htmlAttention(model.imagePlus.getBitDepth() + " bit / pixel"));
		}
	}

	private void updateRoiInfo()
	{
//		System.out.println("updateRoiInfo EDT? " + SwingUtilities.isEventDispatchThread());
		if (model.imagePlus != null && model.imagePlus.getRoi() != null && !model.imagePlus.getRoi().getBounds().isEmpty())
		{
			roi.setText(model.imagePlus.getRoi().getBounds().toString());
		}
		else
		{
			roi.setText(htmlAttention("not available"));
		}
	}

	@Override
	public void imageOpened(ImagePlus imp)
	{
		System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
		System.out.println("imageOpened " + (imp != null ? imp.getTitle() : "null"));
		// TODO Auto-generated method stub		
	}


	@Override
	public void imageClosed(ImagePlus imp)
	{
		System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
		System.out.println("imageClosed " + (imp != null ? imp.getTitle() : "null"));
		if (imp == model.imagePlus)
		{
			model.imagePlus = null;
		}
	}

	@Override
	public void imageUpdated(ImagePlus imp)
	{
		// This is not called from the Java EDT, so direct calls to Swing widgets will *not* happen
		// immediately.

//		System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
//		System.out.println("imageUpdated " + (imp != null ? imp.getTitle() : "null"));

		// Does this get called when the user changes the bit-depth of the image?
		
		if (model.imagePlus != imp)
			model.imagePlus = imp;
		
		SwingUtilities.invokeLater(() -> {
			updateImageInfo();
			updateRoiInfo();
		});
	}

	@Override
	public void roiModified(ImagePlus imp, int id)
	{
		// This gets called from the Java EDT (Event Dispatching Thread),
		// I presume because it is sent from a Swing UI element.
		
//		System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
//		System.out.println("roiModified " + (imp != null ? imp.getTitle() : "null" + " id:" + id));
		if (imp == null)
			return;

		model.roi = imp.getRoi() != null ? imp.getRoi().getBounds() : null;
		updateRoiInfo();
	}
	
	private static String htmlAttention(String s)
	{
		return "<html><font color=red>" + s + "</font></html>";
	}
	
}
