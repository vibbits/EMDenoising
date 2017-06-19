package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

class BilateralParamsPanel extends DenoiseParamsPanelBase 
{	
	private BilateralParams params;
	private SliderFieldPair hPair;
	private SliderSpinnerPair rPair;
	
	public BilateralParamsPanel(BilateralParams params)
	{
		this.params = params;
		buildUI();
	}
	
	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Bilateral Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		hPair = new SliderFieldPair(0, 100, floatFormat, params.hMin, params.hMax);
		hPair.setValue(params.h);
		hPair.addPropertyChangeListener(e -> { params.h = hPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider hSlider = hPair.getSlider();
		
		JFormattedTextField hField = hPair.getFloatField();
		hField.setColumns(5);
		
		JLabel hLabel = new JLabel("h:");

		//
		
		rPair = new SliderSpinnerPair(BilateralParams.rMin, BilateralParams.rMax);
		rPair.setValue(params.r);
		rPair.addPropertyChangeListener(e -> { params.r = rPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider rSlider = rPair.getSlider();
		
		JSpinner rSpinner = rPair.getSpinner();
		
		JLabel rLabel = new JLabel("Radius:");

		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(hLabel)
			           .addComponent(rLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hField)
			           .addComponent(rSpinner))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(hSlider)
			           .addComponent(rSlider))
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
		    				  .addComponent(rLabel)
		    				  .addComponent(rSpinner))
			           .addComponent(rSlider))
		      );    	
		
		setLayout(layout);
	}
	
	@Override
	public void updatePanelFromParams()
	{
		hPair.updateRange(params.hMin, params.hMax, params.h);	
		rPair.setValue(params.r);		// FIXME: for generality it would be nicer to updateRange (but we know that currently for bilateral filtering the iterations range is never changed)
	}
}