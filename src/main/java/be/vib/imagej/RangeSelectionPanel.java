package be.vib.imagej;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

class RangeSelectionPanel extends JPanel
{
	private WizardModel model;
	
	private JRadioButton allSlicesRadioButton;
	private JRadioButton currentSliceRadioButton;
	private JRadioButton rangeOfSlicesRadioButton;
	private FromToSlicesPanel fromToSlicesPanel;
	private JLabel errorLabel;
	
	private JButton denoiseButton;

	private ArrayList<ImageRangeChangeEventListener> listeners = new ArrayList<ImageRangeChangeEventListener>();
	
	private enum RangeState
	{
		NO_ERROR,
		NUMBERS_ERROR,  // slice value either not an integer or outside 1 to numslices
		ORDER_ERROR     // from slice number > to slice number
	}
	
	// A class for verifying two text fields ("from" and "to") that together represent a numeric range.
	// The "from" field value must be between a minimum value and the value of the "to" field,
	// the "to" field between the value of the "from" field and a maximum value.
	public class RangeVerifier extends InputVerifier
	{
		private int minVal;
		private int maxVal;
		private JFormattedTextField fromField;
		private JFormattedTextField toField;
		
		public RangeVerifier(JFormattedTextField fromField, JFormattedTextField toField, int minVal, int maxVal)
		{
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.fromField = fromField;
			this.toField = toField;
		}
		
		public void setRange(int minVal, int maxVal)
		{
			this.minVal = minVal;
			this.maxVal = maxVal;
		}
		
		@Override
		public boolean shouldYieldFocus(JComponent input)
		{
			// Even if the field's contents are invalid it can safely lose focus.
			return true;
		}
		
	    @Override
	    public boolean verify(JComponent input)
	    {
	       String text = ((JTextField)input).getText();
	       
	       int value = 0;
	       try
	       {
	           value = Integer.parseInt(text);
	       }
	       catch (NumberFormatException e)
	       {
	    	   setRangeState(RangeState.NUMBERS_ERROR, minVal, maxVal);
	           return false;
	       }
	       
	       int newFromVal = (input == fromField) ? value : (int)fromField.getValue();
	       int newToVal = (input == toField) ? value : (int)toField.getValue();
	       	       
    	   if (newFromVal < minVal || newFromVal > maxVal || newToVal < minVal || newToVal > maxVal)
    	   {
    		   setRangeState(RangeState.NUMBERS_ERROR, minVal, maxVal);
    		   return false;
    	   }
    	   else if (newFromVal > newToVal)
    	   {
    		   setRangeState(RangeState.ORDER_ERROR, minVal, maxVal);
    		   return false;	    		   
    	   }
    	   else
    	   {
    		   setRangeState(RangeState.NO_ERROR, minVal, maxVal);
    		   return true;
    	   }
	    }
	}
	
	private class FromToSlicesPanel extends JPanel
	{
	    private JFormattedTextField fromSliceField;
	    private JFormattedTextField toSliceField;
	    private JLabel toSliceLabel;
	    private RangeVerifier rangeVerifier;
	    
