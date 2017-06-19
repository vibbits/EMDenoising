package be.vib.imagej;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Format;
import java.util.function.Function;

import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// TODO: explain class
// 			
// An alternative design for different widgets that need to stay synchronized modifying the same data (one float, one int, which complicates the issue a bit):
// http://docs.oracle.com/javase/tutorial/uiswing/components/model.html

class SliderFieldPair implements ChangeListener, PropertyChangeListener
{
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private JSlider slider;
	private JFormattedTextField floatField;
	
	private boolean ignoreSlider = false;  // if set, then value changes of the slider are (temporarily) ignored
	
	private Function<Float, Integer> toSlider;    // maps a floating point value to an integer position on the slider
	private Function<Integer, Float> fromSlider;  // maps an integer slider position to the corresponding floating point value
	
	private int sliderMin;
	private int sliderMax;
	
	private float value; // the shared value that is controlled by both the slider and the text field
	
	public SliderFieldPair(int sliderMin, int sliderMax, Format formatter, float fieldMin, float fieldMax)
	{
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		
		// Linear interpolation to map a float range to/from an integer range
		toSlider = toSliderConversion(sliderMin, sliderMax, fieldMin, fieldMax);
		fromSlider = fromSliderConversion(sliderMin, sliderMax, fieldMin, fieldMax);
		
		value = fieldMin;
		
		int sliderValue = toSlider.apply(value);
		slider = new JSlider(sliderMin, sliderMax, sliderValue);
		
		floatField = new JFormattedTextField(formatter);
		floatField.setValue(new Float(value));
		
		floatField.addPropertyChangeListener("value", this);
		slider.addChangeListener(this);
	}
	
	private Function<Float, Integer> toSliderConversion(int sliderMin, int sliderMax, float fieldMin, float fieldMax)
	{
		return f -> (int)(sliderMin + (sliderMax - sliderMin) * (f - fieldMin) / (fieldMax - fieldMin)); // TODO: round to nearest integer instead of truncate
	}
	
	private Function<Integer, Float> fromSliderConversion(int sliderMin, int sliderMax, float fieldMin, float fieldMax)
	{
		return i -> fieldMin + (fieldMax - fieldMin) * (i - sliderMin) / (float)(sliderMax - sliderMin);
	}
	
	// Updates the range of (floating point) values that the slider covers, and sets a new actual value.
	public void updateRange(float fieldMin, float fieldMax, float value)
	{
		// Note: we can keep on using the current JSlider object, but we will use different
		// formulae to convert the floating point value that the slider represents
		// to/from the slider's (fixed) integer range.
		toSlider = toSliderConversion(sliderMin, sliderMax, fieldMin, fieldMax);
		fromSlider = fromSliderConversion(sliderMin, sliderMax, fieldMin, fieldMax);
		setValue(value);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}
	
	public JSlider getSlider()
	{
		return slider;
	}
	
	public JFormattedTextField getFloatField()
	{
		return floatField;
	}
	
	public void setValue(float newValue)
	{
		if (value == newValue)
			return;
		
		float oldValue = value;
		value = newValue;
		
		updateSlider();
		updateField();
		
        pcs.firePropertyChange("value", oldValue, newValue);
	}
	
	public float getValue()
	{
		return value;
	}
	
	private void updateSlider()
	{
		ignoreSlider = true;
		slider.setValue(toSlider.apply(value));
		ignoreSlider = false;		
	}
	
	private void updateField()
	{
    	floatField.setValue(new Float(value));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		assert(e.getSource() == floatField);
		
		float newValue = ((Number)e.getNewValue()).floatValue();
		
		if (value == newValue)
			return;
		
		float oldValue = value;
		value = newValue;
		
		updateSlider();
		
        pcs.firePropertyChange("value", oldValue, newValue);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		assert(e.getSource() == slider);
		
		if (ignoreSlider)
			return;
		
    	float newValue = fromSlider.apply(((Number)slider.getValue()).intValue());
    	
    	if (value == newValue)
    		return;
    	
    	float oldValue = value;
    	value = newValue;
    	
    	updateField();
    	
        pcs.firePropertyChange("value", oldValue, newValue);
	}
}