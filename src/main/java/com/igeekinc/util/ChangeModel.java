/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package com.igeekinc.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ChangeModel extends DeferredEventProcessor
{
	public ChangeModel(CheckCorrectDispatchThread checker)
	{
		super(checker);
	}
	
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	private VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);
    
	public void addEventListener(EventListener listener) 
	{
	    EventListener [] curListeners = otherEventListeners.getListeners((Class<EventListener>)listener.getClass());
	    for (EventListener checkListener:curListeners)
	    {
	        if(checkListener == listener)
	        {
	            System.out.println("Adding pre-existing listener");
	            return;
	        }
	    }
	    otherEventListeners.add((Class<EventListener>)listener.getClass(), listener);
	}
	public void removeEventListener(EventListener listener) 
	{
	    otherEventListeners.remove((Class<EventListener>)listener.getClass(), listener);
	}

	/**
	 * fireActionEvent - an example of how to add an event firing method
	 * @param fireEvent
	 */
	public void fireActionEvent(ActionEvent fireEvent)
	{
	    try
        {
            fireEventOnCorrectThread(fireEvent, ActionListener.class, ActionListener.class.getMethod("actionPerformed", new Class[]{ActionEvent.class}));
        } catch (SecurityException e)
        {
            throw new InternalError("Got security exception retrieving actionPerformed method from ActionListener class");
        } catch (NoSuchMethodException e)
        {
            throw new InternalError("ActionListener class missing actionPerformed method");
        }
	}
	
	public void addPropertyChangeListener(PropertyChangeListener newListener)
	{
		changeSupport.addPropertyChangeListener(newListener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener newListener)
	{
		changeSupport.addPropertyChangeListener(propertyName, newListener);
	}
    
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) 
    {
        vetoableChangeSupport.addVetoableChangeListener(propertyName, listener);
    }
	
    public void addVetoableChangeListener(VetoableChangeListener listener) 
    {
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }
    
	public void removePropertyChangeListener(PropertyChangeListener removeListener)
	{
		changeSupport.removePropertyChangeListener(removeListener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener removeListener)
	{
		changeSupport.removePropertyChangeListener(propertyName, removeListener);
	}
	
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) 
    {
        vetoableChangeSupport.removeVetoableChangeListener(propertyName, listener);
    }
    
    public void removeVetoableChangeListener(VetoableChangeListener listener) 
    {
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }
    
	protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
	{
		firePropertyChange(this, propertyName, new Boolean(oldValue), new Boolean(newValue));
	}

	protected void firePropertyChange(String propertyName, int oldValue, int newValue)
	{
		firePropertyChange(this, propertyName, new Integer(oldValue), new Integer(newValue));	
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		firePropertyChange(this, propertyName, oldValue, newValue);	
	}

	
    protected void firePropertyChange(Object source, String propertyName, boolean oldValue, boolean newValue)
    {
        firePropertyChange(source, propertyName, new Boolean(oldValue), new Boolean(newValue));
    }

    protected void firePropertyChange(Object source, String propertyName, int oldValue, int newValue)
    {
        firePropertyChange(source, propertyName, new Integer(oldValue), new Integer(newValue));   
    }
    
	protected void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue)
	{
		if (changeSupport.hasListeners(propertyName))
		{
		    PropertyChangeEvent eventToFire = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
		    fireEventOnCorrectThread(eventToFire);
		}
	}
	
	   protected void firePropertyChangeAsync(Object source, String propertyName, Object oldValue, Object newValue)
	    {
	        if (changeSupport.hasListeners(propertyName))
	        {
	            PropertyChangeEvent eventToFire = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
	            fireEventOnCorrectThreadAsync(eventToFire);
	        }
	    }
    protected void fireVetoablePropertyChange(String propertyName, boolean oldValue, boolean newValue)
    throws PropertyVetoException
    {
        fireVetoablePropertyChange(this, propertyName, new Boolean(oldValue), new Boolean(newValue));
    }

    protected void fireVetoablePropertyChange(String propertyName, int oldValue, int newValue)
    throws PropertyVetoException
    {
        fireVetoablePropertyChange(this, propertyName, new Integer(oldValue), new Integer(newValue)); 
    }

    
    protected void fireVetoablePropertyChange(Object source, String propertyName, boolean oldValue, boolean newValue)
    throws PropertyVetoException
    {
        fireVetoablePropertyChange(source, propertyName, new Boolean(oldValue), new Boolean(newValue));
    }

    protected void fireVetoablePropertyChange(Object source, String propertyName, int oldValue, int newValue)
    throws PropertyVetoException
    {
        fireVetoablePropertyChange(source, propertyName, new Integer(oldValue), new Integer(newValue)); 
    }
    
    protected void fireVetoablePropertyChange(Object source, String propertyName, Object oldValue, Object newValue)
    throws PropertyVetoException
    {
        if (vetoableChangeSupport.hasListeners(propertyName))
        {
            PropertyChangeEvent eventToFire = new PropertyChangeEvent(source, propertyName, oldValue, newValue);

            fireVetoableEventOnCorrectThread(eventToFire);
        }
    }
    
    /**
     * Don't call me! - call fireEventOnCorrectThread
     */
	protected void fireEvent(EventObject eventToFire)
	{
        PropertyChangeEvent propertyChangeEvent = (PropertyChangeEvent)eventToFire;
        changeSupport.firePropertyChange(propertyChangeEvent);
	}
    
	/**
	 * Don't call me!
	 */
    protected void fireVetoableEvent(EventObject eventToFire)
    throws PropertyVetoException
    {
        PropertyChangeEvent propertyChangeEvent = (PropertyChangeEvent)eventToFire;
        vetoableChangeSupport.fireVetoableChange(propertyChangeEvent); 
    }

	public void notifyFiringThread()
	{
		dispatcher.fireEventsOnDispatchThread(this);
	}
    public void addListDataListener(ListDataListener listener)
    {
        addEventListener(listener);
    }
    public void removeListDataListener(ListDataListener listener)
    {
        removeEventListener(listener);
    }
    public void fireListDataEvent(ListDataEvent fireEvent)
    {
        try
        {
            switch(fireEvent.getType())
            {
            case ListDataEvent.CONTENTS_CHANGED:
                fireEventOnCorrectThread(fireEvent, ListDataListener.class, ListDataListener.class.getMethod("contentsChanged", new Class[]{ListDataEvent.class}));
            break;
            case ListDataEvent.INTERVAL_ADDED:
                fireEventOnCorrectThread(fireEvent, ListDataListener.class, ListDataListener.class.getMethod("intervalAdded", new Class[]{ListDataEvent.class}));
                break;
            case ListDataEvent.INTERVAL_REMOVED:
                fireEventOnCorrectThread(fireEvent, ListDataListener.class, ListDataListener.class.getMethod("intervalRemoved", new Class[]{ListDataEvent.class}));
    
                break;
            }
        } catch (SecurityException e)
        {
            throw new InternalError("Got security exception retrieving actionPerformed method from ActionListener class");
        } catch (NoSuchMethodException e)
        {
            throw new InternalError("ActionListener class missing actionPerformed method");
        }
    }

}
