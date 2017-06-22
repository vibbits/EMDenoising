//////////////////////////////////////////////////////////////////
// Example JavaScript that calls the EMDenoising plugin machinery
// to denoise an image. It uses Quasar for GPU-acceleration.
//
// Frank Vernaillen, June 2017
//////////////////////////////////////////////////////////////////

// TODO: in another example, show how to use Bioformats to denoise a large image stack
//       without loading it in memory completely first.
// TODO: look at the bioformats plugin to see how they implemented scriptability
// TODO: make an example script using the ImageJ1 *macro* language
// TODO: try to make our plugin recordable

importClass(Packages.ij.IJ);
importClass(Packages.ij.io.OpenDialog);

importClass(Packages.be.vib.imagej.QuasarTools);
importClass(Packages.be.vib.imagej.NonLocalMeansParams);
importClass(Packages.be.vib.imagej.NonLocalMeansDenoiser);
importClass(Packages.be.vib.imagej.DenoiseEngine);
importClass(Packages.be.vib.imagej.ImageRange);

// Load the Quasar library for GPU-accelerated computing.
// Do this only once, at the beginnen of the ImageJ session.
// FIXME: Simply ignore subsequent initializations (so we can run this script several times in a row,
//        or we can run it while the EMDenoising plugin has started Quasar itself already too).
var tempFolder = QuasarTools.loadQuasarBridge();
var engine = "cuda";
QuasarTools.startQuasar(engine, tempFolder, false);
// TODO: Ensure there is no race condition here - are we sure at this point that Quasar has finished initializing completely?
//       (Do we wait for that thread to be finished?)

// Open a noisy image
dialog = new OpenDialog("Choose an 8 or 16-bit image or image stack", null);
path = dialog.getDirectory() + dialog.getFileName();
var noisyImage = IJ.openImage(path);
//noisyImage.show(); // beware: do not close the image window during denoising
// TODO: Check if image gets locked inadvertently. Should we lock it?
// TODO: Reject images that are not 8 or 16 bit grayscale

// Specify the range of image slices to denoise
// (here we use only the current slice, so the first slice, as an example).
var range = ImageRange.makeCurrentSliceRange(noisyImage);

// Specify the denoising algorithm and its parameters
var params = new NonLocalMeansParams();
params.h = 1.5;
var denoiser = new NonLocalMeansDenoiser(params);

// Denoise the image using the GPU
var engine = new DenoiseEngine(denoiser, params);
var denoisedImage =	engine.denoise(noisyImage, range, noisyImage.getTitle() + " [denoised]");

// Show the denoised image
denoisedImage.show();
