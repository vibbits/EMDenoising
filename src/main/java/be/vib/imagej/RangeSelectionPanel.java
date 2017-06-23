package be.vib.imagej;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

// FIXME: enable/disable denoise button

// FIXME: add slice range to denoise image title (if numslices > 1 and not all slices denoised)

// FIXME: error message below range field, not next to it

// FIXME: "e.g. 1-10" in range field must disappear when user gives it focus and starts typing

// ----------------------------------------------------------------------------------------------------------------
// FIXME: what happens if the user changes the current slice while the RangeSelectionPanel is shown - should it be updated dynamically? I think so, since changing
// the current slice number in Fiji is in fact the easiest way to decided what slice to denoise. (Apart from the more blind setting of a range x-y of slices.
// Note: our WizardModel.imagePlus will have the current slice number. So it would be the DenoiseSummaryPanel that may have to listen to current slice changes
//    if we were to display it there.

// FIXME: does this all work well if the user deletes one or more slices from model.imagePlus while a RangeSelectionPanel is visible...?
// or can/should we somehow lock the image so that these changes are not possible?

// FIXME: what if the range that is filled in in the range field is not consistent with the current image anymore?
// ----------------------------------------------------------------------------------------------------------------

// Example behavior of Chrome's Print functionality:
// - if user types invalid input in range field, an error message appears underneath and print button is disabled
// - if range field is in error, user can switch to all pages or tab out of the range field or select another widget. In each case the invalid input remains in the field, but error disappears.
// - if range field is empty, a dimmed example is shown inside it e.g 1-5, 8, 11-13. This message disappears if the user starts typing.
// - clicking the range radio button automatically gives focus to the edit field next to it
// - even if the range edit field has focus, it shows a (dimmed) range example. Only if the user has typed something in it does it disappear.
// - if range edit field has focus, but is still empty, and user tabs away, then the all pages radio button becomes automatically chosen
//   if range edit field has focus, not empty but invalid input, and user tabs away, then the range radio button remain selected, but background is pink and print button is disabled

class RangeSelectionPanel extends JPanel
{
	private WizardModel model;
	private JRadioButton allSlicesRadioButton;
	private JRadioButton currentSliceRadioButton;
	private JRadioButton rangeOfSlicesRadioButton;
	private JLabel errorLabel;
	private HintedTextField rangeField;
	private RangeVerifier rangeVerifier;
	private ArrayList<ImageRangeChangeEventListener> listeners = new ArrayList<ImageRangeChangeEventListener>();
	
   	private enum RangeStatus
	{
		VALID_RANGE,
		INVALID_RANGE,
		OUT_OF_BOUNDS_RANGE
	};
	
	public RangeSelectionPanel(WizardModel model)
	{
		this.model = model;
		buildUI();
	}
		
	public void enable(boolean e)
	{
		allSlicesRadioButton.setEnabled(e);
		currentSliceRadioButton.setEnabled(e);
		rangeOfSlicesRadioButton.setEnabled(e);
	}
	
	public synchronized void addEventListener(ImageRangeChangeEventListener listener)
	{
		listeners.add(listener);
	}

	public synchronized void removeEventListener(ImageRangeChangeEventListener listener)
	{
		listeners.remove(listener);
	}

	private synchronized void fireRangeChangeEvent()
	{
		ImageRangeChangeEvent event = new ImageRangeChangeEvent(this);

		Iterator<ImageRangeChangeEventListener> i = listeners.iterator();
		while (i.hasNext())
		{
			((ImageRangeChangeEventListener)i.next()).handleImageRangeChangeEvent(event);
		}
	}
	
	class HintedTextField extends JTextField implements FocusListener
	{
		private final String hint;
		private boolean hintShown;

		public HintedTextField(final String hint, int columns)
		{
			super(hint, columns);
			this.hint = hint;
			this.hintShown = true;
			addFocusListener(this);
		}

		@Override
		public void focusGained(FocusEvent e)
		{
			if (getText().isEmpty())
			{
				super.setText("");
				hintShown = false;
			}
		}
		
		@Override
		public void focusLost(FocusEvent e)
		{
			if (getText().isEmpty())
			{
				super.setText(hint);
				hintShown = true;
			}
		}

		@Override
		public String getText()
		{
			return hintShown ? "" : super.getText();
		}
	}

	private class RangeVerifier extends InputVerifier
    {    	
		private ImageRange range = null;
		
    	public boolean shouldYieldFocus(JComponent input)
    	{
    		// The user can always leave the edit field, but the Denoise button
    		// will only become active if the range field content is valid.
    		return true;
    	}
    	
    	public ImageRange getRange(JComponent input)
    	{
    		boolean validRange = verify(input);
    		return validRange ? range : null;
    	}
    	
