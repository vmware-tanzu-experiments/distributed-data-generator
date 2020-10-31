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

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Properties;


public class MonitoredProperties extends Properties
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8848738319344135580L;
	ChangeModel changeModel;
	/**
	 * 
	 */
	public MonitoredProperties(CheckCorrectDispatchThread dispatcher)
	{
		changeModel = new ChangeModel(dispatcher);
	}

	/**
	 * @param defaults
	 */
	public MonitoredProperties(Properties defaults, CheckCorrectDispatchThread dispatcher)
	{
		super(defaults);
		changeModel = new ChangeModel(dispatcher);
	}


	/* (non-Javadoc)
	 * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
	 */
	public synchronized Object setProperty(String key, String value)
	{
		Object oldValue = getProperty(key);
		Object returnObject = super.setProperty(key, value);
		changeModel.firePropertyChange(this, key, oldValue, value);
		return returnObject;
	}

	/* (non-Javadoc)
	 * @see java.util.Dictionary#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value)
	{
		Object oldValue;
		Object returnObject;
		synchronized(this)
		{
			oldValue = get(key);
			returnObject = super.put(key, value);
		}
		changeModel.firePropertyChangeAsync(this, (String)key, oldValue, value);
		return returnObject;
	}

	/* (non-Javadoc)
	 * @see java.util.Dictionary#remove(java.lang.Object)
	 */
	public synchronized Object remove(Object key)
	{
		Object oldValue = get(key);
		Object returnObject = super.remove(key);
		changeModel.firePropertyChange(this, (String)key, oldValue, null);
		return returnObject;
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener newListener)
	{
		changeModel.addPropertyChangeListener(propertyName, newListener);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener newListener)
	{
		changeModel.addPropertyChangeListener(newListener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener removeListener)
	{
		changeModel.removePropertyChangeListener(removeListener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener removeListener)
	{
		changeModel.removePropertyChangeListener(propertyName, removeListener);
	}
	
	public synchronized void replaceProperties(Properties newProperties)
	{
		// First remove any properties that don't exist in the new Properties
		Iterator<Object> oldKeyIterator = keySet().iterator();
		while(oldKeyIterator.hasNext())
		{	
			String curKey = (String)oldKeyIterator.next();
			if (!newProperties.containsKey(curKey))
			{	
				changeModel.firePropertyChange(this, curKey, getProperty(curKey), null);
				oldKeyIterator.remove();	// Use the iterator remove rather than calling thru our own
											// remove or the iterator will barf
			}
		}
		Iterator<Object> newKeyIterator = newProperties.keySet().iterator();
		while(newKeyIterator.hasNext())
		{	
			String curKey = (String)newKeyIterator.next();
			String newValue = newProperties.getProperty(curKey);
			setProperty(curKey, newValue);	// Let our own setProperty do the right thing here
		}
	}
    
    public String getDefaultForProperty(String propertyName)
    {
        if (defaults != null)
            return defaults.getProperty(propertyName);
        return null;
    }
}
