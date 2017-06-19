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
	
	private float noiseEstimate;
	
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
		noiseEstimate = -1.0f; // < 0 means unknown
		
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
	
	public float getNoiseEstimate()
	{
		return noiseEstimate;
	}
	
	public void setNoiseEstimate(float noiseEstimate)
	{
		this.noiseEstimate = noiseEstimate;
	}

	public void setImage(ImagePlus image)
	{
		// If we've got this image in the model already
		// then do nothing. In particular, leave the existing
		// image noise estimation alone.
		if (this.image == image)
			return;

		// If the old image is locked, unlock it since
		// we don't need it anymore. We assume that only
		// one plugin (ours) will be locking the image...
		if (this.image != null && this.image.isLocked())
			this.image.unlock();

		this.image = image;
		
		// Lock the image (stack) so that if the user closes the image window,
		// the underlying image slices remain in memory.
		if (this.image != null && !this.image.isLocked())
			this.image.lock();
		
		// Remember to re-estimate the noise level
		this.noiseEstimate = -1.0f;
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
