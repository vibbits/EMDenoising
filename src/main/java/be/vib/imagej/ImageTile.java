package be.vib.imagej;

import ij.process.ImageProcessor;

public class ImageTile
{
	private ImageProcessor tileImp; // the image tile, including the margins
	private int xpos, ypos; // position of the complete tile (without the margins) with respect to the top left corner of the larger original image.
	private int topMargin, rightMargin, bottomMargin, leftMargin; // size of the margins in this tile (any margin can be zero)
	
	
	// Create an image tile from the given large source image. The tile's top left corner starts at (x,y)
	// in the original image, before extending with the given margin. Tiles will not be extended with the margin on the side where they
	// border an edge of the source image. Also, even without margin, a tile may be smaller than tileWidth/HeightWithoutMargins if the
	// tile overlaps with the source image edges.
	public ImageTile(ImageProcessor source, int xWithoutMargins, int yWithoutMargins, int tileWidthWithoutMargins, int tileHeightWithoutMargins, int margin)
	{
		assert(xWithoutMargins >= 0 && xWithoutMargins < source.getWidth());
		assert(yWithoutMargins >= 0 && yWithoutMargins < source.getHeight());
		assert(tileWidthWithoutMargins > 0);
		assert(tileHeightWithoutMargins > 0);
		assert(margin >= 0);
		
		xpos = xWithoutMargins;
		ypos = yWithoutMargins;
		
		int tlx = Math.max(0, xWithoutMargins - margin);
		int tly = Math.max(0, yWithoutMargins - margin);
		int brx = Math.min(source.getWidth() - 1, xWithoutMargins + tileWidthWithoutMargins + margin - 1);
		int bry = Math.min(source.getHeight() - 1, yWithoutMargins + tileHeightWithoutMargins + margin - 1);
		
		assert(brx >= tlx);
		assert(bry >= tly);
		
		leftMargin = Math.min(margin, xpos);
		topMargin = Math.min(margin, ypos);
		rightMargin = (xWithoutMargins + tileWidthWithoutMargins >= source.getWidth() - 1) ? 0 : Math.min(margin, source.getWidth() - 1 - xWithoutMargins - tileWidthWithoutMargins);
		bottomMargin = (yWithoutMargins + tileHeightWithoutMargins >= source.getHeight() - 1) ? 0 : Math.min(margin, source.getHeight() - 1 - yWithoutMargins - tileHeightWithoutMargins);
		
		System.out.println("ImageTile: tl(" + tlx + ", " + tly + ") w=" + (brx - tlx + 1) + " h=" + (bry - tly + 1));
		source.resetRoi();
		source.setRoi(tlx, tly, brx - tlx + 1, bry - tly + 1);
		tileImp = source.crop();
	}
	
	public int getXPositionWithoutMargins()
	{
		return xpos;
	}
	
	public int getYPositionWithoutMargins()
	{
		return ypos;
	}
	
	public int getWidthWithoutMargins()
	{
		return tileImp.getWidth() - leftMargin - rightMargin;
	}
	
	public int getHeightWithoutMargins()
	{
		return tileImp.getHeight() - topMargin - bottomMargin;
	}
	
	public int getTopMargin()
	{
		return topMargin;
	}
	
	public int getLeftMargin()
	{
		return leftMargin;
	}
	
	public ImageProcessor getImageWithMargins()
	{
		return tileImp;
	}	
}
