package be.vib.imagej;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class PreferencesDialog extends JDialog
{
	private Preferences prefs;
	
	public PreferencesDialog(JDialog owner, Preferences prefs)
	{
		super(owner, "DenoisEM Preferences", true);
		this.prefs = prefs;
		
		getContentPane().add(createUI());
		setMinimumSize(new Dimension(300, 150));
		pack();
		setLocationRelativeTo(owner);  // must be done after pack()
	}
	
	private JPanel createUI()
	{
		JPanel statsPanel = createImageStatisticsPanel();
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(e -> dispose());
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
				
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(okButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new BorderLayout(0, 10));					
		panel.add(statsPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.PAGE_END);
		return panel;
	}
		
	private JPanel createImageStatisticsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Image statistics"));
		
		JCheckBox noiseBox = new JCheckBox("Show noise estimate");
		JCheckBox blurBox = new JCheckBox("Show blur estimate");
		
		noiseBox.setSelected(prefs.getBoolean("imagestats.shownoise", false));
		blurBox.setSelected(prefs.getBoolean("imagestats.showblur", false));
		
		noiseBox.setToolTipText("Show an estimate of the noise in the original and the denoised images during parameter selection.");
		blurBox.setToolTipText("Show an estimate of the blur level of the original and the denoised images during parameter selection.");
		
		noiseBox.addActionListener(e -> { prefs.putBoolean("imagestats.shownoise", noiseBox.isSelected()); savePrefs(); });
		blurBox.addActionListener(e -> { prefs.putBoolean("imagestats.showblur", blurBox.isSelected()); savePrefs(); });

		panel.add(noiseBox);
		panel.add(blurBox);
		return panel;
	}
	
	private void savePrefs()
	{
		try
		{
			prefs.flush();
		}
		catch (Exception e)
		{
			// Silently ignore - no big deal if this fails.
		}
	}
}
