package be.vib.imagej;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;

@Plugin(type = Command.class, menuPath = "Plugins>EM Denoising>About")
public class AboutDialog implements Command
{
	@Override
	public void run() 
	{		
		boolean modal = true;  // do not return from the run() method before the user closes the modal dialog
		Frame parent = IJ.getInstance();		
		JDialog dialog = new JDialog(parent, "About EM Denoising", modal);
		dialog.add(new AboutPanel());
		dialog.pack();
		dialog.setLocationRelativeTo(null); // center the dialog on the screen
		dialog.setResizable(false);
		dialog.setVisible(true);
	}
	
	private class AboutPanel extends JPanel
	{
		public AboutPanel()
		{
			super();

			setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			// Beware aligment issues with JLabels with HTML text.
			// For example https://stackoverflow.com/questions/30655246/html-text-in-jlabel-ignores-alignment-with-text-align-center
			
			JLabel title = new JLabel("<html><div align='center'>" + 
			                          "<font size=+1>EM Denoising v0.0.1</font><br>" + 
			                          "</html>", JLabel.CENTER);
						
			JLabel subTitle = new JLabel("<html><div align='center'>" + 
                                         "A GPU Accelerated ImageJ Plugin for EM Image Denoising<br>" + 
                                         "</html>", JLabel.CENTER);
			
			JLabel copyright = new JLabel("<html><div align='center'>" + 
                                          "&copy; VIB 2017. All rights reserved." + 
                                          "</html>", JLabel.CENTER);
			
			JPanel logos = new LogosPanel();
			
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
			logos.setAlignmentX(Component.CENTER_ALIGNMENT);
			copyright.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			add(title);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(subTitle);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(copyright);
			add(Box.createRigidArea(new Dimension(0, 5)));
			add(logos);
		}
	}
	
	private class LogosPanel extends JPanel
	{
		public LogosPanel()
		{
			super();
			add(new IconLabel("images/bioimaging_core_rgb_pos_h75.png", "http://bio-imaging-core.be"));
			add(Box.createRigidArea(new Dimension(5, 0)));
			add(new IconLabel("images/QUASAR_logo_FINAL_S_crop_h75_transp.png", "http://gepura.io/quasar"));
			add(Box.createRigidArea(new Dimension(5, 0)));
			add(new IconLabel("images/IPI-logo_h75.png", "http://ipi.ugent.be"));	
		}
	}
	
	private class IconLabel extends JLabel
	{
		/** Constructs an image label. The image is loaded from the given \a path in the class's JAR file.
		 * When the mouse cursor hovers over the image it turns into a pointing finger
		 * suggesting it is possible to click the image. Clicking then opens the associated \a url in the browser. 
		 */
		public IconLabel(String path, String url)
		{
			super(createImageIcon(path));
			
			setToolTipText(url);
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					try
					{
						Desktop.getDesktop().browse(new URI(url));
					}
					catch (URISyntaxException | IOException ex)
					{
						ex.printStackTrace();
					}
				}
			});			
		}
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path)
	{
	    java.net.URL imgURL = AboutDialog.class.getResource(path);
	    if (imgURL != null)
	    {
	        return new ImageIcon(imgURL);
	    }
	    else
	    {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
}
