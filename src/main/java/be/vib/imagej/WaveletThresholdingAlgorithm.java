package be.vib.imagej;

public class WaveletThresholdingAlgorithm extends Algorithm
{
	private WaveletThresholdingParams params;
	private WaveletThresholdingParamsPanel panel;
	
	public WaveletThresholdingAlgorithm()
	{
		super(Name.WAVELET_THRESHOLDING);
		params = new WaveletThresholdingParams();
		panel = new WaveletThresholdingParamsPanel(params);
	}
    
	@Override
    public String getReadableName()
    {
    	return "Wavelet Thresholding";
    }
	
	@Override
	public Object getParams()
	{
	    return new WaveletThresholdingParams(params);
	}
	
	@Override
	public Denoiser getDenoiser()
	{
		return new WaveletThresholdingDenoiser(new WaveletThresholdingParams(params));
	}

	@Override
    public DenoiseParamsPanelBase getPanel()
    {
		return panel;
    }
}
