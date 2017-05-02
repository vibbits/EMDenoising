package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class AnisotropicDiffusionParamsPanel extends DenoiseParamsPanelBase 
{
	public AnisotropicDiffusionParamsPanel(AnisotropicDiffusionParams params)
	{
		setBorder(BorderFactory.createTitledBorder("Anisotropic Diffusion Denoising Parameters"));
		
		JLabel diffusionFactorLabel = new JLabel("Diffusion factor:");
		diffusionFactorLabel.setToolTipText("Use a larger diffusion factor for more denoising.");
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		SliderFieldPair diffusionFactorPair = new SliderFieldPair(0, 100, floatFormat, AnisotropicDiffusionParams.diffusionFactorMin, AnisotropicDiffusionParams.diffusionFactorMax);
		diffusionFactorPair.setValue(params.diffusionFactor);
		diffusionFactorPair.addPropertyChangeListener(e -> { params.diffusionFactor = diffusionFactorPair.getValue(); fireChangeEvent(); });
		
		JSlider diffusionFactorSlider = diffusionFactorPair.getSlider();
		
		JFormattedTextField diffusionFactorField = diffusionFactorPair.getFloatField();
		diffusionFactorField.setColumns(5);
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(diffusionFactorLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(diffusionFactorField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(diffusionFactorSlider))
		      );
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(diffusionFactorLabel)
		    				  .addComponent(diffusionFactorField))
			           .addComponent(diffusionFactorSlider))
		      );  
		
		setLayout(layout);
	}
}