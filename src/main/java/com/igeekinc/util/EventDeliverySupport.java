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

import com.igeekinc.util.exceptions.DeadListenerError;
import com.igeekinc.util.logging.DebugLogMessage;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Hashtable;
import org.apache.logging.log4j.LogManager;



public class EventDeliverySupport extends DeferredEventProcessor
{
    Hashtable<Class<? extends EventObject>, ArrayList<EventHandler>> eventHandlers;
    public EventDeliverySupport(CheckCorrectDispatchThread checker)
    {
        super(checker);
        eventHandlers = new Hashtable<Class<? extends EventObject>, ArrayList<EventHandler>>();
    }
    public void addEventHandler(Class<? extends EventObject> eventType, EventHandler eventHandler)
    {
        if (eventType != null)
        {
            ArrayList<EventHandler> handlers = eventHandlers.get(eventType);
            boolean newList = false;
            if (handlers == null)
            {
                handlers = new ArrayList<EventHandler>();
                newList = true;
            }
            handlers.add(eventHandler);
            if (newList)
                eventHandlers.put(eventType, handlers);
        }
    }
    
    public void removeEventHandler(Class<? extends EventObject> eventType, EventHandler eventHandler)
    {
        if (eventType != null)
        {
            ArrayList<EventHandler> handlers = eventHandlers.get(eventType);
            if (handlers != null)
                handlers.remove(eventHandler);
        }
    }
    
    @Override
    public void fireEvent(EventObject eventToSend)
    {
        ArrayList<EventHandler> handlers = eventHandlers.get(eventToSend.getClass());
        if (handlers != null)
        {
            EventHandler [] curHandlers = new EventHandler[handlers.size()];
            curHandlers = (EventHandler [])handlers.toArray(curHandlers);
            for (int curHandlerNum = 0; curHandlerNum < curHandlers.length; curHandlerNum++)
            {
                try
                {
                    curHandlers[curHandlerNum].handleEvent(eventToSend);
                } catch (DeadListenerError e)
                {
                    LogManager.getLogger(getClass()).debug(new DebugLogMessage("Listener is dead, removing"));
                    removeEventHandler(eventToSend.getClass(), curHandlers[curHandlerNum]);
                }
            }
        }
    }
    public void sendEvent(EventObject eventToFire)
    {
        fireEventOnCorrectThread(eventToFire);
        
    }
    
    /**
     * Catches and disposes of all throwables
     * @param eventToFire
     */
    public void sendEventNoError(EventObject eventToFire)
    {
        fireEventOnCorrectThreadNoErrors(eventToFire);
    }
    
    @Override
    public void fireVetoableEvent(EventObject eventToFire) throws PropertyVetoException
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void notifyFiringThread()
    {
        dispatcher.fireEventsOnDispatchThread(this);
    }
}
