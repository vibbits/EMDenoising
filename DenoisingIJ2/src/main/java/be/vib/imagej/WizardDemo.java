package be.vib.imagej;

import java.io.IOException;

import ij.ImagePlus;

public class WizardDemo
{
	public static void main(String[] args) throws IOException
	{
		WizardModel model = new WizardModel();		
		model.imagePlus = new ImagePlus("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\2013_11_28_arabidopsis_root_0086_crop.png");
//		model.imagePlus.show();
		
		WizardPage pageROI = new WizardPageROI(model, "Select ROI");
		WizardPage pageAlgorithm = new WizardPageDenoisingAlgorithm(model, "Select Denoising Algorithm");
		WizardPage pageDenoise = new WizardPageDenoise(model, "Denoise");
		
		Wizard wizard = new Wizard("EM Denoising wizard");
		wizard.addPage(pageROI);
		wizard.addPage(pageAlgorithm);
		wizard.addPage(pageDenoise);;
		
		System.out.println("Wizard - about to set visible");
		wizard.setVisible(true);
	}
}
