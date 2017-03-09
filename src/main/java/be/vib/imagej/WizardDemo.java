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
		
		Wizard wizard = new Wizard("EM Denoising wizard");

		WizardPage pageROI = new WizardPageROI(wizard, model, "Select ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(wizard, model, "Select Denoising Algorithm");
		WizardPage pageDenoise = new WizardPageDenoise(wizard, model, "Denoise");
		
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);;
		
		System.out.println("Wizard - about to set visible");
		wizard.setVisible(true);
	}
}
