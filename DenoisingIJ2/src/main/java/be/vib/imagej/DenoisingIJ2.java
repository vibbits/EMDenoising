package be.vib.imagej;

import java.time.Duration;
import java.time.Instant;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

// Starting Fiji:
// e:\Fiji.app\ImageJ-win64.exe --console --class-path e:\Fiji.app\plugins\vib_denoising_ij2-1.0.0-amd64-Windows-msvc-jni.nar
// (the --console triggers and error but is needed anyway to be able to see the stderr output from the C++ side)
//
// Building:
//    mvn
// This will compile and copy the .jar and .nar into the Fiji installation. It will *not* copy any other dependencies alone (which is intentional).
//
// We do not set the "imagej.app.directory" property in our pom.xml because otherwise when running "mvn" the "copy-jars" phase
// of the "imagej-maven-plugin" will copy our dependencies (such as scijava-common.jar) into the Fiji distribution.
// This may break the installation if the versions we are copying into the installation are incompatible with some other jars.
// (See also http://imagej.1557.x6.nabble.com/Eclipse-Maven-workflow-depencies-copied-to-Fiji-app-directory-td5005804.html)
//
// Be careful: after updating Quasar, we probably have to copy the corresponding .h and .cpp files into our source folders
// to avoid that compile time and runtime versions of Quasar are different. (Or is that irrelevant?)
//
// TODO:
// - Let Maven pick up the Quasar include files from the Quasar installation instead of the copies we placed in src\main\include. Also let it pick up quasar_host.cpp
//   from the Quasar installation instead of our copy.
//
// Preliminary timings on my VIB laptop:
// 2013_11_28_arabidopsis_root_0086.tiff (3896x3896 pixels, 8 bit/pixel), NLMS(sigma 25, searchwin=11, halfblocksize=3, novect, noklt)
// Times including moving data from Java over JNI to C++ and Quasar (and to GPU) and all the way back.
//   cpu: Denoising time: 320383 ms (47.377 kpix/s)
//   cuda: Denoising time: 13860 ms (1095.153 kpix/s) = 23x faster than Quasar utilising the cpu


@Plugin(type = Command.class, menuPath = "Plugins>EM Denoising")
public class DenoisingIJ2 implements Command
{
	@Parameter
	private LogService log;

	// If no image is active, the Fiji plugin framework will automatically
	// issue an error message.
    @Parameter
    private ImagePlus imp;
	
	static
	{
        NarSystem.loadLibrary();
		System.out.println("Native library loaded");
	}
	
	@Override
	public void run() 
	{
		log.info("DenoisingIJ2.run() called.");		
        log.info("Image: " + imp.getTitle());
        log.info("Height:" + imp.getHeight() + " width:" + imp.getWidth() + " channels:" + imp.getNChannels() + " slices:" + imp.getNSlices() + " frames:" + imp.getNFrames());
        log.info("Bytes per pixel:" + imp.getBytesPerPixel());
		
		if (imp.getNDimensions() > 2)
		{
			// No stacks or hyperstacks supported for now
			IJ.showMessage("Only single channel 2D images are supported. This is a " + imp.getNDimensions() + "D image.");
			return;
		}

		if (imp.getBytesPerPixel() != 1)
		{
			IJ.showMessage("For now only 8-bit images are supported. This image has " + imp.getBytesPerPixel() + " bytes per pixel. Please change the bit depth to 8 bits per pixel. In Fiji: Image > Type > 8-bit.");
			// TODO: support 16-bit images, they are common for EM

			return;
		}

		String quasarDevice = "cuda";
		boolean haveQuasar = quasarInit(quasarDevice);
		if (!haveQuasar)
		{
			IJ.showMessage("Could not initialize Quasar device '" + quasarDevice + "'");
			return;
		}
		
		// FIXME: support loading from JAR or so
		boolean sourceLoaded = quasarLoadSource("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\nlmeans_denoising_stillimages.q");
		if (!sourceLoaded)
		{
			IJ.showMessage("Could not load Quasar source module.");
			return;
		}
		
		Instant start = Instant.now();
		
		byte[] inputPixels = get8BitGrayscalePixels(imp);
		
		byte[] outputPixels = quasarNlmeans(imp.getWidth(), imp.getHeight(), inputPixels, 25.0f, 11, 3, 0, 0);
		
		if (outputPixels != null)
		{
			// Show denoised result
			ImagePlus newImage = make8BitGrayscaleImage(imp.getWidth(), imp.getHeight(), outputPixels, "NLMS Denoised " + imp.getTitle());
			newImage.show();

			// Log some timing statistics
			Instant end = Instant.now();
			long durationMs = Duration.between(start, end).toMillis();
			long numPixels = imp.getWidth() * imp.getHeight();
			log.info("Denoising time: " + durationMs + " ms " +
			         "(" + (double)numPixels / (double)durationMs + " kpix/s)");
		}
		else
		{
			IJ.showMessage("Non-local means denoising failed.");
		}
		
		quasarRelease();
		
		log.info("EM denoising plugin done.");
	}
	
	private static byte[] get8BitGrayscalePixels(ImagePlus image)
	{
		ImageStack stack = image.getStack();
		assert(stack.getSize() == 1);
		
		Object pixelsObject = stack.getProcessor(1).getPixels();
		assert(pixelsObject instanceof byte[]);
		
		return (byte[])pixelsObject;
	}
	
	private static ImagePlus make8BitGrayscaleImage(int width, int height, byte[] pixels, String title)
	{
		ImageProcessor imageProcessor = new ByteProcessor(width, height, pixels); // ByteProcessor handles grayscale images
		
		ImageStack stack = new ImageStack(width, height);
		stack.addSlice("", imageProcessor);
		
		ImagePlus image = new ImagePlus(title, stack);
		return image;
	}
	
	public native static boolean quasarInit(String deviceName);  // deviceName is "cpu" or "cuda" or ...
	public native static void quasarRelease();
	public native static boolean quasarLoadSource(String source);  // load .q file whose full path name is source
	public native static byte[] quasarNlmeans(int width, int height, byte[] inputPixels, float sigma, int searchWindow, int halfBlockSize,  int vectorBasedFilter, int kltPostProcessing);



//	public static void main(String[] args) throws IOException
//	{	
//		final ImageJ ij = net.imagej.Main.launch(args);
//		ij.ui().showUI();
//
//		final String testImagePath = "e:\\Datasets\\EM\\frank_small_em_crop.tif";
//		
//		final ServiceHelper sh = new ServiceHelper(ij.getContext());
//		final IOService io = sh.loadService(DefaultIOService.class);
//		final Dataset dataset = (Dataset)io.open(testImagePath);
//		
//		final ImageDisplay imageDisplay = (ImageDisplay)ij.display().createDisplay(dataset);
//		
//		ij.ui().show(imageDisplay);
//		
//		// Launch the plugin
//		ij.command().run(DenoisingIJ2.class, true);
//	}
	
}
	