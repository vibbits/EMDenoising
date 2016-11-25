package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class NonLocalMeansSCDParamsPanel extends DenoiseParamsPanelBase 
{
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
		
		SliderFieldPair lambdaPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCDParams.lambdaMin, NonLocalMeansSCDParams.lambdaMax);
		lambdaPair.setValue(params.lambda);
		lambdaPair.addPropertyChangeListener(e -> { params.lambda = lambdaPair.getValue(); fireChangeEvent(); });
		
		JSlider lambdaSlider = lambdaPair.getSlider();
		
		JFormattedTextField lambdaField = lambdaPair.getFloatField();
		lambdaField.setColumns(5);
		
		JLabel lambdaLabel = new JLabel("lambda:");
		
		// ----
		
		SliderFieldPair sigma0Pair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCDParams.sigma0Min, NonLocalMeansSCDParams.sigma0Max);
		sigma0Pair.setValue(params.sigma0);
		sigma0Pair.addPropertyChangeListener(e -> { params.sigma0 = sigma0Pair.getValue(); fireChangeEvent(); });
		
		JSlider sigma0Slider = sigma0Pair.getSlider();
		
		JFormattedTextField sigma0Field = sigma0Pair.getFloatField();
		sigma0Field.setColumns(5);
		
		JLabel sigma0Label = new JLabel("sigma0:");
		
		// ----
		
		SliderSpinnerPair iterationsPair = new SliderSpinnerPair(NonLocalMeansSCDParams.numIterationsMin, NonLocalMeansSCDParams.numIterationsMax);
		iterationsPair.setValue(params.numIterations);
		iterationsPair.addPropertyChangeListener(e -> { params.numIterations = iterationsPair.getValue(); fireChangeEvent(); });

		JLabel iterationsLabel = new JLabel("Iterations:");
		JSpinner iterationsSpinner = iterationsPair.getSpinner();
		JSlider iterationsSlider = iterationsPair.getSlider();

		// ----

		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(iterationsLabel)
		    		   .addComponent(sigma0Label)
		    		   .addComponent(lambdaLabel)
			           .addComponent(hLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(iterationsSpinner)
		    		   .addComponent(sigma0Field)
		    		   .addComponent(lambdaField)
			           .addComponent(hField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(iterationsSlider)
		    		   .addComponent(sigma0Slider)
		    		   .addComponent(lambdaSlider)
			           .addComponent(hSlider))
		      );
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(iterationsLabel)
		    				  .addComponent(iterationsSpinner))
			           .addComponent(iterationsSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(sigma0Label)
		    				  .addComponent(sigma0Field))
			           .addComponent(sigma0Slider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(lambdaLabel)
		    				  .addComponent(lambdaField))
			           .addComponent(lambdaSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(hLabel)
		    				  .addComponent(hField))
			           .addComponent(hSlider))
		      );    	
		
		setLayout(layout);
	}
}