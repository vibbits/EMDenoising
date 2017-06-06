package be.vib.imagej;

import java.util.Properties;

public abstract class DenoiseParams
{
	protected static final String PREFIX = "be.vib.emdenoising.";
	
    public abstract Properties getParameterList();
}
