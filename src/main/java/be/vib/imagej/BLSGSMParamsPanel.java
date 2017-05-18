package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class BLSGSMParamsPanel extends DenoiseParamsPanelBase 
{
	private SliderFieldPair sigmaPair;
	
	public BLSGSMParamsPanel(BLSGSMParams params)
	{
		setBorder(BorderFactory.createTitledBorder("BLS-GSM Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		sigmaPair = new SliderFieldPair(0, 100, floatFormat, BLSGSMParams.sigmaMin, BLSGSMParams.sigmaMax);
		sigmaPair.setValue(params.sigma);
		sigmaPair.addPropertyChangeListener(e -> { params.sigma = sigmaPair.getValue(); fireChangeEvent(); });
		
		JSlider sigmaSlider = sigmaPair.getSlider();
		
		JFormattedTextField sigmaField = sigmaPair.getFloatField();
		sigmaField.setColumns(5);
		
		JLabel sigmaLabel = new JLabel("Sigma:");
		sigmaLabel.setToolTipText("Larger values of sigma yield stronger denoising.");

		//
		
		SliderSpinnerPair scalesPair = new SliderSpinnerPair(BLSGSMParams.scalesMin, BLSGSMParams.scalesMax);
		scalesPair.setValue(params.scales);
		scalesPair.addPropertyChangeListener(e -> { params.scales = scalesPair.getValue(); fireChangeEvent(); });
		
		JSlider scalesSlider = scalesPair.getSlider();
		
		JSpinner scalesSpinner = scalesPair.getSpinner();
		
		JLabel scalesLabel = new JLabel("Analysis scales:");

		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(sigmaLabel)
			           .addComponent(scalesLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(sigmaField)
			           .addComponent(scalesSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(sigmaSlider)
			           .addComponent(scalesSlider))
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
		    				  .addComponent(scalesLabel)
		    				  .addComponent(scalesSpinner))
			           .addComponent(scalesSlider))
		);    	
		
		setLayout(layout);
	}
}