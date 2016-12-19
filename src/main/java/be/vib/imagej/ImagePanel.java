package be.vib.imagej;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImagePanel extends JPanel
{
	private BufferedImage image;
	private final JPanel parent;
	private String text; // Info text shown on top of the image (if non-null, if null no text is shown).
	
	public ImagePanel(JPanel parent)
	{
		this.parent = parent;
		this.text = null;
		this.image = null;
	}
	
	// For layout debugging:
	// this.setBorder(BorderFactory.createDashedBorder(null));
	
	// Interesting: Deven_C_Miller's answer in
	// http://stackoverflow.com/questions/2155351/swing-jpanel-wont-repaint
	
	public void setImage(BufferedImage image)
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		this.image = image;
		
		// If the image size has changed, it also implicitly modified the
		// minimum/preferred/maximum sizes, so we must notify the layout manager.
		invalidate();
		
		// The image's content may have changed, so redraw the image.
		repaint();
		
		// Trigger a redraw of the parent panel of ImagePanel to redraw itself (and all its descendants).
		parent.validate();
	}
	
	public void setText(String text)
	{
		System.out.println("ImagePanel.setText " + " EDT? " + SwingUtilities.isEventDispatchThread());
		this.text = text;
		repaint();
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		//System.out.println("ImagePanel.paintComponent " + image + " EDT? " + SwingUtilities.isEventDispatchThread());

		super.paintComponent(g);
		
		Dimension dim = getPreferredSize();		
		//System.out.print("ImagePanel preferred size: " + dim + " Image:" + (image == null ? "null" : image));
		
		if (image != null)
		{
			g.drawImage(image, 0, 0, dim.width, dim.height, Color.WHITE, null);  // left pixels of image are at x=0, right pixels at x=dim.width - 1 (so not at x=dim.width)
			g.drawRect(0, 0, dim.width - 1, dim.height - 1);                     // left rectangle edge is at x=0, right edge at x=dim.width - 1
		}

//      FIXME: If the text is larger than the preferred size of the window,
//             it will not be rendered nicely but will be clipped...
//	
//		if (text != null)
//		{
//			System.out.println("drawing text: '" + text + "' at " + 0 +" " + dim.height / 2);
//			FontMetrics metrics = g.getFontMetrics();
//			int x = (dim.width - metrics.stringWidth(text)) / 2;
//			int y = metrics.getHeight() + 10;
//			g.drawChars(text.toCharArray(), 0, text.length(), x, y);
//		}
	}
}
