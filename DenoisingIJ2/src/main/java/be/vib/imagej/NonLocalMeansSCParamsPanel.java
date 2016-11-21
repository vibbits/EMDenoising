package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class NonLocalMeansSCParamsPanel extends DenoiseParamsPanelBase 
{
	private SliderFieldPair hPair;
	
	public NonLocalMeansSCParamsPanel(NonLocalMeansSCParams params)
	{
		setBorder(BorderFactory.createTitledBorder("Non-Local Means SC Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		hPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansSCParams.hMin, NonLocalMeansSCParams.hMax);
		hPair.setValue(params.h);
		hPair.addPropertyChangeListener(e -> { params.h = hPair.getValue(); fireChangeEvent(); });
		
		JSlider hSlider = hPair.getSlider();
		
		JFormattedTextField hField = hPair.getFloatField();
		hField.setColumns(5);
		
		JLabel hLabel = new JLabel("h:");

		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(hLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(hField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(hSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(hLabel)
		    				  .addComponent(hField))
			           .addComponent(hSlider))
		);    	
		
		setLayout(layout);
	}
}