package be.vib.imagej;

import java.util.LinkedHashMap;
import java.util.Map;

public class DenoisePreviewCache extends LinkedHashMap<DenoisePreviewCacheKey, DenoisePreviewCacheValue>
{
    private int capacity;
 
    public DenoisePreviewCache(final int capacity)
    {
        super(16, 0.75f, true);
        this.capacity = capacity;
    }
 
    @Override
    protected boolean removeEldestEntry(final Map.Entry<DenoisePreviewCacheKey, DenoisePreviewCacheValue> eldest)
    {
        return size() >= capacity;
    }
}