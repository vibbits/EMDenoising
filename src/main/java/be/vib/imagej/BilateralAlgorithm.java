package be.vib.imagej;

public class BilateralAlgorithm extends Algorithm
{
	private BilateralParams params;
	private BilateralParamsPanel panel;
	
	public BilateralAlgorithm()
	{
		super(Name.BILATERAL);
		params = new BilateralParams();
		panel = new BilateralParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Bilateral";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new BilateralParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new BilateralDenoiser(new BilateralParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
