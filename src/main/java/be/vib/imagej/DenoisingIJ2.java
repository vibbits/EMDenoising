package be.vib.imagej;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>DenoisEM>Denoise")
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;
	
	@Override
	public void run() 
	{
		log.info("VIB DenoisEM plugin");
		
		Wizard wizard = DenoisingWizardSingleton.getInstance();
		wizard.setVisible(true);
		
		// After displaying the denoising wizard the ImageJ plugin run() method finishes immediately,
		// but the wizard is still visible and active.
	}
	
}
	