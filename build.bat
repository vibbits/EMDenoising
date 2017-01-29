rmdir /s /q output
mkdir output\class
mkdir output\jar

set SRC_JAVA=src\main\java\be\vib\imagej

javac -d output\class ^
-classpath ^
C:\Users\frver\.m2\repository\org\scijava\scijava-common\2.56.0\scijava-common-2.56.0.jar;^
e:\git\ImageJRepository\ij-1.50a.jar;^
e:\git\bits\bioimaging\JavaQuasarBridge\dist\JavaQuasarBridge.jar ^
%SRC_JAVA%\AnisotropicDiffusionDenoiser.java ^
%SRC_JAVA%\AnisotropicDiffusionParams.java ^
%SRC_JAVA%\AnisotropicDiffusionParamsPanel.java ^
%SRC_JAVA%\BilateralDenoiser.java ^
%SRC_JAVA%\BilateralParams.java ^
%SRC_JAVA%\BilateralParamsPanel.java ^
%SRC_JAVA%\BLSGSMDenoiser.java ^
%SRC_JAVA%\BLSGSMParams.java ^
%SRC_JAVA%\BLSGSMParamsPanel.java ^
%SRC_JAVA%\BM3DParams.java ^
%SRC_JAVA%\DenoisingIJ2.java ^
%SRC_JAVA%\Denoiser.java ^
%SRC_JAVA%\DenoiseParamsChangeEvent.java ^
%SRC_JAVA%\DenoiseParamsChangeEventListener.java ^
%SRC_JAVA%\DenoiseParamsPanelBase.java ^
%SRC_JAVA%\DenoisePreviewSwingWorker.java ^
%SRC_JAVA%\DenoiseSummaryPanel.java ^
%SRC_JAVA%\DenoiseSwingWorker.java ^
%SRC_JAVA%\GaussianDenoiser.java ^
%SRC_JAVA%\GaussianParams.java ^
%SRC_JAVA%\GaussianParamsPanel.java ^
%SRC_JAVA%\NonLocalMeansDenoiser.java ^
%SRC_JAVA%\NonLocalMeansParams.java ^
%SRC_JAVA%\NonLocalMeansParamsPanel.java ^
%SRC_JAVA%\NonLocalMeansSCDenoiser.java ^
%SRC_JAVA%\NonLocalMeansSCParams.java ^
%SRC_JAVA%\NonLocalMeansSCParamsPanel.java ^
%SRC_JAVA%\NonLocalMeansSCDDenoiser.java ^
%SRC_JAVA%\NonLocalMeansSCDParams.java ^
%SRC_JAVA%\NonLocalMeansSCDParamsPanel.java ^
%SRC_JAVA%\NoOpDenoiser.java ^
%SRC_JAVA%\ImagePanel.java ^
%SRC_JAVA%\ImageRange.java ^
%SRC_JAVA%\ImageTile.java ^
%SRC_JAVA%\ImageTiler.java ^
%SRC_JAVA%\LinearImage.java ^
%SRC_JAVA%\QuasarInitializationSwingWorker.java ^
%SRC_JAVA%\RangeSelectionPanel.java ^
%SRC_JAVA%\SliderFieldPair.java ^
%SRC_JAVA%\SliderSpinnerPair.java ^
%SRC_JAVA%\WaveletThresholdingDenoiser.java ^
%SRC_JAVA%\WaveletThresholdingParams.java ^
%SRC_JAVA%\WaveletThresholdingParamsPanel.java ^
%SRC_JAVA%\WizardPageDenoise.java ^
%SRC_JAVA%\WizardPageDenoisingAlgorithm.java ^
%SRC_JAVA%\WizardPageInitializeQuasar.java ^
%SRC_JAVA%\WizardPageROI.java ^
%SRC_JAVA%\Wizard.java ^
%SRC_JAVA%\WizardPage.java ^
%SRC_JAVA%\WizardModel.java

jar cvfm output\jar\VIBDenoising-0.0.1.jar src\main\Manifest.txt -C output\class\ .

