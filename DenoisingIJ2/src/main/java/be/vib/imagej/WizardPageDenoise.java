package be.vib.imagej;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

public class WizardPageDenoise extends WizardPage
                     implements PropertyChangeListener
{
	
	
	public WizardPageDenoise(WizardModel model, String name)
	{
		super(model, name);
		buildUI();
	}
	
	private void buildUI()
	{

	}

	@Override
	public void propertyChange(PropertyChangeEvent e)
	{

	}
	
}
