package be.vib.imagej;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import be.vib.bits.QHost;
import be.vib.bits.QExecutor;

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
	
	public Wizard(String title) //throws InterruptedException, ExecutionException
	{				
		currentPageIdx = 0;
		
		System.out.println("Wizard constructor (Java thread=" + Thread.currentThread().getId() + ")");


		addWindowListener(new WindowAdapter(){
//			@Override
//			public void windowOpened(WindowEvent e)
//			{
//				System.out.println("Wizard window opened - about to init quasar host (Java thread=" + Thread.currentThread().getId() + ")");
//
//				try {
//						QExecutor.getInstance().submit(() -> {
//						boolean loadCompiler = true;
//						QHost.init("cuda", loadCompiler);
//						System.out.println("QHost.init done");
//					}).get();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (ExecutionException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//				System.out.println("Wizard window opened - quasar host initialized");
//			}

			@Override
			public void windowClosing(WindowEvent e) // TODO: or windowClosed ?
			{
				System.out.println("Wizard window closing - about to release quasar host (Java thread=" + Thread.currentThread().getId() + ")");

					try {
						QExecutor.getInstance().submit(() -> {
							QHost.release();
							System.out.println("Wizard window closing - quasar host released");
						}).get();
					} catch (InterruptedException | ExecutionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				System.out.println("Wizard window closed (Java thread=" + Thread.currentThread().getId() + ")");
			}
		});
		
		buildUI(title);
	}
	
	// FIXME? call aboutToShowPanel() when first panel is shown initially (without the back/next buttons being pressed)

	public void addPage(WizardPage page)
	{
		pagesPanel.add(page);

		updateButtons();
		updateCrumbs();
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
	public void updateButtons()
	{
		final int numPages = pagesPanel.getComponentCount();
		if (numPages == 0)
			return;
		
		final WizardPage currentPage = (WizardPage)pagesPanel.getComponent(currentPageIdx);
		
		backButton.setEnabled((currentPageIdx > 0) && currentPage.canGoToPreviousPage());
		nextButton.setEnabled((currentPageIdx < numPages - 1) && currentPage.canGoToNextPage());		
		finishButton.setEnabled((currentPageIdx == numPages - 1) && currentPage.canFinish());
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
