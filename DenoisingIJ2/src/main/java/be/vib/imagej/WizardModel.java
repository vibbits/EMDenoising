package be.vib.imagej;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * WizardModel stores all model data for the wizard. It is shared by all wizard pages.
 * Typically user input done in one wizard page is stored in the model, and will influence
 * what is shown on another wizard page later.
 *
 */
public class WizardModel
{
	public enum DenoisingAlgorithm
	{
		NLMS,      // Non-local means filtering
		NLMS_SC,   // NLMS-SC, an extension of non-local means filtering that deals with the fact that EM noise is correlated and signal-dependent
		NLMS_SCD,  // NLMS-SCD, like NLMS-SC but with deblurring
		BM3D       // Block-matching and 3D filtering
	};

	public DenoisingAlgorithm denoisingAlgorithm;
	public NonLocalMeansParams nonLocalMeansParams;	
	public BM3DParams bm3dParams;
	
	ImagePlus imagePlus; // original image or image stack (a reference not a copy)
	Rectangle roi;  // null if the full image should be used; the ROI bounds are with respect to the original image (not the possibly rescaled on-screen version in the dialog)

	ImageProcessor previewOrigROI;
	ImageProcessor previewDenoisedROI;
	// TODO: Maybe we want to cache the result of denoising the ROI with the last parameter values used (for each algorithm)?
	//       That would at least allow the user to compare the effect of different algorithms (by toggling the algorithm radio button)
	//       without incurring any waiting.
	
	public class NonLocalMeansParams
	{
		public float sigma;
		public int searchWindow;
		public int halfBlockSize;
		
		// TODO: pick sensible parameter ranges
		//       (some values probably depend on one another though, like sigma and block size...)
		
		public static final float sigmaMin = 0.01f;
		public static final float sigmaMax = 50.0f;
		
		public static final int searchWindowMin = 1;
		public static final int searchWindowMax = 20;
		
		public static final int halfBlockSizeMin = 1;
		public static final int halfBlockSizeMax = 10;

		public NonLocalMeansParams()
		{
			sigma = 25.0f;
			searchWindow = 11;
			halfBlockSize = 3;
		}
	}
	
	public class BM3DParams  // TODO
	{
		public int magicValue;
		public float luckyNumber;
		
		public BM3DParams()
		{
			magicValue = 42;
			luckyNumber = 13.0f;
		}
	}
	
	// TODO: NLMS-SC
	
	// TODO: NLMS-SCD
	
	WizardModel()
	{
		denoisingAlgorithm = DenoisingAlgorithm.NLMS;
		nonLocalMeansParams = new NonLocalMeansParams();
		bm3dParams = new BM3DParams();
	}
}
