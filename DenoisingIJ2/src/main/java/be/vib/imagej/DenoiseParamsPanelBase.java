package be.vib.imagej;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

public abstract class DenoiseParamsPanelBase extends JPanel
{
	  private ArrayList<DenoiseParamsChangeEventListener> listeners = new ArrayList<DenoiseParamsChangeEventListener>();
	  
	  public synchronized void addEventListener(DenoiseParamsChangeEventListener listener)
	  {
	    listeners.add(listener);
	  }
	  
	  public synchronized void removeEventListener(DenoiseParamsChangeEventListener listener)
	  {
	    listeners.remove(listener);
	  }
	 
	  protected synchronized void fireChangeEvent()
	  {
		DenoiseParamsChangeEvent event = new DenoiseParamsChangeEvent(this);
	    
	    Iterator<DenoiseParamsChangeEventListener> i = listeners.iterator();
	    while(i.hasNext())
	    {
	      ((DenoiseParamsChangeEventListener)i.next()).handleDenoiseParameterChangeEvent(event);
	    }
	  }
	}