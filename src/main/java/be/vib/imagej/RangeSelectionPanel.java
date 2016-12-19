package be.vib.imagej;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

class RangeSelectionPanel extends JPanel
{
	private WizardModel model;
	
	public RangeSelectionPanel(WizardModel model)
	{
		this.model = model;
		buildUI();
	}

	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Slices to Denoise"));
		
		// TODO? Add extra information to the radio button text? (e.g. "Current slice (slice 47)" or "All 132 slices").
		//       The "All 132 slices" looks messy though. The number of sliced could be added in the info box at the top of the WizardPage.
		//       The actual current slice number could also be shown above.
		
		// TODO: what happens if the user changes the current slice while the RangeSelectionPanel is shown - should it be updated dynamically? I think so, since changing
		//       the current slice number in Fiji is in fact the easiest way to decided what slice to denoise. (Apart from the more blind setting of a range x-y of slices.
		//       Note: our WizardModel.imagePlus will have the current slice number. So it would be the DenoiseSummaryPanel that may have to listen to current slice changes
		//             if we were to display it there.
		
		// FIXME: does this all work well if the user deletes one or more slices from model.imagePlus while a RangeSelectionPanel is visible...?
		//        or can/should we somehow lock the image so that these changes are not possible?

	    JRadioButton allSlicesRadioButton = new JRadioButton("All slices");
	    allSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.ALL_SLICES);
	    
		JRadioButton currentSliceRadioButton = new JRadioButton("Current slice");
		currentSliceRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.CURRENT_SLICE);

	    JRadioButton rangeOfSlicesRadioButton = new JRadioButton("Range");
	    rangeOfSlicesRadioButton.setSelected(model.range.getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
	    
	    JTextField rangeField = new JTextField("e.g. 1-42", 10);
	    rangeField.setMaximumSize(rangeField.getPreferredSize());

	    // TODO: input verifier
	    // TODO: remove example text when rangeField gets focus, put it back when it has no valid range
	    // TODO: show example text dimmed, but show valid ranges as *undimmed*
	    
	    allSlicesRadioButton.addActionListener(e -> {
    		model.range = ImageRange.makeAllSlicesRange(model.imagePlus);
    	});

	    currentSliceRadioButton.addActionListener(e -> {
    		model.range = ImageRange.makeCurrentSliceRange(model.imagePlus);
	    });

	    rangeOfSlicesRadioButton.addActionListener(e -> {
	    	// TODO: get first and last from range input field
	    	int first = 1;
	    	int last = 1;
    		model.range = ImageRange.makeNumericSliceRange(model.imagePlus, first, last);
    	});

	    // Add radio buttons to a group to make them mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(allSlicesRadioButton);
	    group.add(currentSliceRadioButton);
	    group.add(rangeOfSlicesRadioButton);
	    
	    JPanel rangeRadioPanel = new JPanel();
	    rangeRadioPanel.setLayout(new BoxLayout(rangeRadioPanel, BoxLayout.LINE_AXIS));
	    rangeRadioPanel.add(rangeOfSlicesRadioButton);
	    rangeRadioPanel.add(rangeField);
				
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		allSlicesRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		currentSliceRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		rangeRadioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		add(allSlicesRadioButton);
		add(currentSliceRadioButton);
		add(rangeRadioPanel);
	}
}