package be.vib.imagej;

public class DenoisePreviewCacheKey
{
	Algorithm.Name name;
	Object params;
	
	public DenoisePreviewCacheKey(Algorithm.Name name, Object params)
	{
		this.name = name;
		this.params = params;
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