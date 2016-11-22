package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class WaveletThresholdingParamsPanel extends DenoiseParamsPanelBase
{			
	public WaveletThresholdingParamsPanel(WaveletThresholdingParams params)
	{			
		setBorder(BorderFactory.createTitledBorder("Wavelet Thresholding Parameters"));
					
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		SliderFieldPair alphaPair = new SliderFieldPair(0, 100, floatFormat, WaveletThresholdingParams.alphaMin, WaveletThresholdingParams.alphaMax);
		alphaPair.setValue(params.alpha);
		alphaPair.addPropertyChangeListener(e -> { params.alpha = alphaPair.getValue(); fireChangeEvent(); });
		
		JSlider alphaSlider = alphaPair.getSlider();
		
		JFormattedTextField alphaField = alphaPair.getFloatField();
		alphaField.setColumns(5);
		
		JLabel alphaLabel = new JLabel("Alpha:");
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(alphaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(alphaField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(alphaSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(alphaLabel)
		    				  .addComponent(alphaField))
			           .addComponent(alphaSlider))
		);    	
		
		setLayout(layout);
	}
}