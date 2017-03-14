package be.vib.imagej;

import java.awt.Component;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

class RangeSelectionPanel extends JPanel
{
	private WizardModel model;
	private JRadioButton allSlicesRadioButton;
	private JRadioButton currentSliceRadioButton;
	private JRadioButton rangeOfSlicesRadioButton;
	
	public RangeSelectionPanel(WizardModel model)
	{
		this.model = model;
		buildUI();
	}
	
// http://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html#inputVerification
//
//    private class RangeVerifier extends InputVerifier
//    {    	
//    	public boolean shouldYieldFocus(JComponent input)
//    	{
//    		boolean inputOK = verify(input);
//    		if (inputOK)
//    		{
//    			return true;
//    		}
//    		else
//    		{
//    			Toolkit.getDefaultToolkit().beep();
//    			return false;
//    		}
//    	}
//    	
//        public boolean verify(JComponent input)
//        {
//            JTextField tf = (JTextField)input;
//            return "pass".equals(tf.getText());
//        }
//    }

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

	    allSlicesRadioButton = new JRadioButton("All slices");
	    allSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.ALL_SLICES);
	    
		currentSliceRadioButton = new JRadioButton("Current slice");
		currentSliceRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.CURRENT_SLICE);

	    rangeOfSlicesRadioButton = new JRadioButton("Range");
	    rangeOfSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
	    
	    JTextField rangeField = new JTextField("e.g. 1-42", 10);
	    rangeField.setMaximumSize(rangeField.getPreferredSize());

	    // TODO: input verifier
	    // TODO: remove example text when rangeField gets focus, put it back when it has no valid range
	    // TODO: show example text dimmed, but show valid ranges as *undimmed*
	    
	    allSlicesRadioButton.addActionListener(e -> { updateRange(); });
	    currentSliceRadioButton.addActionListener(e -> { updateRange(); });
	    rangeOfSlicesRadioButton.addActionListener(e -> { updateRange(); });

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
	
	public void updateRange()
	{
		if (allSlicesRadioButton.isSelected())
		{
    		model.setRange(ImageRange.makeAllSlicesRange(model.getImage()));
		}
		else if (currentSliceRadioButton.isSelected())
		{
    		model.setRange(ImageRange.makeCurrentSliceRange(model.getImage()));
		}
		else if (rangeOfSlicesRadioButton.isSelected())
		{
    		model.setRange(ImageRange.makeAllSlicesRange(model.getImage()));  // FIXME
    		
	    	// TODO: get first and last from range input field
	    	// int first = ...;
	    	// int last = ...;
    		// model.range = ImageRange.makeNumericSliceRange(model.imagePlus, first, last);
    		
    		// TODO: what if the range that is filled in in the range field is not consistent with the current image anymore?
		}
	}
}