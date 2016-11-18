package be.vib.imagej;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

class NonLocalMeansParamsPanel extends DenoiseParamsPanelBase 
{
	private JSpinner searchWindowSpinner;
	private JSlider searchWindowSlider;
	
	private JSpinner halfBlockSizeSpinner;
	private JSlider halfBlockSizeSlider;

	private SliderFieldPair sigmaPair;
			
	public NonLocalMeansParamsPanel(NonLocalMeansParams params)
	{			
		setBorder(BorderFactory.createTitledBorder("Non-Local Means Denoising Parameters"));
		
		JLabel sigmaLabel = new JLabel("Sigma:");
		
		NumberFormat floatFormat = NumberFormat.getNumberInstance();
		floatFormat.setMinimumFractionDigits(2);
		
		sigmaPair = new SliderFieldPair(0, 100, floatFormat, NonLocalMeansParams.sigmaMin, NonLocalMeansParams.sigmaMax);
		sigmaPair.setValue(params.sigma);
		sigmaPair.addPropertyChangeListener(e -> { params.sigma = sigmaPair.getValue(); fireChangeEvent(); });
		
		JSlider sigmaSlider = sigmaPair.getSlider();
		
		JFormattedTextField sigmaField = sigmaPair.getFloatField();
		sigmaField.setColumns(5);
		
		final int searchWindowMin = NonLocalMeansParams.searchWindowMin;
		final int searchWindowMax = NonLocalMeansParams.searchWindowMax;
		
		JLabel searchWindowLabel = new JLabel("Search window:");
		
		SpinnerModel searchWindowSpinnerModel = new SpinnerNumberModel(params.searchWindow, searchWindowMin, searchWindowMax, 1);
		searchWindowSpinner = new JSpinner(searchWindowSpinnerModel);
		searchWindowSpinner.addChangeListener(e -> {
	    	int newValue = ((Number)searchWindowSpinner.getValue()).intValue();
	    	if (newValue == params.searchWindow) return;
	    	params.searchWindow = newValue;
			System.out.println("spinner updated model, searchWindow now:" + params.searchWindow);
			searchWindowSlider.setValue(newValue);
			fireChangeEvent();
	    });

		searchWindowSlider = new JSlider(searchWindowMin, searchWindowMax, params.searchWindow);		
		searchWindowSlider.addChangeListener(e -> {
	    	int newValue = ((Number)searchWindowSlider.getValue()).intValue();
	    	if (newValue == params.searchWindow) return;
	    	params.searchWindow = newValue;
			System.out.println("slider updated model, searchWindow now:" +params.searchWindow);
			searchWindowSpinner.setValue(newValue);
			fireChangeEvent();
	    });
		
		JLabel halfBlockSizeLabel = new JLabel("Half block size:");

		final int halfBlockSizeMin = NonLocalMeansParams.halfBlockSizeMin;
		final int halfBlockSizeMax = NonLocalMeansParams.halfBlockSizeMax;

		SpinnerModel halfBlockSizeSpinnerModel = new SpinnerNumberModel(params.halfBlockSize, halfBlockSizeMin, halfBlockSizeMax, 1);
		halfBlockSizeSpinner = new JSpinner(halfBlockSizeSpinnerModel);
		halfBlockSizeSpinner.addChangeListener(e -> {
	    	int newValue = ((Number)halfBlockSizeSpinner.getValue()).intValue();
	    	if (newValue == params.halfBlockSize) return;
	    	params.halfBlockSize = newValue;
			System.out.println("spinner updated model, halfBlockSize now:" + params.halfBlockSize);
			halfBlockSizeSlider.setValue(newValue);
			fireChangeEvent();
	    });
		
		halfBlockSizeSlider = new JSlider(halfBlockSizeMin, halfBlockSizeMax, params.halfBlockSize);
		halfBlockSizeSlider.addChangeListener(e -> {
	    	int newValue = ((Number)halfBlockSizeSlider.getValue()).intValue();
	    	if (newValue == params.halfBlockSize) return;
	    	params.halfBlockSize = newValue;
			System.out.println("slider updated model, halfBlockSize now:" + params.halfBlockSize);
			halfBlockSizeSpinner.setValue(newValue);
			fireChangeEvent();
	    });
		
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