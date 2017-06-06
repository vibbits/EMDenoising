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
	    return new BilateralParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new BilateralDenoiser(new BilateralParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
