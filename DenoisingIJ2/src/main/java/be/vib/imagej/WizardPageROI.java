package be.vib.imagej;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.RoiListener;

public class WizardPageROI extends WizardPage
                           implements PropertyChangeListener, RoiListener
{		
	private ROISelector roiSelector;
	
	private static final int maxImageSize = 512;
	
	public WizardPageROI(WizardModel model, String name)
	{
		super(model, name);
		buildUI();
	}
	
	private BufferedImage getOriginalImage()
	{
		int slice = model.imagePlus.getCurrentSlice();
		ImageStack stack = model.imagePlus.getStack();
		return stack.getProcessor(slice).getBufferedImage();		
	}
	
	private void buildUI()
	{		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		roiSelector = new ROISelector(getOriginalImage(), maxImageSize);
		roiSelector.addPropertyChangeListener(this);
		
		JLabel instructions = new JLabel("Click and drag in the image above to select a region of interest (ROI).");
		
		roiSelector.setAlignmentX(CENTER_ALIGNMENT);
		instructions.setAlignmentX(CENTER_ALIGNMENT);
		
		add(roiSelector);
		add(instructions);
		add(Box.createVerticalGlue());
		
		updateToolTip();
	}
	
	private void updateToolTip()
	{
		// Gimmick, for testing
		String toolTip = "Image: " + model.imagePlus.getWidth() + " x " + model.imagePlus.getHeight() + " " + 
		         "ROI: " + (model.roi == null || model.roi.isEmpty() ? "none" : model.roi.width + " x " + model.roi.height);
		roiSelector.setToolTipText(toolTip);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
//		System.out.println("Event source: " + event.getSource() + " Changed property: " + event.getPropertyName() + " [old -> "
//		        + event.getOldValue() + "] | [new -> " + event.getNewValue() +"]");
		
		if (event.getSource() == roiSelector && event.getPropertyName() == "roi")
		{
			model.roi = (Rectangle)event.getNewValue();
			updateToolTip();
		}
    }

	@Override
	public void roiModified(ImagePlus imp, int id)
	{
		// TODO Auto-generated method stub
		System.out.println(">>> roiModified imp=" + imp + " id=" + id);
		if (imp != null)
		{
			System.out.println("  ROI=" + imp.getRoi());
		}
	}
	
}
