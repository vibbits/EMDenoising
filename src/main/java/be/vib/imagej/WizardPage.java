package be.vib.imagej;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class WizardPage extends JPanel
{
	protected Wizard wizard;
	private String name; // The name of this wizard page. It will show up in the bread crumbs at the top of the wizard.
	
	public WizardPage(Wizard wizard, String name)
	{
		this.wizard = wizard;
		this.name = name;
		
		this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
	}
	
	public String getName()
	{
		return name;
	}

	// override me
	public void goingToNextPage() 
	{
	}
	
	// override me
	public void goingToPreviousPage()
	{
	}

	// override me
	public void arriveFromNextPage() 
	{
	}
	
	// override me
	public void arriveFromPreviousPage()
	{
	}	
	
	// override me
	public boolean canGoToPreviousPage()
	{
		return true;
	}

	// override me
	public boolean canGoToNextPage()
	{
		return true;
	}
}

