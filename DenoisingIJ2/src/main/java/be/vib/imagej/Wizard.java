package be.vib.imagej;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * 
 * A simple wizard consisting of a number of pages that can be traversed forward and backward in a linear fashion.
 * 
 * The wizard has no cancel button - user needs to close the dialog via the window's close button to cancel
 * (probably we better pop up a confirmation dialog first).
 *
 */
public class Wizard extends JDialog
                    implements ActionListener 
{		
	private CardLayout cardLayout;
	
	private JPanel pagesPanel;  // holds all WizardPages in the Wizard
	private int currentPageIdx;
		
	private JLabel crumbs;  // "bread crumbs" at the top, showing where the user is in the linear wizard
	
	private JButton backButton;
	private JButton nextButton;
	private JButton finishButton;
	// No cancel button
	
	public Wizard(String title)
	{				
		currentPageIdx = 0;
		
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowOpened(WindowEvent e)
			{
				System.out.println("Wizard window opened - about to init quasar host (Java thread=" + Thread.currentThread().getId() + ")");
				boolean success = QuasarInterface.quasarInit("cuda");
				assert(success);
				
				// FIXME: support loading from JAR or so
				boolean sourceLoaded = QuasarInterface.quasarLoadSource("E:\\git\\DenoisingIJ2Repository\\DenoisingIJ2\\src\\main\\resources\\quasar\\nlmeans_denoising_stillimages.q");
				assert(sourceLoaded);
				
				System.out.println("Wizard window opened - quasar host initialized");
			}

			@Override
			public void windowClosing(WindowEvent e) // TODO: or windowClosed ?
			{
				System.out.println("Wizard window closing - about to release quasar host (Java thread=" + Thread.currentThread().getId() + ")");
				QuasarInterface.quasarRelease();
				System.out.println("Wizard window closing - quasar host released");
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				System.out.println("Wizard window closed (Java thread=" + Thread.currentThread().getId() + ")");
			}
		});
		
		buildUI(title);
	}
	
	public void addPage(WizardPage page)
	{
		pagesPanel.add(page);

		updateButtons();
		updateCrumbs();
		
		pack();
	}
	
	private void buildUI(String title)
	{		
		JPanel buttonsPanel = new JPanel();
		
		pagesPanel = new JPanel();
		
		cardLayout = new CardLayout();
		pagesPanel.setLayout(cardLayout);
		
		backButton = new JButton("Back");
		nextButton = new JButton("Next");
		finishButton = new JButton("Finish");
		
		backButton.addActionListener(this);
		nextButton.addActionListener(this);
		finishButton.addActionListener(this);
		
		buttonsPanel.setLayout(new BorderLayout());
		
		Box buttonsBox = new Box(BoxLayout.X_AXIS);
		buttonsBox.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		buttonsBox.add(backButton);
		buttonsBox.add(nextButton);
		buttonsBox.add(Box.createHorizontalStrut(10));

		buttonsBox.add(finishButton);
		
		buttonsPanel.add(buttonsBox, BorderLayout.EAST);
		
		crumbs = new JLabel();
		crumbs.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		crumbs.setHorizontalAlignment(JLabel.CENTER);
		
		setMinimumSize(new Dimension(160, 80));
		setTitle(title);

		getContentPane().add(crumbs, BorderLayout.NORTH);
		getContentPane().add(pagesPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source != backButton && source != nextButton && source != finishButton)
			return;
						
		if (source == backButton)
		{
			int newPageIdx = currentPageIdx - 1;

			WizardPage newPage = (WizardPage)pagesPanel.getComponent(newPageIdx);
			newPage.aboutToShowPanel();

			cardLayout.previous(pagesPanel);
			currentPageIdx = newPageIdx;
		}
		else if (source == nextButton)
		{
			int newPageIdx = currentPageIdx + 1;

			WizardPage newPage = (WizardPage)pagesPanel.getComponent(newPageIdx);
			newPage.aboutToShowPanel();

			cardLayout.next(pagesPanel);
			currentPageIdx = newPageIdx;
		}
		else if (source == finishButton)
		{
			// Be careful here. We could also call dispose() but then we need to check the WindowListener code
			// that uses those events to clean up the Quasar host. And hiding the window sends a different event
			// then dispose()ing it.
			
			// FIXME!! Need to unify the events so quasar get released consistently in all cases
			// setVisible(false) : no WindowClosed or WindowClosing event?
			// dispose() : WindowClosed event
			// pressing the x in the dialog's title bar : WindowClosing event
			
			return;
		}

		updateButtons();
		updateCrumbs();
	}
	
	/**
	 *  Enables or disables the buttons based on which panel is currently active
	 */
	private void updateButtons()
	{
		final int numPages = pagesPanel.getComponentCount();
		
		backButton.setEnabled(currentPageIdx > 0);
		nextButton.setEnabled(currentPageIdx < numPages - 1);
		finishButton.setEnabled(false); // SHOULD BE: finishButton(currentPageIdx == numPages - 1); once quasar release is done consistently (currently only works if window is closed via the close button...)
	}
	
	private void updateCrumbs()
	{
		crumbs.setText(crumbsHTML(currentPageIdx));
	}

	/**
	 * Builds an HTML string that represents the trail of bread crumbs when traveling
	 * through the wizard pages from beginning to end. The crumb for one wizard
	 * page is highlighted (suggesting it is the current page), the crumbs for all
	 * other pages are dimmed.
	 * 
	 * @param idxToHighlight The index of the wizard page that needs to be highlighted
	 *        in the bread crumbs trail.
	 * @return An HTML string representing the bread crumbs trail. It uses HTML color 
	 *         tags for dimming/highlighting the crumbs.
	 */
	private String crumbsHTML(int idxToHighlight)
	{
		String crumbs = "<html>";
		
		final int numPages = pagesPanel.getComponentCount();
		
		for (int i = 0; i <numPages; i++)
		{
			WizardPage page = (WizardPage)pagesPanel.getComponent(i);
			String color = (i == idxToHighlight) ? "black" : "gray";
			
			crumbs = crumbs + "<font color='" + color + "'>" + page.getName() + "</font>";
			if (i < numPages - 1)
				crumbs = crumbs + "<font color='gray'> > </font>";
		}
		
		crumbs = crumbs + "</html>";
		
		return crumbs;
	}
}
