package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

class WaveletThresholdingParamsPanel extends DenoiseParamsPanelBase
{		
	public WaveletThresholdingParamsPanel(WaveletThresholdingParams params)
	{			
		setBorder(BorderFactory.createTitledBorder("Wavelet Thresholding Parameters"));
					
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		JLabel alphaLabel = new JLabel("Alpha:");
		JFormattedTextField alphaField = new JFormattedTextField(floatFormat);
		alphaField.setColumns(5);
		alphaField.setValue(new Float(params.alpha));
		alphaField.addPropertyChangeListener("value", e -> { params.alpha = ((Number)alphaField.getValue()).floatValue();
                                                             System.out.println("model updated, alpha now:" + params.alpha);
                                                             fireChangeEvent(); });
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(alphaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(alphaField))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           .addComponent(alphaLabel)
			           .addComponent(alphaField))
		);		
		
		setLayout(layout);
	}
}