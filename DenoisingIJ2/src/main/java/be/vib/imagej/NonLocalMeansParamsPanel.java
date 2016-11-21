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
		
		SliderFieldPair hPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansParams.hMin, NonLocalMeansParams.hMax);
		hPair.setValue(params.h);
		hPair.addPropertyChangeListener(e -> { params.h = hPair.getValue(); fireChangeEvent(); });
				
		SliderSpinnerPair searchWindowPair = new SliderSpinnerPair(NonLocalMeansParams.halfSearchSizeMin, NonLocalMeansParams.halfSearchSizeMax);
		searchWindowPair.setValue(params.halfSearchSize);
		searchWindowPair.addPropertyChangeListener(e -> { params.halfSearchSize = searchWindowPair.getValue(); fireChangeEvent(); });

		SliderSpinnerPair halfBlockSizePair = new SliderSpinnerPair(NonLocalMeansParams.halfBlockSizeMin, NonLocalMeansParams.halfBlockSizeMax);
		halfBlockSizePair.setValue(params.halfBlockSize);
		halfBlockSizePair.addPropertyChangeListener(e -> { params.halfBlockSize = halfBlockSizePair.getValue(); fireChangeEvent(); });

		JLabel hLabel = new JLabel("h:");	
		JFormattedTextField hField = hPair.getFloatField();
		hField.setColumns(5);		
		JSlider hSlider = hPair.getSlider();

		JLabel searchWindowLabel = new JLabel("Half search window:");				
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
			           .addComponent(hLabel)
			           .addComponent(searchWindowLabel)
		      		   .addComponent(halfBlockSizeLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(hField)
			           .addComponent(searchWindowSpinner)
		      		   .addComponent(halfBlockSizeSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hSlider)
		               .addComponent(searchWindowSlider)
		               .addComponent(halfBlockSizeSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(hLabel)
		    				  .addComponent(hField))
			           .addComponent(hSlider))
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