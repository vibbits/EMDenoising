package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;

class GaussianParamsPanel extends DenoiseParamsPanelBase
{	
	private SliderFieldPair sigmaPair;
	private GaussianParams params;
	
	public GaussianParamsPanel(GaussianParams params)
	{
		this.params = params;
		buildUI();
	}

	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Gaussian Denoising Parameters"));
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		sigmaPair = new SliderFieldPair(0, 100, floatFormat, params.sigmaMin, params.sigmaMax);
		sigmaPair.setValue(params.sigma);
		sigmaPair.addPropertyChangeListener(e -> { params.sigma = sigmaPair.getValue(); fireParamsChangeEvent(); });
		
		JSlider sigmaSlider = sigmaPair.getSlider();
		
		JFormattedTextField sigmaField = sigmaPair.getFloatField();
		sigmaField.setColumns(5);
		
		JLabel sigmaLabel = new JLabel("Sigma:");
		sigmaLabel.setToolTipText("A larger sigma yields a less noisy but also a more blurry result.");

		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
				
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			           .addComponent(sigmaLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(sigmaField))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
			           .addComponent(sigmaSlider))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    		   .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    				  .addComponent(sigmaLabel)
		    				  .addComponent(sigmaField))
			           .addComponent(sigmaSlider))
		);    	
		
		setLayout(layout);		
	}
	
	@Override
	public void updatePanelFromParams()
	{
		sigmaPair.updateRange(params.sigmaMin, params.sigmaMax, params.sigma);		
	}
}