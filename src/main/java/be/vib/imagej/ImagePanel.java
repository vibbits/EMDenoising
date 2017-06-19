package be.vib.imagej;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ImagePanel extends JPanel implements ActionListener
{
	private BufferedImage image;
	private final JPanel parent;
	private boolean busyIndicator;
	private Timer timer;
	private final static int busyIndicationDelay = 500; // Only show the busy indication after 500 ms. This avoids showing it for short tasks, and flashing when many short tasks are executed quickly after one another.
	
	public ImagePanel(JPanel parent)
	{
		this.parent = parent;
		this.image = null;
		this.busyIndicator = false;
		this.timer = new Timer(busyIndicationDelay, this); // timer to switch on the busy indication after a little delay
	}
	
	// For layout debugging:
	// this.setBorder(BorderFactory.createDashedBorder(null));
	
	// Interesting: Deven_C_Miller's answer in
	// http://stackoverflow.com/questions/2155351/swing-jpanel-wont-repaint
	
	
	public void setImage(BufferedImage image)
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		this.image = image;
		
		Dimension size = new Dimension(image.getWidth(), image.getHeight());
		setPreferredSize(size);
		
		// Image size may have changed, so we must notify the layout manager.
		invalidate();
		
		// The image's content may have changed, so redraw the image.
		repaint();
		
		// Trigger a redraw of the parent panel of ImagePanel to redraw itself (and all its descendants).
		parent.validate();
	}

	
	public void setBusy(boolean busy)
	{
		assert(SwingUtilities.isEventDispatchThread());

		if (busy)
		{
			timer.restart();
		}
		else
		{
			timer.stop();
			
			if (busyIndicator == true)
			{
				busyIndicator = false;
				repaint();
			}
		}
	
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
		assert(SwingUtilities.isEventDispatchThread());

		super.paintComponent(g);
		
		if (image == null)
			return;
		
		Dimension dim = getPreferredSize();		
		g.drawImage(image, 0, 0, dim.width, dim.height, Color.WHITE, null);  
		
		// left pixels of image are at x=0, right pixels at x=dim.width - 1 (so not at x=dim.width)
		// left rectangle edge is at x=0, right edge at x=dim.width - 1
		
		if (!busyIndicator)
		{
			g.setColor(Color.BLACK);
			g.drawRect(0, 0, dim.width - 1, dim.height - 1);                     
		}
		else
		{
			g.setColor(Color.RED);
			g.drawRect(0, 0, dim.width - 1, dim.height - 1);
			g.drawRect(1, 1, dim.width - 3, dim.height - 3);    			
			g.drawRect(2, 2, dim.width - 5, dim.height - 5); 		
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		timer.stop();
		
		busyIndicator = true;
		repaint();
	}
}
