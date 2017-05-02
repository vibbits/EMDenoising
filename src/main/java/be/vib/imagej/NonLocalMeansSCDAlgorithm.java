package be.vib.imagej;

public class NonLocalMeansSCDAlgorithm extends Algorithm
{
	private NonLocalMeansSCDParams params;
	private NonLocalMeansSCDParamsPanel panel;
	
	public NonLocalMeansSCDAlgorithm()
	{
		super(Name.NONLOCALMEANS);
		params = new NonLocalMeansSCDParams();
		panel = new NonLocalMeansSCDParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Non-Local Means";
    }
	
	@Override
	public Object getParams()
	{
	    return new NonLocalMeansSCDParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new NonLocalMeansSCDDenoiser(new NonLocalMeansSCDParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
