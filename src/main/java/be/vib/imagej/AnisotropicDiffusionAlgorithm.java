package be.vib.imagej;

public class AnisotropicDiffusionAlgorithm extends Algorithm
{
	private AnisotropicDiffusionParams params;
	private AnisotropicDiffusionParamsPanel panel;
	
	public AnisotropicDiffusionAlgorithm()
	{
		super(Name.ANISOTROPIC_DIFFUSION);
		params = new AnisotropicDiffusionParams();
		panel = new AnisotropicDiffusionParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Anisotropic Diffusion";
    }
	
	@Override
	public DenoiseParams getParams()
	{
	    return params;
	}
	
	@Override
	public DenoiseParams getParamsCopy()
	{
	    return new AnisotropicDiffusionParams(params);
	}
	
	@Override
	public Denoiser getDenoiserCopy()
	{
		return new AnisotropicDiffusionDenoiser(new AnisotropicDiffusionParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
