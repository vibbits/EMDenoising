package be.vib.imagej;

import java.awt.Graphics;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;

public class ImagePanel extends JPanel
{
	private BufferedImage image;
	private int maxSize;
	
	public ImagePanel()
	{
	}
	
	public ImagePanel(BufferedImage image, int maxSize)
	{		
		setImage(image, maxSize);
		
		// For layout debugging:
		// this.setBorder(BorderFactory.createDashedBorder(null));
	}
	
	public void setImage(BufferedImage image, int maxSize)
	{
		this.image = image;
		this.maxSize = maxSize;
		
		// If the image size has changed, it also implicitly modified the
		// minimum/preferred/maximum sizes, so we must notify the layout manager.
		invalidate();
		
		// The image's content may have changed, so redraw the image.
		repaint();
	}

	private Dimension bestSize()
	{
		if (image == null)
			return new Dimension();
		
		float scale = getScale();
		int w = (int)(image.getWidth() * scale);
		int h = (int)(image.getHeight() * scale);
		return new Dimension(w, h);
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return bestSize();
	}
	
	@Override
	public Dimension getMinimumSize()
	{
		return bestSize();
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		return bestSize();
	}
	
	public float getScale()
	{
		if (image == null)
			return 1.0f;
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int actualSize = Math.max(width, height);
		
		if (actualSize <= maxSize)
		{
			return 1.0f;
		}
		else
		{
			return (float)maxSize / (float)actualSize;		
		}	
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image == null)
			return;
		
		Dimension dim = bestSize();		
		// System.out.println("ImagePanel size: " + getSize() + " bestSize: " + bestSize() + " scale: " + getScale() + "image size: " + image.getWidth() + " x " + image.getHeight());
		
		g.drawImage(image, 0, 0, dim.width, dim.height, Color.WHITE, null);  // left pixels of image are at x=0, right pixels at x=dim.width - 1 (so not at x=dim.width)
		g.drawRect(0, 0, dim.width - 1, dim.height - 1);                     // left rectangle edge is at x=0, right edge at x=dim.width - 1
	}
}
