package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class TotalVariationParamsPanel extends DenoiseParamsPanelBase 
{
	private TotalVariationParams params;
	private SliderFieldPair lambdaPair;
	private SliderSpinnerPair iterationsPair;
	
	public TotalVariationParamsPanel(TotalVariationParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Total Variation Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);

		// --		
		JLabel lambdaLabel = new JLabel("Lambda:");
		lambdaLabel.setToolTipText("Larger values yield more noise suppression.");
		
		lambdaPair = new SliderFieldPair(0, 100, floatFormat, TotalVariationParams.lambdaMin, TotalVariationParams.lambdaMax);
		lambdaPair.setValue(params.lambda);
		lambdaPair.addPropertyChangeListener(e -> { params.lambda = lambdaPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider lambdaSlider = lambdaPair.getSlider();
		
		JFormattedTextField lambdaField = lambdaPair.getFloatField();
		lambdaField.setColumns(5);

		//
		
		iterationsPair = new SliderSpinnerPair(TotalVariationParams.iterationsMin, TotalVariationParams.iterationsMax);
		iterationsPair.setValue(params.numIterations);
		iterationsPair.addPropertyChangeListener(e -> { params.numIterations = iterationsPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider iterationsSlider = iterationsPair.getSlider();
		
		JSpinner iterationsSpinner = iterationsPair.getSpinner();
		
		JLabel iterationsLabel = new JLabel("Iterations:");
		iterationsLabel.setToolTipText("A larger value yields a better optimization, but is slower.");

		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(lambdaLabel)
			           .addComponent(iterationsLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(lambdaField)
			           .addComponent(iterationsSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(lambdaSlider)
			           .addComponent(iterationsSlider))
		      );
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(lambdaLabel)
		    				  .addComponent(lambdaField))
			           .addComponent(lambdaSlider))
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
		lambdaPair.updateRange(TotalVariationParams.lambdaMin, TotalVariationParams.lambdaMax, params.lambda);		
		iterationsPair.updateRange(TotalVariationParams.iterationsMin, TotalVariationParams.iterationsMax, params.numIterations);
	}
}