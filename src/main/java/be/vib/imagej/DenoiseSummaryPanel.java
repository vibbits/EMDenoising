package be.vib.imagej;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

class DenoiseSummaryPanel extends JPanel
{
	private WizardModel model;
	
	private JLabel denoisingAlgorithm;
	private JLabel inputImage;
	private JLabel denoisedImage;
	
	public DenoiseSummaryPanel(WizardModel model)
	{
		this.model = model;
		this.inputImage = new JLabel();
		this.denoisedImage = new JLabel();
		this.denoisingAlgorithm = new JLabel();
		
		buildUI();
	}

	private void buildUI()
	{
		setBorder(BorderFactory.createTitledBorder("Summary"));
		
		JLabel inputImageLabel = new JLabel("Original image:"); // TODO: add number of slices and current slice? If so, we need to listen to current slice changes. Also to image changes if the user were to delete slices from the image while our window is open...
		JLabel denoisedImageLabel = new JLabel("Denoised image:");
		JLabel denoisingAlgorithmLabel = new JLabel("Denoising algorithm:");
		
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
			           .addComponent(inputImageLabel)
			           .addComponent(denoisedImageLabel)
		      		   .addComponent(denoisingAlgorithmLabel))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
			           .addComponent(inputImage)
			           .addComponent(denoisedImage)
		      		   .addComponent(denoisingAlgorithm))
		);
		
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    		   .addComponent(inputImageLabel)
		    		   .addComponent(inputImage))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    		   .addComponent(denoisedImageLabel)
		    		   .addComponent(denoisedImage))
 		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		    		   .addComponent(denoisingAlgorithmLabel)
		    		   .addComponent(denoisingAlgorithm))
		);		
		
		setLayout(layout);
	}
	
	public void updateText()
	{
		denoisingAlgorithm.setText(html(italic(model.getAlgorithm().getReadableName() + "; " + model.getAlgorithm().getParams())));
		inputImage.setText(html(italic(model.getImage().getTitle())));
		denoisedImage.setText(html(italic("New image, original image will not be modified.")));
	}
	
	private static String html(String text)
	{
		return "<html>" + text + "</html>";  
	}
	
	private static String italic(String text)
	{
		return "<i>" + text + "</i>";  
	}
	
//		private static String color(String text, String color)
//		{
//			return "<font color=" + color + ">" + text + "</html>";  
//		}
}