	    public FromToSlicesPanel()
		{
	    	super();
	    	
	    	fromSliceField = new JFormattedTextField();
	    	toSliceField = new JFormattedTextField();

		    rangeVerifier = new RangeVerifier(fromSliceField, toSliceField, 1, 1);

		    initSliceField(fromSliceField);
		    initSliceField(toSliceField);

		    toSliceLabel = new JLabel(" to ");

		    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));		
		    add(fromSliceField);
		    add(toSliceLabel);
		    add(toSliceField);
		    add(Box.createHorizontalGlue());
		}
	    
	    @Override
	    public void setEnabled(boolean enabled)
	    {
	    	super.setEnabled(enabled);
		    fromSliceField.setEnabled(enabled);
		    toSliceField.setEnabled(enabled);
		    toSliceLabel.setEnabled(enabled);	    	
	    }
	    
	    // Update the "to" and "from" fields with a valid range.
	    public void setRange(int from, int to)
	    {
	    	rangeVerifier.setRange(from, to);
	    	fromSliceField.setValue(from);
			toSliceField.setValue(to);
			
		    setRangeState(RangeState.NO_ERROR, from, to);
	    }
	    
	    public int fromValue()
	    {
	    	return (int)fromSliceField.getValue();
	    }
		
	    public int toValue()
	    {
	    	return (int)toSliceField.getValue();
	    }
	    
	    public boolean isValidRange()
	    {
	    	return rangeVerifier.verify(fromSliceField) && rangeVerifier.verify(toSliceField);
	    }
	    
		private void initSliceField(JFormattedTextField field)
		{
		    field.setColumns(4);
		    field.setMaximumSize(field.getPreferredSize());
		    field.setInputVerifier(rangeVerifier);
		    
		    field.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e)
				{
					// Clicking in a slice range edit field must have the same effect
					// as clicking on the radio button that is associated with it.
					rangeOfSlicesRadioButton.doClick();
				}		    	
		    });
		    
		    field.addKeyListener(new KeyAdapter() {
		    	@Override
		    	public void keyReleased(KeyEvent event)
		    	{
		    		boolean valid = fromToSlicesPanel.isValidRange();
		    		setValidState(valid);
		    	}
		    });
		}
		
		public void setValidState(boolean valid)
		{
    		denoiseButton.setEnabled(valid); 
    		errorLabel.setVisible(!valid);
		}
	}

	public RangeSelectionPanel(WizardModel model, JButton denoiseButton)
	{
		this.model = model;
		this.denoiseButton = denoiseButton;
		buildUI();
	}
		
	private void buildUI()
	{
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Slices to Denoise"), new EmptyBorder(10, 10, 10, 10)));

	    fromToSlicesPanel = new FromToSlicesPanel();
	    errorLabel = new JLabel();

	    int numSlices = model.getImage() != null ? model.getImage().getNSlices() : 1;
		
		currentSliceRadioButton = new JRadioButton("Current slice");
		currentSliceRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.CURRENT_SLICE);

	    allSlicesRadioButton = new JRadioButton("All slices");
	    allSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.ALL_SLICES);
	    allSlicesRadioButton.setEnabled(numSlices > 1);
	    
	    rangeOfSlicesRadioButton = new JRadioButton("Slices");
	    rangeOfSlicesRadioButton.setSelected(model.getRange().getType() == ImageRange.RangeType.NUMERIC_SLICE_RANGE);
	    rangeOfSlicesRadioButton.setEnabled(numSlices > 1);

	    currentSliceRadioButton.addActionListener(e -> { fromToSlicesPanel.setValidState(true); fireRangeChangeEvent(); });
	    allSlicesRadioButton.addActionListener(e -> { fromToSlicesPanel.setValidState(true); fireRangeChangeEvent(); });
	    rangeOfSlicesRadioButton.addActionListener(e -> { fromToSlicesPanel.setValidState(fromToSlicesPanel.isValidRange()); fireRangeChangeEvent(); });

	    // Add radio buttons to a group to make them mutually exclusive
	    ButtonGroup group = new ButtonGroup();
	    group.add(currentSliceRadioButton);
	    group.add(allSlicesRadioButton);
	    group.add(rangeOfSlicesRadioButton);
	    
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.gridwidth = 2; add(currentSliceRadioButton, c);
		c.gridx = 0; c.gridy = 1; c.weightx = 0; c.gridwidth = 2; add(allSlicesRadioButton, c);
		c.gridx = 0; c.gridy = 2; c.weightx = 0; c.gridwidth = 1; add(rangeOfSlicesRadioButton, c);
		c.gridx = 1; c.gridy = 2; c.weightx = 1; c.gridwidth = 1; add(fromToSlicesPanel, c);		
		c.gridx = 1; c.gridy = 3; c.weighty = 1; c.gridwidth = 1; add(errorLabel, c);		
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		allSlicesRadioButton.setEnabled(enabled);
		currentSliceRadioButton.setEnabled(enabled);
		rangeOfSlicesRadioButton.setEnabled(enabled);
		fromToSlicesPanel.setEnabled(enabled);
	}
	
	public void aboutToShow()
	{
		int numSlices = model.getImage() != null ? model.getImage().getNSlices() : 1;

	    currentSliceRadioButton.setEnabled(true);
	    allSlicesRadioButton.setEnabled(numSlices > 1);  
	    rangeOfSlicesRadioButton.setEnabled(numSlices > 1);
	    
	    fromToSlicesPanel.setEnabled(numSlices > 1);

	    currentSliceRadioButton.setSelected(true);

	    fromToSlicesPanel.setRange(1, numSlices);
	}
	
	public void setRangeState(RangeState state, int minVal, int maxVal)
	{
		switch (state)
		{
			default:
			case NO_ERROR:
				errorLabel.setVisible(false);
				break;
			case NUMBERS_ERROR:
				errorLabel.setText("Slices must be numbers between " + minVal + " and " + maxVal + ".");
				errorLabel.setVisible(true);
				break;
			case ORDER_ERROR:
				errorLabel.setText("The first slice must be smaller or equal to the second.");
				errorLabel.setVisible(true);
				break;
		}
	}

	public ImageRange getRange()
	{				
		// Note: we only get here when the range is valid
		//       (= we have an image and if user selected a slices range, it is valid)
		
		if (allSlicesRadioButton.isSelected())
		{
    		return ImageRange.makeAllSlicesRange(model.getImage());
		}
		else if (currentSliceRadioButton.isSelected())
		{
    		return ImageRange.makeCurrentSliceRange(model.getImage());
		}
		else if (rangeOfSlicesRadioButton.isSelected())
		{
			return ImageRange.makeNumericSliceRange(model.getImage(), fromToSlicesPanel.fromValue(), fromToSlicesPanel.toValue());
		}
		else
		{
			return null;
		}		
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
}