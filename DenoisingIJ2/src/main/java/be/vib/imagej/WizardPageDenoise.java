package be.vib.imagej;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class WizardPageDenoise extends WizardPage
{
	public WizardPageDenoise(WizardModel model, String name)
	{
		super(model, name);
		buildUI();
	}
	
	private void buildUI()
	{
		JLabel infoLabel = new JLabel("We are now ready to denoise the current image.");
		
		//boolean isStack = model.imagePlus.getNSlices() > 1;
		
		JButton startButton = new JButton("Start!");
		startButton.addActionListener(e -> { DenoiseSlice(model.imagePlus.getCurrentSlice()); });
		
		startButton.setEnabled(true);
		
		add(infoLabel);
		add(startButton);
	}
	
	private void DenoiseSlice(int slice)
	{
		// TODO: DRY - similar to denoising the preview
		System.out.println("DenoiseSlice " + slice + " (Java thread: " + Thread.currentThread().getId() + ")");
		
		Instant startTime = Instant.now();
		
		ImageProcessor image = model.imagePlus.getStack().getProcessor(slice);
		
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		Object pixelsObject = image.getPixels();
		assert(pixelsObject instanceof byte[]);
		byte[] inputPixels = (byte[])pixelsObject; 
		
		byte[] outputPixels = null;
		switch (model.denoisingAlgorithm)
		{
			case NLMS:
				outputPixels = QuasarInterface.quasarNlmeans(width, height, inputPixels, (float)model.nonLocalMeansParams.sigma, model.nonLocalMeansParams.searchWindow, model.nonLocalMeansParams.halfBlockSize, 0, 0);
				break;
			default:
				outputPixels = Arrays.copyOf(inputPixels, inputPixels.length);
				break;
		}
		
		ByteProcessor denoisedSlice = new ByteProcessor(width, height, outputPixels);
		
		ImageStack denoisedStack = new ImageStack(width, height);
		denoisedStack.addSlice("", denoisedSlice);
		
		ImagePlus denoisedImagePlus = new ImagePlus("Denoised " + model.imagePlus.getTitle(), denoisedStack);
		
		// Log some timing statistics
		Instant endTime = Instant.now();
		long durationMs = Duration.between(startTime, endTime).toMillis();
		long numPixels = width * height;
		System.out.println("Slice denoising time: " + durationMs + " ms " +
		         "(" + (double)numPixels / (double)durationMs + " kpix/s)");
		
		// Display denoised image
		denoisedImagePlus.show();
	}
	
}
