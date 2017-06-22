//////////////////////////////////////////////////////////////////
// Example JavaScript that calls the EMDenoising plugin machinery
// to denoise an image. It uses Quasar for GPU-acceleration.
//
// Frank Vernaillen, June 2017
//////////////////////////////////////////////////////////////////

// TODO: In another example, show how to use Bioformats to denoise a large image stack
//       without loading it in memory completely first.
// TODO: look at the bioformats plugin to see how they implemented scriptability
// TODO: Make an example script using the ImageJ1 *macro* language
// TODO: Try to make our plugin recordable
// TODO: Check if input image stays locked inadvertently. Should we lock it?

importClass(Packages.ij.IJ);
importClass(Packages.ij.io.OpenDialog);

importClass(Packages.be.vib.imagej.QuasarTools);
importClass(Packages.be.vib.imagej.NonLocalMeansParams);
importClass(Packages.be.vib.imagej.NonLocalMeansDenoiser);
importClass(Packages.be.vib.imagej.DenoiseEngine);
importClass(Packages.be.vib.imagej.ImageRange);

// Load the Quasar library for GPU-accelerated computing.
// This needs to be done before using the denoising machinery.
QuasarTools.startQuasar("cuda", false);

// Open a noisy image
dialog = new OpenDialog("Choose an 8 or 16-bit image or image stack", null);
path = dialog.getPath();
if (path != null)
{
	var noisyImage = IJ.openImage(path);
	//noisyImage.show(); // beware: do not close the image window during denoising
	
	if (noisyImage.getBitDepth() == 8 || noisyImage.getBitDepth() == 16)
	{
		// Specify the range of image slices to denoise
		// (here we use only the current slice, so the first slice, as an example).
		var range = ImageRange.makeCurrentSliceRange(noisyImage);
		
		// Specify the denoising algorithm and its parameters
		var params = new NonLocalMeansParams();
		params.h = 1.5;
		var denoiser = new NonLocalMeansDenoiser(params);
		
		// Denoise the image using the GPU
		var engine = new DenoiseEngine(denoiser);
		var denoisedImage =	engine.denoise(noisyImage, range, noisyImage.getTitle() + " [denoised]");
		
		// Show the denoised image
		denoisedImage.show();
	}
}