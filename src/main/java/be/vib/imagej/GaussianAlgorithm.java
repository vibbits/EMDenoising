package be.vib.imagej;

public class GaussianAlgorithm extends Algorithm
{
	private GaussianParams params;
	private GaussianParamsPanel panel;
	
	public GaussianAlgorithm()
	{
		super(Name.GAUSSIAN);
		params = new GaussianParams();
		panel = new GaussianParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Gaussian";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new GaussianParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new GaussianDenoiser(new GaussianParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }

}
