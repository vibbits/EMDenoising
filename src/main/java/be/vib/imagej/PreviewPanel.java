package be.vib.imagej;

import java.awt.image.BufferedImage;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PreviewPanel extends JPanel
{
	private Preferences preferences;
	private JLabel titleLabel;
	private JLabel blurEstimateLabel;
	private JLabel noiseEstimateLabel;
	ImagePanel imagePanel;
	
	public PreviewPanel(String title, Preferences preferences)
	{
		this.preferences = preferences;
		
		titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		
		imagePanel = new ImagePanel(this);

		noiseEstimateLabel = new JLabel("Noise: 0.000");
		noiseEstimateLabel.setToolTipText("Estimated standard deviation of the noise in the image, with pixel intensities rescaled to the range 0-1.");
		noiseEstimateLabel.setVisible(preferences.getBoolean("imagestats.shownoise", false));
		
		blurEstimateLabel = new JLabel("Blur: 0.000");
		blurEstimateLabel.setToolTipText("Estimated amount of blur in the image (between 0 and 1)");
		blurEstimateLabel.setVisible(preferences.getBoolean("imagestats.showblur", false));

		buildUI();
		
		addPreferencesListener();
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		noiseEstimateLabel.setAlignmentX(CENTER_ALIGNMENT);
		blurEstimateLabel.setAlignmentX(CENTER_ALIGNMENT);
		
		add(titleLabel);
		add(imagePanel);
		add(Box.createVerticalStrut(5));
		add(noiseEstimateLabel);
		add(blurEstimateLabel);
		add(Box.createVerticalStrut(5)); // title label introduces some space at the top, also leave some space at the bottom for visual symmetry			
	}
 	
 	private void addPreferencesListener()
 	{
		preferences.addPreferenceChangeListener(new PreferenceChangeListener()
		{
	        public void preferenceChange(PreferenceChangeEvent e)
	        {
	            boolean show = Boolean.valueOf(e.getNewValue());
	            if (e.getKey().equals("imagestats.shownoise"))  // TODO: define "imagestats.xxxx" as constants somewhere
	        	{
	            	noiseEstimateLabel.setVisible(show);
	        	}
	        	else if (e.getKey().equals("imagestats.showblur"))
	        	{
	        		blurEstimateLabel.setVisible(show);
	            }
	        }
	    }); 	
	}
	
	public void setImage(BufferedImage image)
	{
		imagePanel.setImage(image);
	}
	
	public void setNoiseEstimate(float noise)
	{
		noiseEstimateLabel.setText(String.format("Noise: %.3f", noise));
	}
	
	public void setBlurEstimate(float blur)
	{
		blurEstimateLabel.setText(String.format("Blur: %.3f", blur));
	}
}