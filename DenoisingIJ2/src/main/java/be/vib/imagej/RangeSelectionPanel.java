package be.vib.imagej;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

class RangeSelectionPanel extends JPanel
{
	private WizardModel model;
	
	public RangeSelectionPanel(WizardModel model)
	{
		this.model = model;
		
		setBorder(BorderFactory.createTitledBorder("Slices to Denoise"));

		JRadioButton currentSliceRadioButton = new JRadioButton("Current slice");
		currentSliceRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.CURRENT_SLICE);

	    JRadioButton allSlicesRadioButton = new JRadioButton("All slices");
	    allSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.ALL_SLICES);
	    
	    JRadioButton rangeOfSlicesRadioButton = new JRadioButton("Range");
	    rangeOfSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
	    // TODO: input field for range
	    
	    currentSliceRadioButton.addActionListener(e -> {
    		model.range = ImageRange.makeCurrentSliceRange(model.imagePlus);
	    });

	    allSlicesRadioButton.addActionListener(e -> {
    		model.range = ImageRange.makeAllSlicesRange(model.imagePlus);
    	});

	    rangeOfSlicesRadioButton.addActionListener(e -> {
	    	// TODO: get first and last from range input field
	    	int first = 1;
	    	int last = 1;
    		model.range = ImageRange.makeNumericSliceRange(model.imagePlus, first, last);
    	});

	    // Add radio buttons to group so they are mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(currentSliceRadioButton);
	    group.add(allSlicesRadioButton);
	    group.add(rangeOfSlicesRadioButton);
				
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(currentSliceRadioButton);
		add(allSlicesRadioButton);
		add(rangeOfSlicesRadioButton);

	}
}