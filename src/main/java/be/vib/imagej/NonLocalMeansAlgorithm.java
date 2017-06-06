package be.vib.imagej;

public class NonLocalMeansAlgorithm extends Algorithm
{
	private NonLocalMeansParams params;
	private NonLocalMeansParamsPanel panel;
	
	public NonLocalMeansAlgorithm()
	{
		super(Name.NONLOCALMEANS);
		params = new NonLocalMeansParams();
		panel = new NonLocalMeansParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Non-Local Means";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return new NonLocalMeansParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new NonLocalMeansDenoiser(new NonLocalMeansParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
