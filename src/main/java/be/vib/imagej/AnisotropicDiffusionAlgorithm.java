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
	public Object getParams()
	{
	    return new AnisotropicDiffusionParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new AnisotropicDiffusionDenoiser(new AnisotropicDiffusionParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
