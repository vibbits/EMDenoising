rmdir /s /q output
mkdir output\class
mkdir output\jar
mkdir output\qlib

set SRC_JAVA=src\main\java\be\vib\imagej

javac -d output\class ^
-classpath ^
C:\Users\frver\.m2\repository\org\scijava\scijava-common\2.56.0\scijava-common-2.56.0.jar;^
e:\git\ImageJRepository\ij-1.50a.jar;^
e:\git\bits\bioimaging\JavaQuasarBridge\dist\JavaQuasarBridge.jar ^
%SRC_JAVA%\Algorithm.java ^
%SRC_JAVA%\AnisotropicDiffusionAlgorithm.java ^
%SRC_JAVA%\AnisotropicDiffusionDenoiser.java ^
%SRC_JAVA%\AnisotropicDiffusionParams.java ^
%SRC_JAVA%\AnisotropicDiffusionParamsPanel.java ^
%SRC_JAVA%\BilateralAlgorithm.java ^
%SRC_JAVA%\BilateralDenoiser.java ^
%SRC_JAVA%\BilateralParams.java ^
%SRC_JAVA%\BilateralParamsPanel.java ^
%SRC_JAVA%\BLSGSMAlgorithm.java ^
%SRC_JAVA%\BLSGSMDenoiser.java ^
%SRC_JAVA%\BLSGSMParams.java ^
%SRC_JAVA%\BLSGSMParamsPanel.java ^
%SRC_JAVA%\DenoisingIJ2.java ^
%SRC_JAVA%\Denoiser.java ^
%SRC_JAVA%\DenoiseParamsChangeEvent.java ^
%SRC_JAVA%\DenoiseParamsChangeEventListener.java ^
%SRC_JAVA%\DenoiseParamsPanelBase.java ^
%SRC_JAVA%\DenoisePreviewCache.java ^
%SRC_JAVA%\DenoisePreviewCacheKey.java ^
%SRC_JAVA%\DenoiseSummaryPanel.java ^
%SRC_JAVA%\DenoiseSwingWorker.java ^
%SRC_JAVA%\DenoisingWizardSingleton.java ^
%SRC_JAVA%\GaussianAlgorithm.java ^
%SRC_JAVA%\GaussianDenoiser.java ^
%SRC_JAVA%\GaussianParams.java ^
%SRC_JAVA%\GaussianParamsPanel.java ^
%SRC_JAVA%\NonLocalMeansAlgorithm.java ^
%SRC_JAVA%\NonLocalMeansDenoiser.java ^
%SRC_JAVA%\NonLocalMeansParams.java ^
%SRC_JAVA%\NonLocalMeansParamsPanel.java ^
%SRC_JAVA%\ImagePanel.java ^
%SRC_JAVA%\ImageRange.java ^
%SRC_JAVA%\ImageTile.java ^
%SRC_JAVA%\ImageTiler.java ^
%SRC_JAVA%\Preferences.java ^
%SRC_JAVA%\QuasarInitializationSwingWorker.java ^
%SRC_JAVA%\QuasarTools.java ^
%SRC_JAVA%\RangeSelectionPanel.java ^
%SRC_JAVA%\SaturatingExecutor.java ^
%SRC_JAVA%\SliderFieldPair.java ^
%SRC_JAVA%\SliderSpinnerPair.java ^
%SRC_JAVA%\WaveletThresholdingAlgorithm.java ^
%SRC_JAVA%\WaveletThresholdingDenoiser.java ^
%SRC_JAVA%\WaveletThresholdingParams.java ^
%SRC_JAVA%\WaveletThresholdingParamsPanel.java ^
%SRC_JAVA%\WizardPageDenoise.java ^
%SRC_JAVA%\WizardPageDenoisingAlgorithm.java ^
%SRC_JAVA%\WizardPageInitializeQuasar.java ^
%SRC_JAVA%\WizardPageROI.java ^
%SRC_JAVA%\Wizard.java ^
%SRC_JAVA%\WizardPage.java ^
%SRC_JAVA%\WizardModel.java ^
src\main\java\BilateralDenoiserTest.java

@if %errorlevel% neq 0 exit /b %errorlevel%

rem ================================================================================
rem TODO: delete old qlib?
rem TODO: use --rebuild to force regenerating the .qlib
pushd src\main\resources\quasar
"e:\Program Files\Quasar\Quasar.exe" --make_lib --gpu vib_denoising_algorithms.q
@if %errorlevel% neq 0 exit /b %errorlevel%
popd
copy src\main\resources\quasar\vib_denoising_algorithms.qlib output\qlib
rem copy src\main\resources\quasar\*.q output\qlib
rem ================================================================================

jar cvfm output\jar\EM_Denoising-0.0.1.jar src\main\Manifest.txt -C output\class\ . -C output\ qlib

copy output\jar\EM_Denoising-0.0.1.jar e:\Fiji.app\plugins

