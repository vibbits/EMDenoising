package be.vib.imagej;

import java.io.IOException;

import ij.ImagePlus;

public class WizardDemo
{
	public static void main(String[] args) throws IOException
	{
		WizardModel model = new WizardModel();		
		model.setImage(new ImagePlus("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\2013_11_28_arabidopsis_root_0086_crop.png"));
//		model.imagePlus.show();
		
		Wizard wizard = new Wizard("EM Denoising wizard", model);

		WizardPage[] pages = { new WizardPageROI(wizard, "Select ROI"),
		                       new WizardPageDenoisingAlgorithm(wizard, "Select Denoising Algorithm"),
		                       new WizardPageDenoise(wizard, "Denoise") };
		
		wizard.build(pages);
		
		System.out.println("Wizard - about to set visible");
		wizard.setVisible(true);
	}
}
