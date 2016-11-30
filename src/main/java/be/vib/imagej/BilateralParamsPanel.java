package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class BilateralParamsPanel extends DenoiseParamsPanelBase 
{	
	public BilateralParamsPanel(BilateralParams params)
	{
		setBorder(BorderFactory.createTitledBorder("Bilateral Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		SliderFieldPair alphaPair = new SliderFieldPair(0, 100, floatFormat, BilateralParams.alphaMin, BilateralParams.alphaMax);
		alphaPair.setValue(params.alpha);
		alphaPair.addPropertyChangeListener(e -> { params.alpha = alphaPair.getValue(); fireChangeEvent(); });
		
		JSlider alphaSlider = alphaPair.getSlider();
		
		JFormattedTextField alphaField = alphaPair.getFloatField();
		alphaField.setColumns(5);
		
		JLabel alphaLabel = new JLabel("Alpha:");

		//
		
		SliderFieldPair betaPair = new SliderFieldPair(0, 100, floatFormat, BilateralParams.betaMin, BilateralParams.betaMax);
		betaPair.setValue(params.beta);
		betaPair.addPropertyChangeListener(e -> { params.beta = betaPair.getValue(); fireChangeEvent(); });
		
		JSlider betaSlider = betaPair.getSlider();
		
		JFormattedTextField betaField = betaPair.getFloatField();
		betaField.setColumns(5);
		
		JLabel betaLabel = new JLabel("Beta:");

		//
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
		    		   .addComponent(alphaLabel)
			           .addComponent(betaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(alphaField)
			           .addComponent(betaField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
		    		   .addComponent(alphaSlider)
			           .addComponent(betaSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(alphaLabel)
		    				  .addComponent(alphaField))
			           .addComponent(alphaSlider))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(betaLabel)
		    				  .addComponent(betaField))
			           .addComponent(betaSlider))
		      );    	
		
		setLayout(layout);
	}
}