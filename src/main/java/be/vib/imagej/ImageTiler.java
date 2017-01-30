package be.vib.imagej;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ij.process.ImageProcessor;


public class ImageTiler implements Iterable<ImageTile>
{
	private ImageProcessor image;

	private int tileWidthWithoutMargins;
	private int tileHeightWithoutMargins;
	
	private int margin;
	
	public ImageTiler(ImageProcessor image, int tileWidthWithoutMargins, int tileHeightWithoutMargins, int margin)
	{
		this.image = image;
		this.tileWidthWithoutMargins = tileWidthWithoutMargins;
		this.tileHeightWithoutMargins = tileHeightWithoutMargins;
		this.margin = margin;
	}

	@Override
	public Iterator<ImageTile> iterator()
	{
		return new TilesIterator();
	}
	
	// Predicts the number of tiles that will be returned by the TilesIterator
	public int getNumTiles()
	{
		int rows = (image.getHeight() + tileHeightWithoutMargins - 1 ) / tileHeightWithoutMargins;
		int cols = (image.getWidth() + tileWidthWithoutMargins - 1 ) / tileWidthWithoutMargins;
		return rows * cols;
	}
	
	private class TilesIterator implements Iterator<ImageTile>
	{
		// Position of top left corner of the tile (without margins) with respect to the top left corner of the image it is a tile of.
		private int x = 0;
		private int y = 0;
		
		@Override
		public boolean hasNext()
		{
			return x < image.getWidth() && y < image.getHeight();
		}

		@Override
		public ImageTile next()
		{
			if (this.hasNext())
			{
				ImageTile tile = new ImageTile(image, x, y, tileWidthWithoutMargins, tileHeightWithoutMargins, margin);
				
				x += tileWidthWithoutMargins;
				if (x >= image.getWidth())
				{
					x = 0;
					y += tileHeightWithoutMargins;
				}
				
				return tile;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}
	}
}

