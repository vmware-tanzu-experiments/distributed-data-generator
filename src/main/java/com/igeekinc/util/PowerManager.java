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

import com.igeekinc.util.logging.ErrorLogMessage;
import com.igeekinc.util.powermanagement.PowerManagementEvent;
import com.igeekinc.util.powermanagement.PowerManagementListener;
import java.util.Date;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;



public abstract class PowerManager
{
	protected Vector listeners;
	/**
	 * 
	 */
	public PowerManager()
	{
		listeners = new Vector();
	}
	public synchronized void addPowerManagementListener(PowerManagementListener newListener)
	{
		listeners.add(newListener);
	}
	
	public synchronized void removePowerManagementListener(PowerManagementListener removeListener)
	{
		listeners.remove(removeListener);
	}
	
	protected boolean firePowerManagementEvent(PowerManagementEvent evt)
	{
		java.util.Vector targets = null;
		synchronized (this) 
		{
			if (listeners != null && listeners.size() > 0) 
			{
				targets = (java.util.Vector) listeners.clone();
			}
	
		}
		boolean allowChange = true;
		if (targets != null) 
		{
			for (int i = 0; i < targets.size(); i++) 
			{
				PowerManagementListener target = (PowerManagementListener)targets.elementAt(i);
				try {
					if (!target.powerManagementChange(evt))
						allowChange = false;
				} catch (Throwable e) {
					LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught error sending power management event"), e);
				}
			}
		}
		return allowChange;
	}

	public abstract void wakeUpAt(Date wakeupTime, boolean wakeFromSleepOnly);
	
	public abstract void cancelWakeup();
	
	public abstract void shutdownAt(Date shutdownTime);
	
	public abstract void cancelShutdown();
	
	public abstract void sleepNow();
	
	public abstract void shutdownNow();
	
}
