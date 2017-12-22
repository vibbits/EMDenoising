package be.vib.imagej;

public class TotalVariationAlgorithm extends Algorithm
{
	private TotalVariationParams params;
	private TotalVariationParamsPanel panel;
	
	public TotalVariationAlgorithm()
	{
		super(Name.TOTAL_VARIATION);
		params = new TotalVariationParams();
		panel = new TotalVariationParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Total Variation";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new TotalVariationParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new TotalVariationDenoiser(new TotalVariationParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
