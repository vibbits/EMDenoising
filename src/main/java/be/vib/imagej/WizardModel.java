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
    
	private ImageRange range; // the range of image slices that need to be denoised

	private ImageProcessor noisyPreview; // a small region of interest cropped from the original noisy image
	
	private float noiseEstimate;  // estimated standard deviation of the noise in the noisy input image (< 0 means noise not estimated yet)
	
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
		algorithms.put(Name.TIKHONOV, new TikhonovAlgorithm());
		algorithms.put(Name.TOTAL_VARIATION, new TotalVariationAlgorithm());
		
		range = new ImageRange();
		
		image = null;
		noiseEstimate = -1.0f;
		
		noisyPreview = null;
	}
	
	public void reset()
	{
		currentAlgorithmName = Algorithm.Name.GAUSSIAN;
		range = new ImageRange();
		noisyPreview = null;
		
		// Make sure we're not keeping any images locked anymore.
		setImage(null);
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
	// in_the_order_we_would_like_them_to_appear_in_the_user_interface.
	public Algorithm[] getAlgorithms()
	{
		Algorithm[] arr = { algorithms.get(Name.GAUSSIAN),
				            algorithms.get(Name.BILATERAL),
							algorithms.get(Name.ANISOTROPIC_DIFFUSION),
							algorithms.get(Name.BLSGSM),
							algorithms.get(Name.WAVELET_THRESHOLDING),
							algorithms.get(Name.NONLOCALMEANS),
							algorithms.get(Name.TIKHONOV),
							algorithms.get(Name.TOTAL_VARIATION) };
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
		lockImage(false);

		this.image = image;
		
		// Lock the image (stack) so that if the user closes the image window,
		// the underlying image slices remain in memory. Unfortunately locked
		// image stacks are not very user friendly in ImageJ: it is not obvious that 
		// the image is locked, the image window allows moving the current slice slider
		// in a locked stack but does not actually show the correct slice, etc.
		lockImage(true);
		
		// Remember to re-estimate the noise level
		this.noiseEstimate = -1.0f;
	}
	
	public void lockImage(boolean lock)
	{
		if (image == null)
			return;
		
		if (lock && !image.isLocked())
			image.lock();
		else if (!lock && image.isLocked())
			image.unlock();
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
