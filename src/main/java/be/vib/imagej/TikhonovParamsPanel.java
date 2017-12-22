package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class TikhonovParamsPanel extends DenoiseParamsPanelBase 
{
	private TikhonovParams params;
	private SliderFieldPair lambdaPair;
	private SliderSpinnerPair iterationsPair;
	private JCheckBox deconvolutionCheckBox;
	
	public TikhonovParamsPanel(TikhonovParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Tikhonov Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);

		// --		
		JLabel lambdaLabel = new JLabel("Lambda:");
		// FIXME: lambdaLabel.setToolTipText("xxxx");
		
		lambdaPair = new SliderFieldPair(0, 100, floatFormat, TikhonovParams.lambdaMin, TikhonovParams.lambdaMax);
		lambdaPair.setValue(params.deconvolution ? params.lambda2 : params.lambda1);
		lambdaPair.addPropertyChangeListener(e -> { if (params.deconvolution) params.lambda2 = lambdaPair.getValue(); else params.lambda1 = lambdaPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider lambdaSlider = lambdaPair.getSlider();
		
		JFormattedTextField lambdaField = lambdaPair.getFloatField();
		lambdaField.setColumns(5);

		//
		
		iterationsPair = new SliderSpinnerPair(TikhonovParams.iterationsMin, TikhonovParams.iterationsMax);
		iterationsPair.setValue(params.numIterations);
		iterationsPair.addPropertyChangeListener(e -> { params.numIterations = iterationsPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider iterationsSlider = iterationsPair.getSlider();
		
		JSpinner iterationsSpinner = iterationsPair.getSpinner();
		
		JLabel iterationsLabel = new JLabel("Iterations:");

		//
		
		deconvolutionCheckBox = new JCheckBox("Apply deconvolution");
		deconvolutionCheckBox.setSelected(params.deconvolution);
		deconvolutionCheckBox.addActionListener(e -> { params.deconvolution = deconvolutionCheckBox.isSelected(); EnableDeconvolutionControls(params.deconvolution); fireParamsChangeEvent(); });
		deconvolutionCheckBox.setToolTipText("Sharpen the image.");

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
			           .addComponent(iterationsSpinner)
					   .addComponent(deconvolutionCheckBox))
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
		      .addComponent(deconvolutionCheckBox)
		      );  
		
		setLayout(layout);
	}
	
	private void EnableDeconvolutionControls(boolean enable)
	{
// FIXME: probably not needed
//		lambdaLabel.setEnabled(enable);
//		lambdaField.setEnabled(enable);
//		lambdaSlider.setEnabled(enable);
	}
	
	@Override
	public void updatePanelFromParams()
	{
		deconvolutionCheckBox.setSelected(false);
		lambdaPair.updateRange(TikhonovParams.lambdaMin, TikhonovParams.lambdaMax, params.lambda1);		
		iterationsPair.updateRange(TikhonovParams.iterationsMin, TikhonovParams.iterationsMax, params.numIterations);
	}
}