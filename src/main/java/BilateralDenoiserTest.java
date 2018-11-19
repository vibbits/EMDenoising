import be.vib.bits.QHost;
import be.vib.imagej.BilateralDenoiser;
import be.vib.imagej.BilateralParams;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class BilateralDenoiserTest
{
	static
	{		
		System.out.println("About to load JavaQuasarBridge dynamic library");
		System.loadLibrary("JavaQuasarBridge"); // loads JavaQuasarBridge.dll (on Windows)
		// FIXME this needs updating, I think
	}
	
	public static void main(String[] args) throws Exception
	{
			System.out.println("BilateralDenoiserTest");
		
			QHost.init("cuda", true);
			System.out.println("QHost.init done");
			
			ImagePlus imp = new ImagePlus("e:\\datasets\\em\\frank_arabidopsis_root_0086_8bit.tif");
			System.out.println("imp=" + imp);
			
//			// Crop image
//			ImageProcessor ip = imp.getProcessor();
//			ip.setRoi(1, 1, 2000, 2000);
//			imp.setProcessor(null, ip.crop());
//			// End crop
			
			ImageProcessor noisyImage = imp.getStack().getProcessor(1);
			
			BilateralDenoiser denoiser = new BilateralDenoiser(new BilateralParams());			
			denoiser.setImage(noisyImage);
			
			System.out.println("About to call denoiser");
			ImageProcessor denoisedImage = denoiser.call();
			
			System.out.println("Denoising complete");

			ImageStack denoisedStack = new ImageStack(denoisedImage.getWidth(), denoisedImage.getHeight());
			denoisedStack.addSlice("", denoisedImage);
			ImagePlus denoisedImagePlus = new ImagePlus("denoised", denoisedStack);
			System.out.println("denoised: " + denoisedImagePlus);
						
			QHost.release();			
			System.out.println("QHost.release done");
	}
	
}
