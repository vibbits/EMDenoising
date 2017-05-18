package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class NonLocalMeansParamsPanel extends DenoiseParamsPanelBase 
{
	private JLabel lambdaLabel;
	private JFormattedTextField lambdaField;
	private JSlider lambdaSlider;
	
	public NonLocalMeansParamsPanel(NonLocalMeansParams params)
	{
		setBorder(BorderFactory.createTitledBorder("Non-Local Means Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		// ----
		
		SliderFieldPair hPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansParams.hMin, NonLocalMeansParams.hMax);
		hPair.setValue(params.h);
		hPair.addPropertyChangeListener(e -> { params.h = hPair.getValue(); fireChangeEvent(); });
		
		JSlider hSlider = hPair.getSlider();
		
		JFormattedTextField hField = hPair.getFloatField();
		hField.setColumns(5);
		
		JLabel hLabel = new JLabel("h:");
		
		// ----
		
		SliderFieldPair lambdaPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansParams.DeconvolutionParams.lambdaMin, NonLocalMeansParams.DeconvolutionParams.lambdaMax);
		lambdaPair.setValue(params.deconvolutionParams.lambda);
		lambdaPair.addPropertyChangeListener(e -> { params.deconvolutionParams.lambda = lambdaPair.getValue(); fireChangeEvent(); });
		
		lambdaSlider = lambdaPair.getSlider();
		
		lambdaField = lambdaPair.getFloatField();
		lambdaField.setColumns(5);
		
		lambdaLabel = new JLabel("lambda:");
		lambdaLabel.setToolTipText("Denoising versus sharpening trade-off.");
		
		// ----
		
		JCheckBox decorrelationCheckBox = new JCheckBox("Apply decorrelation");
		decorrelationCheckBox.setSelected(params.decorrelation);
		decorrelationCheckBox.addActionListener(e -> { params.decorrelation = decorrelationCheckBox.isSelected(); fireChangeEvent(); });

		// ----
		
		JCheckBox deconvolutionCheckBox = new JCheckBox("Apply deconvolution (slow)");
		deconvolutionCheckBox.setSelected(params.deconvolution);
		deconvolutionCheckBox.addActionListener(e -> { params.deconvolution = deconvolutionCheckBox.isSelected(); EnableDeconvolutionControls(params.deconvolution); fireChangeEvent(); });
		deconvolutionCheckBox.setToolTipText("Sharpen the image.");

		// Update controls that are dependent on whether we want deconvolution or not.
		EnableDeconvolutionControls(params.deconvolution);

		// ----
		
		SliderSpinnerPair blockSizePair = new SliderSpinnerPair(NonLocalMeansParams.halfBlockSizeMin, NonLocalMeansParams.halfBlockSizeMax);
		blockSizePair.setValue(params.halfBlockSize);
		blockSizePair.addPropertyChangeListener(e -> { params.halfBlockSize = blockSizePair.getValue(); fireChangeEvent(); });
		
		JSlider blockSizeSlider = blockSizePair.getSlider();
		
		JSpinner blockSizeSpinner = blockSizePair.getSpinner();
		
		JLabel blockSizeLabel = new JLabel("Block Size:");

		// ----
		
		SliderSpinnerPair searchSizePair = new SliderSpinnerPair(NonLocalMeansParams.halfSearchSizeMin, NonLocalMeansParams.halfSearchSizeMax);
		searchSizePair.setValue(params.halfSearchSize);
		searchSizePair.addPropertyChangeListener(e -> { params.halfSearchSize = searchSizePair.getValue(); fireChangeEvent(); });
		
		JSlider searchSizeSlider = searchSizePair.getSlider();
		
		JSpinner searchSizeSpinner = searchSizePair.getSpinner();
		
		JLabel searchSizeLabel = new JLabel("Search Window:");

		// ----
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(hLabel)
		    		   .addComponent(lambdaLabel)
		    		   .addComponent(blockSizeLabel)
		    		   .addComponent(searchSizeLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hField)
		    		   .addComponent(decorrelationCheckBox)
		    		   .addComponent(deconvolutionCheckBox)
		    		   .addComponent(lambdaField)
		    		   .addComponent(blockSizeSpinner)
		    		   .addComponent(searchSizeSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hSlider)
		    		   .addComponent(lambdaSlider)
		    		   .addComponent(blockSizeSlider)
		    		   .addComponent(searchSizeSlider))
		      );

		// Define top-to-bottom order
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(hLabel)
		    				  .addComponent(hField))
			           .addComponent(hSlider))

		      .addComponent(decorrelationCheckBox)
		      .addComponent(deconvolutionCheckBox)
	      
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(lambdaLabel)
		    				  .addComponent(lambdaField))
			           .addComponent(lambdaSlider))		      

		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(blockSizeLabel)
		    				  .addComponent(blockSizeSpinner))
			           .addComponent(blockSizeSlider))		      

		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(searchSizeLabel)
		    				  .addComponent(searchSizeSpinner))
			           .addComponent(searchSizeSlider))		      
				);    	
		
		setLayout(layout);
	}
	
	private void EnableDeconvolutionControls(boolean enable)
	{
		lambdaLabel.setEnabled(enable);
		lambdaField.setEnabled(enable);
		lambdaSlider.setEnabled(enable);
	}
}