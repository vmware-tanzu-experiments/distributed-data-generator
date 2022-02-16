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

import com.igeekinc.util.logging.DebugLogMessage;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




class EventWrapper
{
    private EventObject event;
    private Throwable t;
    private boolean vetoable;
    private boolean fired;
    private Class<? extends EventListener> eventListenerClass;
    private Method dispatchMethod;
    EventWrapper(EventObject event, boolean vetoable, Class<? extends EventListener> eventListenerClass, Method dispatchMethod)
    {
        this.event = event;
        this.vetoable = vetoable;
        fired = false;
        this.eventListenerClass = eventListenerClass;
        this.dispatchMethod = dispatchMethod;
    }
    
    synchronized void waitForFinish()
    throws RuntimeException, PropertyVetoException
    {
        while (!fired)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught InterruptedException in waitForFinish"), e);
                return;
            }
        }
        if (t != null)
        {
            if (t instanceof RuntimeException)
                throw (RuntimeException)t;
            if (t instanceof PropertyVetoException)
                throw (PropertyVetoException)t;
            if (t instanceof Error)
                throw (Error)t;
        }
    }
    
    synchronized void finish(Throwable t)
    {
        this.t = t;
        fired = true;
        notifyAll();
    }
    
    EventObject getEvent()
    {
        return event;
    }

    boolean isVetoable()
    {
        return vetoable;
    }

    Class<? extends EventListener> getEventListenerClass()
    {
        return eventListenerClass;
    }

    Method getDispatchMethod()
    {
        return dispatchMethod;
    }
    
}

public abstract class DeferredEventProcessor
{
    ArrayList<EventWrapper> deferredEvents = new ArrayList<EventWrapper>();
    protected CheckCorrectDispatchThread dispatcher;
    protected EventListenerList otherEventListeners = new EventListenerList();
    protected Logger logger = LogManager.getLogger(getClass());
    
    public DeferredEventProcessor(CheckCorrectDispatchThread inChecker)
    {
        dispatcher = inChecker;
    }
    
    public void deferEvent(EventObject eventToDefer, boolean vetoable, boolean waitForEventProcessing)
    throws PropertyVetoException
    {
        EventWrapper wrapper = new EventWrapper(eventToDefer, vetoable, null, null);
        synchronized(deferredEvents)
        {
            deferredEvents.add(wrapper);
        }
        notifyFiringThread();
        if (waitForEventProcessing)
            wrapper.waitForFinish();
    }
    
