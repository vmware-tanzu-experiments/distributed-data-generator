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
 
package com.igeekinc.util.objectcache;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * ObjectCacheClearHandler p0olls the reference queues for all of the
 * ObjectCaches that have been instantiated, looking for references that
 * have been garbage collected and removing them from their respective caches.
 * 
 * Under normal circumstances there should only be one ObjectCacheClearHandler instantiated.
 *
 */
public class ObjectCacheClearHandler implements Runnable
{
    private Thread queueClearThread;
    private ArrayList<WeakReference<ObjectCache<?, ?>>> caches;
    private static ObjectCacheClearHandler handler = new ObjectCacheClearHandler();
    private Logger logger;
    
    private ObjectCacheClearHandler()
    {
        caches = new ArrayList<WeakReference<ObjectCache<?, ?>>>();
        logger = LogManager.getLogger(getClass());
        queueClearThread = new Thread(this, "ObjectCache clear thread");
        queueClearThread.setDaemon(true);
        queueClearThread.start();
    }

    public static ObjectCacheClearHandler getObjectCacheClearHandler()
    {
        return handler;
    }
    
    public synchronized void addObjectCache(ObjectCache<?, ?> cacheToAdd)
    {
        caches.add(new WeakReference<ObjectCache<?, ?>>(cacheToAdd));
    }
    
    public synchronized void removeObjectCache(ObjectCache<?, ?> cacheToRemove)
    {
    	Iterator<WeakReference<ObjectCache<?, ?>>> cacheRefIterator = caches.iterator();
    	while(cacheRefIterator.hasNext())
    	{
    	    WeakReference<ObjectCache<?, ?>> curRef = cacheRefIterator.next();
    		if (curRef.get() == cacheToRemove)
    			cacheRefIterator.remove();
    	}
    }
    
    @Override
    public void run()
    {
        boolean shouldSleep = false;
        while (true)
        {
            try
            {
                if (shouldSleep)
                    Thread.sleep(500);
                Iterator<WeakReference<ObjectCache<?, ?>>> cacheIterator;
                int outstanding = 0;
                synchronized (this)
                {
                    cacheIterator = caches.iterator();
                    while(cacheIterator.hasNext())
                    {
                        WeakReference<ObjectCache<?, ?>> curRef = cacheIterator.next();
                        ObjectCache<?, ?> checkCache = curRef.get();

                        if (checkCache == null)
                        {
                        	cacheIterator.remove();
                        	continue;
                        }
                        synchronized(checkCache)
                        {
                            WeakReference<?> removeRef;

                            while ((removeRef = (WeakReference<?>)checkCache.refQueue.poll()) != null)
                            {
                                Object key = checkCache.reverseMap.get(removeRef);
                                if (key != null)
                                {
                                    WeakReference<?> whackRef = checkCache.cacheObjs.get(key);
                                    // There's a race between the refQueue and the object being re-fetched
                                    // Make sure that we're really removing the dead reference
                                    if (whackRef == removeRef)
                                        checkCache.cacheObjs.remove(key);
                                    checkCache.reverseMap.remove(removeRef);
                                }
                            }
                            outstanding += checkCache.dirtyMap.size();
                        }

                        // Release the lock before flushing.  Otherwise there are some circular dependencies
                        checkCache.flush();
                    }
                }
                shouldSleep = (outstanding < ObjectCache.getMaxDirtySize());   // If people will be waiting to write, run the flush loop continuously
            } catch (Throwable t)
            {
                logger.error("Caught exception in ObjectCache cleanup loop", t);
            }
        }
    }

}
