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

		SpinnerModel spinnerModel = makeSpinnerModel(minValue, maxValue, value);
		spinner = new JSpinner(spinnerModel);

		slider = new JSlider(minValue, maxValue, value);
		// TODO: it would be nice if for integer ranges the slider knob would jump from one integer value to the next,
		// instead of moving smoothly over fractional values that have no meaning. I don't think JSlider offers this out of the box.

		slider.addChangeListener(this);
		spinner.addChangeListener(this);
	}

	public void updateRange(int minValue, int maxValue, int value)
	{
		SpinnerModel spinnerModel = makeSpinnerModel(minValue, maxValue, value);
		spinner.setModel(spinnerModel);

		slider.setMinimum(minValue);
		slider.setMaximum(maxValue);

		setValue(value);
	}

	private SpinnerNumberModel makeSpinnerModel(int minValue, int maxValue, int value)
	{
		int stepSize = 1;
		return new SpinnerNumberModel(value, minValue, maxValue, stepSize);
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