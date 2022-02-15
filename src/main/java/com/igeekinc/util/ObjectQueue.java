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
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;




public class ObjectQueue<T>
{
    //private ArrayList buffers;
    private Object [] objects;
    private int maxObjects;
    private boolean buffersDropped;
    protected boolean closed;

    private int addPos, retrievePos, buffersQueued;
    public ObjectQueue(int maxBuffers)
    {
        if (maxBuffers < 1)
            throw new IllegalArgumentException("Buffer queue must accept at least one buffer");
        objects = new Object[maxBuffers];
        this.maxObjects = maxBuffers;
        closed = false;
        addPos = 0;
        retrievePos = 0;
        buffersQueued = 0;
    }
    

    
    public void addObject(T objectToAdd)
    {
        synchronized(objects)
        {
            if (buffersQueued >= (maxObjects * .9))
            {
                objects.notifyAll();
                try
                {
                    objects.wait(100);   // Give everyone else a chance to catch up?
                }
                catch (InterruptedException e)
                {
                    
                }
                if (buffersQueued >= maxObjects)
                {
                    buffersDropped = true;  // Drop this block
                    objects.notifyAll();
                    LogManager.getLogger(getClass()).error(new ErrorLogMessage("Object queue dropping objects.  buffersQueued = {0}, maxObjects = {1}",
                            new Serializable[]{(Integer)buffersQueued, (Integer)maxObjects}));
                    return;
                }
            }

            objects[addPos] = objectToAdd;
            addPos = (addPos + 1) % maxObjects;
            buffersQueued++;
            objects.notifyAll();

        }
    }
    
    @SuppressWarnings("unchecked")
    public T getObject() throws InterruptedException, BuffersDroppedException
    {
        synchronized(objects)
        {
            while(!closed && buffersQueued == 0)
                objects.wait();
            if (closed && buffersQueued == 0)
                return null;
            if (buffersDropped)
            {
                buffersDropped = false; // We've notified at least one client now or we will
                throw new BuffersDroppedException();
            }
            T returnObject = (T) objects[retrievePos];
            objects[retrievePos] = null;
            retrievePos = (retrievePos + 1) % maxObjects;
            buffersQueued --;
            return returnObject;
        }
    }
    

    
    public void close()
    {
        synchronized(objects)
        {
            closed = true;
            objects.notifyAll();
        }
    }

    /**
     * Returns true when the queue has been drained and closed
     * @return
     */
    public boolean isClosed()
    {
        return (buffersQueued == 0 && closed);
    }
    
    public boolean containsObject(Object checkObject)
    {
        if (checkObject != null)
        {
            synchronized(objects)
            {
                for (int checkOffset = 0; checkOffset < buffersQueued; checkOffset ++)
                {
                    int checkPos = (retrievePos + checkOffset) % maxObjects;
                    if (objects[checkPos] != null)
                    {
                        if (checkObject.equals(objects[checkPos]))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
