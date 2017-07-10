package be.vib.imagej;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * 
 * A simple wizard consisting of a number of pages that can be traversed forward and backward in a linear fashion.
 * 
 * The wizard has no cancel button - user can close the dialog via the window's close button to cancel.
 *
 */
public class Wizard extends JDialog
                    implements ActionListener
{		
	private CardLayout cardLayout;
	
	private JPanel pagesPanel;  // holds all WizardPages in the Wizard
	private int currentPageIdx = 0;
		
	private JLabel crumbs;  // "bread crumbs" at the top, showing where the user is in the linear wizard
	
	private JButton backButton;
	private JButton nextButton;
	// No cancel or finish buttons for now
	
	private WizardModel model;

	public Wizard(String title, WizardModel model)
	{						
		this.model = model;

		buildUI(title);
		setWindowCloseHandler();
	}
	
	public WizardModel getModel()
	{
		return model;
	}
	
	public void build(WizardPage[] pages)
	{
		for (WizardPage page : pages)
		{
			this.pagesPanel.add(page);
		}
		goToFirstPage();
	}
	
	private void goToFirstPage()
	{
		assert(pagesPanel.getComponentCount() > 0);
		
		cardLayout.first(pagesPanel);
		currentPageIdx = 0;

		WizardPage firstPage = (WizardPage)pagesPanel.getComponent(currentPageIdx);
		firstPage.arriveFromPreviousPage();
		
		updateButtons();
		updateCrumbs();		
	}
	
	private void setWindowCloseHandler()
	{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent e)
			{ 
				setVisible(false);
				
				// Start with a clean slate next time we display the wizard.
				model.reset();
				
				goToFirstPage();
			}
		});		
	}
	
	private void buildUI(String title)
	{		
		JPanel buttonsPanel = new JPanel();
		
		pagesPanel = new JPanel();
		
		cardLayout = new CardLayout();
		pagesPanel.setLayout(cardLayout);
		
		backButton = new JButton("Back");
		nextButton = new JButton("Next");
		
		backButton.addActionListener(this);
		nextButton.addActionListener(this);
		
		buttonsPanel.setLayout(new BorderLayout());
		
		Box buttonsBox = new Box(BoxLayout.X_AXIS);
		buttonsBox.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		buttonsBox.add(backButton);
		buttonsBox.add(nextButton);
		buttonsBox.add(Box.createHorizontalStrut(10));
		
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
		
		if (source != backButton && source != nextButton)
			return;
								
		int newPageIdx = (source == backButton) ? currentPageIdx - 1 : currentPageIdx + 1;

		if (source == backButton)
		{
			getPage(currentPageIdx).goingToPreviousPage();
			getPage(newPageIdx).arriveFromNextPage();
			cardLayout.previous(pagesPanel);
		}
		else
		{
			getPage(currentPageIdx).goingToNextPage();
			getPage(newPageIdx).arriveFromPreviousPage();
			cardLayout.next(pagesPanel);
		}
		
		currentPageIdx = newPageIdx;

		updateButtons();
		updateCrumbs();
	}
	
	private WizardPage getPage(int idx)
	{
		return (WizardPage)pagesPanel.getComponent(idx);
	}
	
	/**
	 *  Enables or disables the buttons based on which panel is currently active
	 */
	public void updateButtons()
	{
		int numPages = pagesPanel.getComponentCount();
		if (numPages == 0)
			return;
		
		WizardPage currentPage = getPage(currentPageIdx);
		
		backButton.setEnabled((currentPageIdx > 0) && currentPage.canGoToPreviousPage());
		nextButton.setEnabled((currentPageIdx < numPages - 1) && currentPage.canGoToNextPage());		
	}
	
	/**
	 * Position the wizard horizontally in the center of the screen,
	 * and vertically somewhat above center.
	 */
	public void moveToMiddleOfScreen()
	{
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int wizardWidth = getSize().width;
        int wizardHeight = getSize().height;
        int x = (screen.width - wizardWidth) / 2; 
        int y = (screen.height - wizardHeight) / 3;
        setLocation(x, y);
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
			
			crumbs = crumbs + "<font color=" + color + " > " + page.getName() + "</font>";
			if (i < numPages - 1)
				crumbs = crumbs + "<font color=gray> > </font>";
		}
		
		crumbs = crumbs + "</html>";
		
		return crumbs;
	}
}
