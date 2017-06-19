package be.vib.imagej;

public class DenoisePreviewCacheKey
{
	private Algorithm.Name name;
	private Object params;
	
	public DenoisePreviewCacheKey(Algorithm algorithm)
	{
		this.name = algorithm.getName();
		this.params = algorithm.getParamsCopy();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		DenoisePreviewCacheKey other = (DenoisePreviewCacheKey)obj;
		
		return (obj instanceof DenoisePreviewCacheKey) && (name == other.name) && params.equals(other.params);
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode() ^ params.hashCode();
	}
}