        public boolean verify(JComponent input)
        {
            JTextField tf = (JTextField)input;
            String text = tf.getText();
            
            // The regex detects: begin line + an optional integer + an optional hyphen + an optional number + end line
            //                    (with optional whitespace in between).
            // This corresponds to a single slice number (e.g. 5) or a range of slices (e.g. 5-10) or a range
            // of slices where the first or last slice number is implicit (e.g. 5- for slices 5 till the end, or e.g. -10
            // for the first slice up till the 10th).
            Pattern pattern = Pattern.compile("^\\s*(\\d+)?\\s*(-)?\\s*(\\d+)?\\s*$"); 
            Matcher matcher = pattern.matcher(text);
            range = null;

            boolean found = matcher.find();
            if (!found)
            {
        		setError(RangeStatus.INVALID_RANGE);
                return false;
            }
            else
            {
                String start = matcher.group(1);
                String hyphen = matcher.group(2);
                String end = matcher.group(3);
                
                if (start == null && end == null)
                {
            		setError(RangeStatus.INVALID_RANGE);
                	return false;
                }
                else
                {
                	int numSlices = model.getImage().getNSlices();
                	
                	int startSlice = 1;
                	int endSlice = numSlices;
                	
                	if (hyphen == null)
                	{
                		assert(start != null);
                		assert(end == null);
                		startSlice = endSlice = Integer.valueOf(start);
                	}
                	else
                	{
                		if (start != null)
                			startSlice = Integer.valueOf(start);
                		
                    	if (end != null)
                    		endSlice = Integer.valueOf(end);
                	}        
                	
                	if (startSlice > numSlices || endSlice > numSlices)  
                	{
                		setError(RangeStatus.OUT_OF_BOUNDS_RANGE);
                		return false;
                	}
                	
                	if (startSlice < 1 || endSlice < 1 || startSlice > endSlice)
                	{
                		setError(RangeStatus.INVALID_RANGE);
                		return false;
                	}
                	
            		setError(RangeStatus.VALID_RANGE);
            		range = ImageRange.makeNumericSliceRange(model.getImage(), startSlice, endSlice);
                	return true;
                }
            }
        }
        
        private void setError(RangeStatus status)
        {
        	switch (status)
        	{
        		case INVALID_RANGE:
            		errorLabel.setText("Invalid range syntax. Use ranges such as 5-10 or 5 or 5- or -10.");
            		break;
            		
        		case OUT_OF_BOUNDS_RANGE:
            		errorLabel.setText("Slice number is out of bounds. It must be smaller or equal to " + model.getImage().getNSlices() +".");
            		break;
            		
            	default:
        	}
        	
        	errorLabel.setVisible(status != RangeStatus.VALID_RANGE);
//        	
//        	rangeField.invalidate();
//        	validate();
        }
    }

	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Slices to Denoise"));
		
		int numSlices = model.getImage() != null ? model.getImage().getNSlices() : 1;
		
		currentSliceRadioButton = new JRadioButton("Current slice");
		currentSliceRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.CURRENT_SLICE);

	    allSlicesRadioButton = new JRadioButton("All slices");
	    allSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.ALL_SLICES);
	    allSlicesRadioButton.setEnabled(numSlices > 1);
	    
	    rangeOfSlicesRadioButton = new JRadioButton("Range");
	    rangeOfSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
	    rangeOfSlicesRadioButton.setEnabled(numSlices > 1);
	    
	    rangeVerifier = new RangeVerifier();
	    
	    rangeField = new HintedTextField("e.g. 1-42", 10);  // FIXME
	    rangeField.setMaximumSize(rangeField.getPreferredSize());
	    rangeField.setInputVerifier(rangeVerifier);
	    rangeField.setEnabled(numSlices > 1);
	    rangeField.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e)
			{
				rangeOfSlicesRadioButton.setSelected(true);
				updateRange();
			}

			@Override
			public void focusLost(FocusEvent e)
			{ 
			}
		});
	    
	    rangeField.addKeyListener(new KeyAdapter() {
	    	@Override
	    	public void keyReleased(KeyEvent event)
	    	{
	    		// Already show the range error message (if needed) while the range 
	    		// text field still has focus. It tells the user how to enter a valid range.
	    		boolean rangeValid = rangeVerifier.verify(rangeField);
	    		errorLabel.setVisible(!rangeValid);	    	
	    	}
	    });

	    currentSliceRadioButton.addActionListener(e -> { updateRange(); });
	    allSlicesRadioButton.addActionListener(e -> { updateRange(); });
	    rangeOfSlicesRadioButton.addActionListener(e -> { updateRange(); });

	    // Add radio buttons to a group to make them mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(currentSliceRadioButton);
	    group.add(allSlicesRadioButton);
	    group.add(rangeOfSlicesRadioButton);
	    
	    errorLabel = new JLabel();
	    errorLabel.setVisible(false);
				
	    JPanel rangeRadioPanel = new JPanel();
	    rangeRadioPanel.setLayout(new BoxLayout(rangeRadioPanel, BoxLayout.LINE_AXIS));
	    rangeRadioPanel.add(rangeOfSlicesRadioButton);
	    rangeRadioPanel.add(rangeField);
	    rangeRadioPanel.add(errorLabel); // FIXME: should go below not next to the range edit field
	    
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		allSlicesRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		currentSliceRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		rangeRadioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		add(currentSliceRadioButton);
		add(allSlicesRadioButton);
		// TODO: add(rangeRadioPanel);
	}
	
	public void updateRange() // called (among others) when user selects a different range radio button
	{		
		// System.out.println("updateRange()");
		
		int numSlices = model.getImage() != null ? model.getImage().getNSlices() : 1;

	    currentSliceRadioButton.setEnabled(true);
	    allSlicesRadioButton.setEnabled(numSlices > 1);
	    rangeOfSlicesRadioButton.setEnabled(numSlices > 1);

		boolean rangeValid = rangeVerifier.verify(rangeField);
		errorLabel.setVisible(rangeOfSlicesRadioButton.isSelected() && !rangeValid);
		
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
			model.setRange(rangeVerifier.getRange(rangeField));
		}
		
		fireRangeChangeEvent();
	}
}