package be.vib.imagej;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// TODO: explain class

class SliderSpinnerPair implements ChangeListener
{
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private JSlider slider;
	private JSpinner spinner;
	
	private int value;
			
	public SliderSpinnerPair(int minValue, int maxValue)
	{
		value = minValue;
		
		int stepSize = 1;
		SpinnerModel spinnerModel = new SpinnerNumberModel(value, minValue, maxValue, stepSize);
		spinner = new JSpinner(spinnerModel);

		slider = new JSlider(minValue, maxValue, value);

		slider.addChangeListener(this);
		spinner.addChangeListener(this);
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
	
	public JSpinner getSpinner()
	{
		return spinner;
	}
	
	public void setValue(int newValue)
	{
		int oldValue = value;
				
		if (oldValue == newValue)
			return;
		
		value = newValue;
		
		spinner.setValue(newValue);
		slider.setValue(newValue);
		
        pcs.firePropertyChange("value", oldValue, newValue);
	}
	
	public int getValue()
	{
		return value;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == slider)
		{
//			if (slider.getValueIsAdjusting())
//			return;
			
			int newValue = ((Number)slider.getValue()).intValue();
			if (newValue == value)
				return;
			
			int oldValue = value;
			value = newValue;
			
			spinner.setValue(newValue);
			pcs.firePropertyChange("value", oldValue, newValue);
		}
		else if (e.getSource() == spinner)
		{
			int newValue = ((Number)spinner.getValue()).intValue();
			if (newValue == value)
				return;
			
			int oldValue = value;
			value = newValue;
			
			slider.setValue(newValue);
			pcs.firePropertyChange("value", oldValue, newValue);
			
		}
	}
}