package be.vib.imagej;

import ij.ImagePlus;

public class ImageRange
{
	public enum RangeType
	{
		CURRENT_SLICE,
		ALL_SLICES,
		NUMERIC_SLICE_RANGE
	}
	
	private RangeType type;
	private int first;   // index of first slice of the range (1-based, in imagePlus)
	private int last;    // index of last slice of the range (1-based, in imagePlus)
	
	public static ImageRange makeCurrentSliceRange(ImagePlus imagePlus)
	{
		int slice = imagePlus.getCurrentSlice();
		return new ImageRange(RangeType.CURRENT_SLICE, slice, slice);
	}
	
	public static ImageRange makeAllSlicesRange(ImagePlus imagePlus)
	{
		int first = 1;
		int last = imagePlus.getNSlices();
		return new ImageRange(RangeType.ALL_SLICES, first, last);
	}
	
	public static ImageRange makeNumericSliceRange(ImagePlus imagePlus, int first, int last)  // 0 <= first <= last
	{
		assert(first >= 1);
		assert(last <= imagePlus.getNSlices());
		return new ImageRange(RangeType.NUMERIC_SLICE_RANGE, first, last);
	}
	
	private ImageRange(RangeType type, int first, int last)
	{
		assert(0 <= first && first <= last);
		assert(type != RangeType.CURRENT_SLICE ||
			   type == RangeType.CURRENT_SLICE && first == last);
		
		this.type = type;
		this.first = first;
		this.last = last;
	}
	
	public ImageRange()
	{
		this.type = RangeType.CURRENT_SLICE;
		this.first = this.last = 1;
	}
	
	public RangeType getType()
	{
		return type;
	}
	
	public int getFirst()
	{
		return first;
	}
	
	public int getLast()
	{
		return last;
	}
}
