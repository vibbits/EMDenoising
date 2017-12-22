package be.vib.imagej;

public class TikhonovAlgorithm extends Algorithm
{
	private TikhonovParams params;
	private TikhonovParamsPanel panel;
	
	public TikhonovAlgorithm()
	{
		super(Name.TIKHONOV);
		params = new TikhonovParams();
		panel = new TikhonovParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Tikhonov";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new TikhonovParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new TikhonovDenoiser(new TikhonovParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
