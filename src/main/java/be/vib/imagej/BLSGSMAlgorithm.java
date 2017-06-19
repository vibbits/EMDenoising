package be.vib.imagej;

public class BLSGSMAlgorithm extends Algorithm
{
	private BLSGSMParams params;
	private BLSGSMParamsPanel panel;
	
	public BLSGSMAlgorithm()
	{
		super(Name.BLSGSM);
		params = new BLSGSMParams();
		panel = new BLSGSMParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "BLS-GSM";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new BLSGSMParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new BLSGSMDenoiser(new BLSGSMParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
