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
	public Object getParams()
	{
	    return new BLSGSMParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new BLSGSMDenoiser(new BLSGSMParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
