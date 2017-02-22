package be.vib.imagej;

public class DenoisePreviewCacheKey
{
	WizardModel.DenoisingAlgorithm algorithm;
	Object params;
	
	public DenoisePreviewCacheKey(WizardModel.DenoisingAlgorithm algorithm, Object params)
	{
		this.algorithm = algorithm;
		this.params = params;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		DenoisePreviewCacheKey other = (DenoisePreviewCacheKey)obj;
		
		return (obj instanceof DenoisePreviewCacheKey) && (algorithm == other.algorithm) && params.equals(other.params);
	}
	
	@Override
	public int hashCode()
	{
		return algorithm.hashCode() ^ params.hashCode();
	}
}