package be.vib.imagej;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ROISelector extends ImagePanel
{
	private Rectangle roi;  // Region of interest, where (0,0) is the image's top left corner. If no ROI is selected, roi.isEmpty() == true.
	private Rectangle oldRoi;
	private Rectangle imageRect;
	
	public ROISelector(BufferedImage image, int maxSize)
	{
		super(image, maxSize);
		
		roi = new Rectangle();
		oldRoi = new Rectangle();
		imageRect = new Rectangle(0, 0, image.getWidth() - 1, image.getHeight() - 1);
		
		ROIMouseAdapter mouseAdapter = new ROIMouseAdapter();
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
    }
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (!roi.isEmpty())
		{
			g.setColor(Color.yellow);
			g.drawRect(roi.x, roi.y, roi.width, roi.height);
		}
	}	
	
	class ROIMouseAdapter extends MouseAdapter
	{
		private Point startPoint;

		@Override
		public void mousePressed(MouseEvent e)
		{
			oldRoi = roi;
			roi = new Rectangle();
			startPoint = e.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			int x = Math.min(startPoint.x, e.getX());
			int y = Math.min(startPoint.y, e.getY());
			int width = Math.abs(startPoint.x - e.getX());
			int height = Math.abs(startPoint.y - e.getY());

			roi.setBounds(x, y, width, height);
			// FIXME: when dragging outside the image, some edges of the ROI rectangle
			//        are not drawn anymore. This is ugly.
			
			repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (roi.isEmpty())
			{
				// A click without a drag clears the roi,
				// make sure to repaint this case too.
				repaint();
			}
			
			// Notify listeners of ROI change.
			// (Note that we scale window coordinates to image coordinates,
			// and also make sure that the resulting rectangle is fully within the image.)
			firePropertyChange("roi",
					           imageRect.intersection(scaleRectangle(oldRoi, getScale())), 
					           imageRect.intersection(scaleRectangle(roi, getScale())));
		}	
	}
	
	static private Rectangle scaleRectangle(Rectangle rect, float s)
	{
		Rectangle r = new Rectangle(rect);
		r.x /= s;
		r.y /= s;
		r.width /= s;
		r.height /= s;
		return r;
	}
	
}
