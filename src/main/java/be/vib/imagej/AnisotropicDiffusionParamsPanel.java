package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class AnisotropicDiffusionParamsPanel extends DenoiseParamsPanelBase 
{
	private AnisotropicDiffusionParams params;
	private SliderFieldPair diffusionFactorPair;
	private SliderFieldPair stepSizePair;
	private SliderSpinnerPair iterationsPair;
	
	public AnisotropicDiffusionParamsPanel(AnisotropicDiffusionParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Anisotropic Diffusion Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);

		//	
		JLabel diffusionFactorLabel = new JLabel("Diffusion factor:");
		diffusionFactorLabel.setToolTipText("Use a larger diffusion factor for more denoising.");
		
		diffusionFactorPair = new SliderFieldPair(0, 100, floatFormat, params.diffusionFactorMin, params.diffusionFactorMax);
		diffusionFactorPair.setValue(params.diffusionFactor);
		diffusionFactorPair.addPropertyChangeListener(e -> { params.diffusionFactor = diffusionFactorPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider diffusionFactorSlider = diffusionFactorPair.getSlider();
		
		JFormattedTextField diffusionFactorField = diffusionFactorPair.getFloatField();
		diffusionFactorField.setColumns(5);

		//
		
		JLabel stepSizeLabel = new JLabel("Step size:");
		
		stepSizePair = new SliderFieldPair(0, 100, floatFormat, AnisotropicDiffusionParams.stepSizeMin, AnisotropicDiffusionParams.stepSizeMax);
		stepSizePair.setValue(params.stepSize);
		stepSizePair.addPropertyChangeListener(e -> { params.stepSize = stepSizePair.getValue(); fireParamsChangeEvent(); });
		
		JSlider stepSizeSlider = stepSizePair.getSlider();
		
		JFormattedTextField stepSizeField = stepSizePair.getFloatField();
		stepSizeField.setColumns(5);

		//
		
		iterationsPair = new SliderSpinnerPair(AnisotropicDiffusionParams.iterationsMin, AnisotropicDiffusionParams.iterationsMax);
		iterationsPair.setValue(params.numIterations);
		iterationsPair.addPropertyChangeListener(e -> { params.numIterations = iterationsPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider iterationsSlider = iterationsPair.getSlider();
		
		JSpinner iterationsSpinner = iterationsPair.getSpinner();
		
		JLabel iterationsLabel = new JLabel("Iterations:");

		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(diffusionFactorLabel)
			           .addComponent(stepSizeLabel)
			           .addComponent(iterationsLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(diffusionFactorField)
			           .addComponent(stepSizeField)
			           .addComponent(iterationsSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(diffusionFactorSlider)
			           .addComponent(stepSizeSlider)
			           .addComponent(iterationsSlider))
		      );
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(diffusionFactorLabel)
		    				  .addComponent(diffusionFactorField))
			           .addComponent(diffusionFactorSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(stepSizeLabel)
		    				  .addComponent(stepSizeField))
			           .addComponent(stepSizeSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(iterationsLabel)
		    				  .addComponent(iterationsSpinner))
			           .addComponent(iterationsSlider))
		      );  
		
		setLayout(layout);
	}
	
	@Override
	public void updatePanelFromParams()
	{
		diffusionFactorPair.updateRange(params.diffusionFactorMin, params.diffusionFactorMax, params.diffusionFactor);		
		stepSizePair.updateRange(AnisotropicDiffusionParams.stepSizeMin, AnisotropicDiffusionParams.stepSizeMax, params.stepSize);		
		iterationsPair.updateRange(AnisotropicDiffusionParams.iterationsMin, AnisotropicDiffusionParams.iterationsMax, params.numIterations);
	}
}