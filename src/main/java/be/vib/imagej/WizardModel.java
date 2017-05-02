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
    private Name currentAlgorithmName; // name (enum) of currently active denoising algorithm
	
	private Map<Name, Algorithm> algorithms;

	private ImagePlus image; // original image or image stack (a reference not a copy). Can be null iff. the wizard shows the WizardPageROI; is non-null otherwise.
 	                         //                                                         FIXME: can also be null if the user closes the image window, and we move from the denoising panel back to the algorithm selection panel
    
	private ImageRange range; // the range of image slices that need to be denoised

	private ImageProcessor noisyPreview; // a small region of interest cropped from the original noisy image
	
	public static final int maxPreviewSize = 512; // max size of the denoising preview windows, and thus of the ROI selected on the image
	
	public WizardModel()
	{
		currentAlgorithmName = Algorithm.Name.GAUSSIAN;
		
		algorithms = new HashMap<Name, Algorithm>();
		algorithms.put(Name.GAUSSIAN, new GaussianAlgorithm());
		algorithms.put(Name.BILATERAL, new BilateralAlgorithm());
		algorithms.put(Name.BLSGSM, new BLSGSMAlgorithm());
		algorithms.put(Name.ANISOTROPIC_DIFFUSION, new AnisotropicDiffusionAlgorithm());
		algorithms.put(Name.WAVELET_THRESHOLDING, new WaveletThresholdingAlgorithm());
		algorithms.put(Name.NONLOCALMEANS, new NonLocalMeansAlgorithm());
		
		range = new ImageRange();
		
		image = null;
		
		noisyPreview = null;
	}
	
	// Returns the currently active algorithm.
	public Algorithm getAlgorithm()
	{
		return algorithms.get(currentAlgorithmName);
	}
	
	public void setAlgorithm(Algorithm.Name name)
	{
		this.currentAlgorithmName = name;
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
							algorithms.get(Name.NONLOCALMEANS) };
		return arr;
	}

	public ImageProcessor getNoisyPreview()
	{
		return noisyPreview;
	}

	public void setNoisyPreview(ImageProcessor preview)
	{
		this.noisyPreview = preview;
	}

	public ImagePlus getImage()
	{
		return image;
	}

	public void setImage(ImagePlus image)
	{
		this.image = image;
	}
	
	public ImageRange getRange()
	{
		return range;
	}

	public void setRange(ImageRange range)
	{
		this.range = range;
	}
}
