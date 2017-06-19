package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class WaveletThresholdingParamsPanel extends DenoiseParamsPanelBase
{			
	private SliderFieldPair thresholdPair;
	private WaveletThresholdingParams params;
	
	public WaveletThresholdingParamsPanel(WaveletThresholdingParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{	
		setBorder(BorderFactory.createTitledBorder("Wavelet Thresholding Parameters"));
					
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		thresholdPair = new SliderFieldPair(0, 100, floatFormat, params.thresholdMin, params.thresholdMax);
		thresholdPair.setValue(params.threshold);
		thresholdPair.addPropertyChangeListener(e -> { params.threshold = thresholdPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider thresholdSlider = thresholdPair.getSlider();
		
		JFormattedTextField thresholdField = thresholdPair.getFloatField();
		thresholdField.setColumns(5);
		
		JLabel thresholdLabel = new JLabel("Threshold:");
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(thresholdLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(thresholdField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(thresholdSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(thresholdLabel)
		    				  .addComponent(thresholdField))
			           .addComponent(thresholdSlider))
		);    	
		
		setLayout(layout);
	}
	
	@Override
	public void updatePanelFromParams()
	{
		thresholdPair.updateRange(params.thresholdMin, params.thresholdMax, params.threshold);		
	}
}