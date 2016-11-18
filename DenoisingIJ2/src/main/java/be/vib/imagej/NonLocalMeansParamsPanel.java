package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class NonLocalMeansParamsPanel extends DenoiseParamsPanelBase 
{			
	public NonLocalMeansParamsPanel(NonLocalMeansParams params)
	{			
		setBorder(BorderFactory.createTitledBorder("Non-Local Means Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		SliderFieldPair sigmaPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansParams.sigmaMin, NonLocalMeansParams.sigmaMax);
		sigmaPair.setValue(params.sigma);
		sigmaPair.addPropertyChangeListener(e -> { params.sigma = sigmaPair.getValue(); fireChangeEvent(); });
				
		SliderSpinnerPair searchWindowPair = new SliderSpinnerPair(NonLocalMeansParams.searchWindowMin, NonLocalMeansParams.searchWindowMax);
		searchWindowPair.setValue(params.searchWindow);
		searchWindowPair.addPropertyChangeListener(e -> { params.searchWindow = searchWindowPair.getValue(); fireChangeEvent(); });

		SliderSpinnerPair halfBlockSizePair = new SliderSpinnerPair(NonLocalMeansParams.halfBlockSizeMin, NonLocalMeansParams.halfBlockSizeMax);
		halfBlockSizePair.setValue(params.halfBlockSize);
		halfBlockSizePair.addPropertyChangeListener(e -> { params.halfBlockSize = halfBlockSizePair.getValue(); fireChangeEvent(); });

		JLabel sigmaLabel = new JLabel("Sigma:");	
		JFormattedTextField sigmaField = sigmaPair.getFloatField();
		sigmaField.setColumns(5);		
		JSlider sigmaSlider = sigmaPair.getSlider();

		JLabel searchWindowLabel = new JLabel("Search window:");				
		JSpinner searchWindowSpinner = searchWindowPair.getSpinner();
		JSlider searchWindowSlider = searchWindowPair.getSlider();
		
		JLabel halfBlockSizeLabel = new JLabel("Half block size:");
		JSpinner halfBlockSizeSpinner = halfBlockSizePair.getSpinner();
		JSlider halfBlockSizeSlider = halfBlockSizePair.getSlider();
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
			           .addComponent(sigmaLabel)
			           .addComponent(searchWindowLabel)
		      		   .addComponent(halfBlockSizeLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(sigmaField)
			           .addComponent(searchWindowSpinner)
		      		   .addComponent(halfBlockSizeSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(sigmaSlider)
		               .addComponent(searchWindowSlider)
		               .addComponent(halfBlockSizeSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(sigmaLabel)
		    				  .addComponent(sigmaField))
			           .addComponent(sigmaSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(searchWindowLabel)
		    				  .addComponent(searchWindowSpinner))
			           .addComponent(searchWindowSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    		  		.addComponent(halfBlockSizeLabel)
		    		  		.addComponent(halfBlockSizeSpinner))
		    		  .addComponent(halfBlockSizeSlider))
		);		
		
		setLayout(layout);
	}
}