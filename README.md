# DenoisEM
<img src="https://github.com/vibbits/EMDenoising/blob/master/src/main/resources/be/vib/imagej/images/logo.png" alt="DenoisEM logo" width="25%"/>

[DenoiseEM](https://bioimagingcore.be/DenoisEM/) is an [ImageJ](https://imagej.nih.gov/ij/index.html) plugin for GPU accelerated denoising of electron microscopy images or image stacks. It offers state-of-the-art denoising algorithms that can be optimized through interactive parameter selection. The plugin is fast and scalable due to its GPU accelerated backend based on the [Quasar](https://gepura.io/) programming language. 

<a href="https://gepura.io/" target="_blank"><img src="https://github.com/vibbits/EMDenoising/blob/master/src/main/resources/be/vib/imagej/images/QUASAR_logo_FINAL_S_crop_h75_transp.png" alt="Quasar logo" height="100"/></a>

This software was developed by the Image Processing and Interpretation research group of Ghent University and the Bioinformatics Core of the Flanders Institute for Biotechnology (VIB).

<a href="https://www.ugent.be" target="_blank"><img src="https://github.com/vibbits/EMDenoising/blob/master/src/main/resources/be/vib/imagej/images/ugent.png" alt="UGent logo" height="100"/></a>
<a href="http://www.vib.be" target="_blank"><img src="https://github.com/vibbits/EMDenoising/blob/master/src/main/resources/be/vib/imagej/images/vib.png" alt="VIB logo" height="100"/></a>

# Requirements
- 64-bit Windows 7, 8 or 10
- A relatively recent NVIDIA graphics card, for example a GeForce GTX 900 or 1000 Series card. Technically, for DenoisEM to use the GPU the NVIDIA graphics card needs to have [compute capability 3.0](https://en.wikipedia.org/wiki/CUDA#GPUs_supported). However, the plugin will automatically fall back to (slower) parallel computation on the CPU if no supported GPU is present. 
- [ImageJ](https://imagej.nih.gov/ij/index.html) (we recommend [Fiji](https://fiji.sc/), which comes with many alternative useful plugins)

# Installation
1. Save both [JavaQuasarBridge.jar](http://bioimagingcore.be/DenoisEM/bin/JavaQuasarBridge.jar) and [DenoisEM.jar](http://bioimagingcore.be/DenoisEM/bin/DenoisEM.jar) into the plugins folder of ImageJ/Fiji. 
2. Save and extract Quasar.zip in the root folder of ImageJ or Fiji.

The installation shouldn't take more than a few minutes. For more detailed installation instructions, we refer to the [project page](http://bioimagingcore.be/DenoisEM/installation.html). 

# Getting started
A demo example to get started with DenoisEM in a few minutes is provided on the [project page](http://bioimagingcore.be/DenoisEM/getting-started.html). 

# Reference
We ask users that employ our plugin to refer to DenoisEM as follows:

J. Roels, F. Vernaillen, A. Kremer, A. Goncalves, J. Aelterman, H. Q. Luong, B. Goossens, W. Philips, S. Lippens, Y. Saeys, "DenoisEM: An Interactive ImageJ Plugin for Semi-automated Image Denoising in Electron Microscopy", bioRxiv 644146; doi: https://doi.org/10.1101/644146

# Contact
Users with questions related to DenoisEM can always contact us through denoisem@irc.vib-ugent.be. Note that we have also provided an FAQ section on the [project page](http://bioimagingcore.be/DenoisEM/faq.html). 
