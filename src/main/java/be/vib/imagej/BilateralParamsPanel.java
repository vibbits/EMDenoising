package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class BilateralParamsPanel extends DenoiseParamsPanelBase 
{	
	private BilateralParams params;
	private SliderFieldPair spatialSigmaPair;
	private SliderFieldPair rangeSigmaPair;
	
	public BilateralParamsPanel(BilateralParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Bilateral Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(1);
		
		//
		
		spatialSigmaPair = new SliderFieldPair(0, 100, floatFormat, params.spatialSigmaMin, params.spatialSigmaMax);
		spatialSigmaPair.setValue(params.spatialSigma);
		spatialSigmaPair.addPropertyChangeListener(e -> { params.spatialSigma = spatialSigmaPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider spatialSigmaSlider = spatialSigmaPair.getSlider();
		
		JFormattedTextField spatialSigmaField = spatialSigmaPair.getFloatField();
		spatialSigmaField.setColumns(5);
		
		JLabel spatialSigmaLabel = new JLabel("Spatial sigma:");
		spatialSigmaLabel.setToolTipText("A larger value removes more noise, and details.");

		//
		
		rangeSigmaPair = new SliderFieldPair(0, 100, floatFormat, params.rangeSigmaMin, params.rangeSigmaMax);
		rangeSigmaPair.setValue(params.rangeSigma);
		rangeSigmaPair.addPropertyChangeListener(e -> { params.rangeSigma = rangeSigmaPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider rangeSigmaSlider = rangeSigmaPair.getSlider();
		
		JFormattedTextField rangeSigmaField = rangeSigmaPair.getFloatField();
		rangeSigmaField.setColumns(5);
		
		JLabel rangeSigmaLabel = new JLabel("Range sigma:");
		rangeSigmaLabel.setToolTipText("A smaller value preserves edges better.");


		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(spatialSigmaLabel)
			           .addComponent(rangeSigmaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(spatialSigmaField)
			           .addComponent(rangeSigmaField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(spatialSigmaSlider)
			           .addComponent(rangeSigmaSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(spatialSigmaLabel)
		    				  .addComponent(spatialSigmaField))
			           .addComponent(spatialSigmaSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(rangeSigmaLabel)
		    				  .addComponent(rangeSigmaField))
			           .addComponent(rangeSigmaSlider))
		      );    	
		
		setLayout(layout);
	}
	
	@Override
	public void updatePanelFromParams()
	{
		spatialSigmaPair.updateRange(params.spatialSigmaMin, params.spatialSigmaMax, params.spatialSigma);	
		rangeSigmaPair.updateRange(params.rangeSigmaMin, params.rangeSigmaMax, params.rangeSigma);	
	}
}
