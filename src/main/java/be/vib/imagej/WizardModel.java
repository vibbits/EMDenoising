package be.vib.imagej;

import java.util.HashMap;
import java.util.Map;

import be.vib.imagej.Algorithm.Name;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * WizardModel stores all model data for the wizard. It is shared by all wizard pages.
 * Typically user input done in one wizard page is stored in the model, and will influence
 * what is shown on another wizard page later.
 */
public class WizardModel
{
	// FIXME: avoid making all variables public
	
    public Name name; // name (enum) of currently active denoising algorithm
	
	public Map<Name, Algorithm> algorithms;

	public ImagePlus imagePlus; // original image or image stack (a reference not a copy). Can be null iff. the wizard shows the WizardPageROI; is non-null otherwise.
 	                            //                                                         FIXME: can also be null if the user closes the image window, and we move from the denoising panel back to the algorithm selection panel
    
	public ImageRange range; // the range of image slices that need to be denoised

	public ImageProcessor previewOrigROI;
	
	public WizardModel()
	{
		name = Algorithm.Name.GAUSSIAN;
		
		algorithms = new HashMap<Name, Algorithm>();
		algorithms.put(Name.GAUSSIAN, new GaussianAlgorithm());
		algorithms.put(Name.BILATERAL, new BilateralAlgorithm());
		algorithms.put(Name.BLSGSM, new BLSGSMAlgorithm());
		algorithms.put(Name.ANISOTROPIC_DIFFUSION, new AnisotropicDiffusionAlgorithm());
		algorithms.put(Name.WAVELET_THRESHOLDING, new WaveletThresholdingAlgorithm());
		algorithms.put(Name.NLMS, new NonLocalMeansAlgorithm());
		algorithms.put(Name.NLMS_SCD, new NonLocalMeansSCDAlgorithm());
		
		range = new ImageRange();
		
		imagePlus = null;
		
		previewOrigROI = null;
	}
	
	// Returns the currently active algorithm.
	public Algorithm getAlgorithm()
	{
		return algorithms.get(name);
	}
	
	// Returns an array of the available algorithms,
	// in the order we would like them to appear in the user interface.
	public Algorithm[] getAlgorithms()
	{
		Algorithm[] arr = { algorithms.get(Name.GAUSSIAN),
				            algorithms.get(Name.BILATERAL),
							algorithms.get(Name.ANISOTROPIC_DIFFUSION),
							algorithms.get(Name.BLSGSM),
							algorithms.get(Name.WAVELET_THRESHOLDING),
							algorithms.get(Name.NLMS),
							algorithms.get(Name.NLMS_SCD) };
		return arr;
	}
}
