package be.vib.imagej;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class WizardPage extends JPanel
{
	protected WizardModel model;
	protected Wizard wizard;
	
	private String name; // The name of this wizard page. It will show up in the bread crumbs at the top of the wizard.
	
	public WizardPage(Wizard wizard, WizardModel model, String name)
	{
		this.wizard = wizard;
		this.model = model;
		this.name = name;
		
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
	}
	
	public String getName()
	{
		return name;
	}
	
	// override me
	public void aboutToShowPanel()
	{
	}
}