    public void deferEvent(EventObject eventToDefer, Class<? extends EventListener> eventListenerClass, Method dispatchMethod, boolean waitForEventProcessing)
    throws PropertyVetoException
    {
        if (eventListenerClass == null || dispatchMethod == null)
            throw new IllegalArgumentException("eventListenerClass and dispatchMethod cannot be null");
        EventWrapper wrapper = new EventWrapper(eventToDefer, false, eventListenerClass, dispatchMethod);
        synchronized(deferredEvents)
        {
            deferredEvents.add(wrapper);
        }
        notifyFiringThread();
        if (waitForEventProcessing)
            wrapper.waitForFinish();
    }
    public void fireDeferredEvent()
    {
        EventWrapper wrapper;
        EventObject eventToFire;
        synchronized(deferredEvents)
        {
            if (deferredEvents.size() == 0)
                return;
            wrapper = (EventWrapper)deferredEvents.get(0);
            eventToFire = wrapper.getEvent();
            deferredEvents.remove(0);
        }
        try
        {
            if (wrapper.isVetoable())
                fireVetoableEvent(eventToFire);
            else
                if (wrapper.getEventListenerClass() != null)
                    fireEvent(eventToFire, wrapper.getEventListenerClass(), wrapper.getDispatchMethod());
                else
                    fireEvent(eventToFire);
            wrapper.finish(null);
        }
        catch (Throwable t)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught throwable sending event"), t);
            wrapper.finish(t);
        }
    }
    
    public void setDispatcher(CheckCorrectDispatchThread inDispatcher)
    {
        dispatcher = inDispatcher;
    }
    
    public void fireEventOnCorrectThreadNoErrors(EventObject eventToFire)
    {
        try
        {
            fireEventOnCorrectThread(eventToFire);
        }
        catch (Throwable t)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught throwable sending event"), t);
        }
    }
    public void fireEventOnCorrectThread(EventObject eventToFire)
    throws RuntimeException
    {
        if (dispatcher == null || dispatcher.isEventDispatchThread())
        {
            fireEvent(eventToFire);
        }
        else
        {
            try
            {
                deferEvent(eventToFire, false, true);
            } catch (PropertyVetoException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got property veto from regular property event"), e);
                throw new InternalError("Got property veto from regular property event");
            }
        }
    }
    
    public void fireEventOnCorrectThreadAsync(EventObject eventToFire)
    throws RuntimeException
    {
        if (dispatcher == null || dispatcher.isEventDispatchThread())
        {
            fireEvent(eventToFire);
        }
        else
        {
            try
            {
                deferEvent(eventToFire, false, false);
            } catch (PropertyVetoException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got property veto from regular property event"), e);
                throw new InternalError("Got property veto from regular property event");
            }
        }
    }
    public void fireEventOnCorrectThread(EventObject eventToFire, Class<? extends EventListener> eventListenerClass, Method dispatchMethod)
    throws RuntimeException
    {
        if (dispatcher == null || dispatcher.isEventDispatchThread())
        {
            fireEvent(eventToFire, eventListenerClass, dispatchMethod);
        }
        else
        {
            try
            {
                deferEvent(eventToFire, eventListenerClass, dispatchMethod, true);
            } catch (PropertyVetoException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got property veto from regular property event"), e);
                throw new InternalError("Got property veto from regular property event");
            }
        }
    }
    
    public void fireEventOnCorrectThreadAsync(EventObject eventToFire, Class<? extends EventListener> eventListenerClass, Method dispatchMethod)
    throws RuntimeException
    {
        if (dispatcher == null || dispatcher.isEventDispatchThread())
        {
            fireEvent(eventToFire, eventListenerClass, dispatchMethod);
        }
        else
        {
            try
            {
                deferEvent(eventToFire, eventListenerClass, dispatchMethod, false);
            } catch (PropertyVetoException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got property veto from regular property event"), e);
                throw new InternalError("Got property veto from regular property event");
            }
        }
    }
    public void fireVetoableEventOnCorrectThread(EventObject eventToFire)
    throws RuntimeException, PropertyVetoException
    {
        if (dispatcher == null || dispatcher.isEventDispatchThread())
        {
            fireVetoableEvent(eventToFire);
        }
        else
        {
            deferEvent(eventToFire, true, true);
        }
    }
    
    protected abstract void fireEvent(EventObject eventToFire);
    protected abstract void fireVetoableEvent(EventObject eventToFire) throws PropertyVetoException;
    public abstract void notifyFiringThread();

    protected void fireEvent(EventObject fireEvent, Class<? extends EventListener> eventListenerClass, Method dispatchMethod)
    {
         Object[] listeners = otherEventListeners.getListenerList();
         // loop through each listener and pass on the event if needed
         int numListeners = listeners.length;
         if (numListeners == 0)
         {
            logger.debug(new DebugLogMessage("No listeners for event {0}", new Serializable []{fireEvent}));
         }
         for (int i = 0; i<numListeners; i+=2) 
         {
              if (eventListenerClass.isAssignableFrom(((Class)listeners[i])))
              {
                  try
                  {
                      dispatchMethod.invoke(listeners[i+1], new Object[]{fireEvent});
                  } catch (IllegalArgumentException e)
                  {
                    LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
                  } catch (IllegalAccessException e)
                  {
                    LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
                  } catch (InvocationTargetException e)
                  {
                    LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
                  }
              }            
         }
    }
}
