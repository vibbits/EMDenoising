package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class NonLocalMeansSCDParamsPanel extends DenoiseParamsPanelBase 
{
	private JLabel iterationsLabel;
	private JSlider iterationsSlider;
	private JSpinner iterationsSpinner;
	
	private JLabel lambdaLabel;
	private JFormattedTextField lambdaField;
	private JSlider lambdaSlider;
	
	public NonLocalMeansSCDParamsPanel(NonLocalMeansSCDParams params)
	{
		setBorder(BorderFactory.createTitledBorder("Non-Local Means SCD Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		// ----
		
		SliderFieldPair hPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCDParams.hMin, NonLocalMeansSCDParams.hMax);
		hPair.setValue(params.h);
		hPair.addPropertyChangeListener(e -> { params.h = hPair.getValue(); fireChangeEvent(); });
		
		JSlider hSlider = hPair.getSlider();
		
		JFormattedTextField hField = hPair.getFloatField();
		hField.setColumns(5);
		
		JLabel hLabel = new JLabel("h:");
		
		// ----
		
		SliderFieldPair lambdaPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCDParams.DeconvolutionParams.lambdaMin, NonLocalMeansSCDParams.DeconvolutionParams.lambdaMax);
		lambdaPair.setValue(params.deconvolutionParams.lambda);
		lambdaPair.addPropertyChangeListener(e -> { params.deconvolutionParams.lambda = lambdaPair.getValue(); fireChangeEvent(); });
		
		lambdaSlider = lambdaPair.getSlider();
		
		lambdaField = lambdaPair.getFloatField();
		lambdaField.setColumns(5);
		
		lambdaLabel = new JLabel("lambda:");
		
		// ----
		
		SliderFieldPair sigma0Pair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCDParams.sigma0Min, NonLocalMeansSCDParams.sigma0Max);
		sigma0Pair.setValue(params.sigma0);
		sigma0Pair.addPropertyChangeListener(e -> { params.sigma0 = sigma0Pair.getValue(); fireChangeEvent(); });
		
		JSlider sigma0Slider = sigma0Pair.getSlider();
		
		JFormattedTextField sigma0Field = sigma0Pair.getFloatField();
		sigma0Field.setColumns(5);
		
		JLabel sigma0Label = new JLabel("sigma0:");
		
		// ----
		
		SliderSpinnerPair iterationsPair = new SliderSpinnerPair(NonLocalMeansSCDParams.DeconvolutionParams.numIterationsMin, NonLocalMeansSCDParams.DeconvolutionParams.numIterationsMax);
		iterationsPair.setValue(params.deconvolutionParams.numIterations);
		iterationsPair.addPropertyChangeListener(e -> { params.deconvolutionParams.numIterations = iterationsPair.getValue(); fireChangeEvent(); });

		iterationsLabel = new JLabel("Iterations:");
		iterationsSpinner = iterationsPair.getSpinner();
		iterationsSlider = iterationsPair.getSlider();

		// ----
		
		JCheckBox deconvolutionCheckBox = new JCheckBox("Deconvolution");
		deconvolutionCheckBox.setSelected(params.deconvolution);
		deconvolutionCheckBox.addActionListener(e -> { params.deconvolution = deconvolutionCheckBox.isSelected(); EnableDeconvolutionControls(params.deconvolution); fireChangeEvent(); });

		// Update controls dependent on whether we want deconvolution or not.
		EnableDeconvolutionControls(params.deconvolution);

		// ----
		
		// FIXME: in case of SCD: window too small for string representation of SCD params

		
		// Note: the way to pick these parameters is to first chose a nice h in the NLMS-SC variant,
		//       and use this h in NLMS-SCD. Then change lambda in NLMS-SCD.
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(hLabel)
		    		   .addComponent(sigma0Label)
		    		   .addComponent(iterationsLabel)
		    		   .addComponent(lambdaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hField)
		    		   .addComponent(sigma0Field)
		    		   .addComponent(deconvolutionCheckBox)
		    		   .addComponent(iterationsSpinner)
		    		   .addComponent(lambdaField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hSlider)
		    		   .addComponent(sigma0Slider)
		    		   .addComponent(iterationsSlider)
		    		   .addComponent(lambdaSlider))
		      );

		// Define top-to-bottom order
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(hLabel)
		    				  .addComponent(hField))
			           .addComponent(hSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(sigma0Label)
		    				  .addComponent(sigma0Field))
			           .addComponent(sigma0Slider))
		      .addComponent(deconvolutionCheckBox)
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
	
	private void EnableDeconvolutionControls(boolean enable)
	{
		iterationsLabel.setEnabled(enable);
		iterationsSlider.setEnabled(enable);
		iterationsSpinner.setEnabled(enable);
		
		lambdaLabel.setEnabled(enable);
		lambdaField.setEnabled(enable);
		lambdaSlider.setEnabled(enable);
	}
}