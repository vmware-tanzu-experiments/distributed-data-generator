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

import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ObjectCache<K, V extends CachableObjectIF<K>>
{
    private static final int kCacheDangerSize = 1000000;
    /* cacheObjs keeps a weak reference to all of the objects that the cache
     * is currently managing.  It enables the cache to ensure that there is only one
     * copy of an object in memory at any time.  It maps from key to weak ref
     */
    protected HashMap<ObjectKey<K>, WeakReference<V>> cacheObjs;
    /*
     * reverseMap is used to lookup objects by their weak reference and is primarily
     * used to process notifications that objects have been garbage collected.
     */
    protected HashMap<WeakReference<V>, ObjectKey<K>> reverseMap;
    /*
     * dirtyMap hangs on to objects which have been modified but not written to stable
     * storage.  The CachableObject setDirty() functions calls into our notifyDirty function
     * to inform the cache of the change.  The dirtyMap contains strong references to the 
     * dirty objects.
     */
    protected HashMap<K, V> dirtyMap;
    /*
     * lruQueue holds on to the most recently used items
     */
    protected LRUQueue<ObjectKey<K>, V> lruQueue;
    protected ReferenceQueue<V> refQueue;
    protected Logger logger;
    protected CacheMissHandler<K, V> missHandler;
    protected long hits, misses;
    protected boolean running;
    protected int lruSize;
    protected static int maxDirtySize = 10000;
    protected static int maxDirtyTime = 250;   // Time elapsed since object was dirtied
    /**
     * 
     */
    public ObjectCache(CacheMissHandler<K, V> inMissHandler, int lruSize)
    {
        this.lruSize = lruSize;
        cacheObjs = new HashMap<ObjectKey<K>, WeakReference<V>>();
        reverseMap = new HashMap<WeakReference<V>, ObjectKey<K>>();
        dirtyMap = new HashMap<K, V>();
        lruQueue = new LRUQueue<ObjectKey<K>, V>(lruSize);
        refQueue = new ReferenceQueue<V>();
        logger = LogManager.getLogger(getClass());
        missHandler = inMissHandler;
        hits = misses = 0;
        ObjectCacheClearHandler.getObjectCacheClearHandler().addObjectCache(this);
    }
    
    protected synchronized void put(ObjectKey<K> key, V objectToCache)
    {
        verifyCache();
        WeakReference<V> cacheRef = new WeakReference<V>(objectToCache, refQueue);
        WeakReference<V> oldCacheRef = cacheObjs.put(key, cacheRef);
        if (oldCacheRef != null)
            reverseMap.remove(oldCacheRef);
        reverseMap.put(cacheRef, key);
        lruQueue.put(key, objectToCache);
        verifyCache();
    }

    protected synchronized V internalGet(ObjectKey<K> key)
    {
    	return internalGet(key, false);
    }
    protected synchronized V internalGet(ObjectKey<K> key, boolean cacheOnly)
    {
        WeakReference<V> cacheRef = cacheObjs.get(key);
        boolean addedToCache = false;
        V returnObject = null;
        if (cacheRef != null)
        {
            returnObject = cacheRef.get();
        }
        if (returnObject == null)
        {
            if (cacheRef != null)
            {
                // Cacheref is dead - remove it
                cacheObjs.remove(key);
                reverseMap.remove(cacheRef);
            }
        	if (!cacheOnly)
        	{
        		returnObject = handleMiss(key);
        		if (returnObject != null)
        		{
        			put(key, returnObject);
        			addedToCache = true;
        		}
        	}
            misses++;
        }
        else
            hits++;
        if (returnObject != null && !addedToCache)
        {
            lruQueue.put(key, returnObject);
            WeakReference<V> newCacheRef = new WeakReference<V>(returnObject, refQueue);
            if (cacheRef != null)
                reverseMap.remove(cacheRef);    // Clean out the old ref
            cacheObjs.put(key, newCacheRef);
            reverseMap.put(newCacheRef, key);
        }
        verifyCache();
        return(returnObject);
    }


    @SuppressWarnings("unchecked")
    protected synchronized HashMap<K, V> internalBulkGet(ObjectKey<K>[] keys)
    {
        verifyCache();
        HashMap<K, V> returnObjects = new HashMap<K, V>();
        if (keys.length > 0)
        {
            K [] missedKeys = (K[])Array.newInstance(keys[0].getChildKey().getClass(), keys.length);
            int numMissedKeys = 0;
            // First, grab everything in the cache and build the list of keys to fetch
            for (int curKeyNum = 0; curKeyNum < keys.length; curKeyNum++)
            {
                WeakReference<V> cacheRef = cacheObjs.get(keys[curKeyNum]);
                boolean cachedFound = false;
                if (cacheRef != null)
                {
                    V cachedObj = cacheRef.get();
                    if (cachedObj != null)
                    {
                        returnObjects.put(keys[curKeyNum].getChildKey(), cachedObj);
                        missedKeys[curKeyNum] = null;
                        cachedFound = true;
                    }
                    /*
                 // Debugging stmt
                else
                    System.out.println("Got back a cache ref that's null");
                     */
                }

                if (!cachedFound)
                {
                    missedKeys[curKeyNum] = keys[curKeyNum].getChildKey();  // Yah, discard the objectKey part. This is kind of silly, isn't it?
                    numMissedKeys++;
                }
            }
            if (numMissedKeys > 0)
            {
                missHandler.handleBulkMiss(returnObjects, missedKeys);
            }
            for (int curKeyNum = 0; curKeyNum < keys.length; curKeyNum++)
            {
                ObjectKey<K> curKey = keys[curKeyNum];
                V object = returnObjects.get(curKey.getChildKey());
                if (object != null)
                {
                    lruQueue.put(curKey, object);
                    WeakReference<V> newCacheRef = new WeakReference<V>(object, refQueue);
                    WeakReference oldCacheRef = cacheObjs.put(curKey, newCacheRef);
                    if (oldCacheRef != null)
                        reverseMap.remove(oldCacheRef);
                    reverseMap.put(newCacheRef, curKey);
                }
            }
        }
        verifyCache();
        return returnObjects;
    }
    
    public synchronized Object remove(ObjectKey<K> key)
    {
        verifyCache();
        WeakReference<V> removeRef = cacheObjs.remove(key);
        Object returnObject = null;
        if (removeRef != null)
        {
            returnObject = removeRef.get();
            reverseMap.remove(removeRef);
        }
        lruQueue.remove(key);
        verifyCache();
        return returnObject;
    }

    public int numReadyToFlush()
    {
        int readyToFlush = 0;
        long now = System.currentTimeMillis();
        synchronized (this)
        {
            for (V curCheckDirtyObject:dirtyMap.values())
            {
                if (now - curCheckDirtyObject.getDirtyTime() > maxDirtyTime)
                    readyToFlush++;
            }
        }
        return readyToFlush;
    }
    
    public void flush() throws IOException
    {
        flush(false);
    }
    
    public void flush(boolean flushComplete) throws IOException
    {
        verifyCache();
        ArrayList<V> objectsToWrite = new ArrayList<V>();
        int numFreshDirty = 0;
        synchronized (this)
        {
            if (flushComplete)
            {
                objectsToWrite.addAll(dirtyMap.values());
                dirtyMap.clear();
            }
            else
            {
                long now = System.currentTimeMillis();
                for (V curCheckDirtyObject:dirtyMap.values())
                {
                    if (now - curCheckDirtyObject.getDirtyTime() > maxDirtyTime)
                        objectsToWrite.add(curCheckDirtyObject);
                    else
                        numFreshDirty++;
                }
                for (V curRemoveFromDirtyMapObject:objectsToWrite)
                    dirtyMap.remove(curRemoveFromDirtyMapObject.getIDObject());
            }
            notifyAll();
        }
        verifyCache();
        for(V curDirtyObject:objectsToWrite)
        {
            missHandler.storeObject(curDirtyObject);
        }
        missHandler.commit();
        verifyCache();
        /*
        if (logger.isDebugEnabled())
            logger.debug("Flushed cache, "+objectsToWrite.size()+" objects flushed, "+numFreshDirty+" objects retained for later");*/
    }

    /**
     * In the event of a cache miss, this routine is called to load the object
     * from the database, or where ever as necessary
     * @param key
     * @return
     */
    protected V handleMiss(ObjectKey<K> key)
    {
        verifyCache();
        if (missHandler != null)
        {
            V returnObj = missHandler.handleMiss(key.getChildKey());
            verifyCache();
            return returnObj;
        }
        return null;
    }

    protected void handleBulkMiss(HashMap<K, V> retrievedObjects, K[] keys)
    {
        verifyCache();
        if (missHandler != null)
            missHandler.handleBulkMiss(retrievedObjects, keys);
        verifyCache();
    }
    
    public long getHits()
    {
        verifyCache();
        return hits;
    }

    public long getMisses()
    {
        verifyCache();
        return misses;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends CachableObject<K>>void notifyDirty(T dirtyObject)
    {
        verifyCache();
        if (dirtyMap.size() > maxDirtySize)
        {
            try
            {
                wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        dirtyMap.put(dirtyObject.getIDObject(), (V)dirtyObject);
    }

    public static int getMaxDirtySize()
    {
        return maxDirtySize;
    }
    
    /**
     * Notifies the cache that the object has been used.  Mainly used
     * by CachableObjectHandle
     * @param key
     * @param object
     */
    protected void notifyUsed(ObjectKey<K> key, V object)
    {
        verifyCache();
        lruQueue.put(key, object);
    }
    
    boolean closed = false;
    public void close() throws IOException
    {
    	closed = true;
        ObjectCacheClearHandler.getObjectCacheClearHandler().removeObjectCache(this);
        flush(true);
    }
    
    private void verifyCache()
    {
        /*
        if (cacheObjs.size() != reverseMap.size())
        {
            System.out.println("cache and reversemap do not match - cache = "+cacheObjs.size()+", reverseMap = "+reverseMap.size());
        }
        if (cacheObjs.size() > (lruSize > kCacheDangerSize ? lruSize:kCacheDangerSize))
        {
            System.out.println("Cache getting large - cache size = "+cacheObjs.size());
        }
        if (dirtyMap.size() > lruSize)
        {
            System.out.println("Cache flushing falling behind - dirtymap size = "+dirtyMap.size());
        }
        */
    }
    
    @SuppressWarnings("unchecked")
    protected void pollRefQueue()
    {
        verifyCache();
        WeakReference<V> removeRef;
        while ((removeRef = (WeakReference<V>)refQueue.poll()) != null)
        {
            Object key = reverseMap.get(removeRef);
            if (key != null)
            {
                WeakReference<V> whackRef = cacheObjs.get(key);
                // There's a race between the refQueue and the object being re-fetched
                // Make sure that we're really removing the dead reference
                if (whackRef == removeRef)
                    cacheObjs.remove(key);
                reverseMap.remove(removeRef);
            }
        }
        
        if (cacheObjs.size() > kCacheDangerSize)
        {
            Iterator<Map.Entry<ObjectKey<K>, WeakReference<V>>> cacheIterator = cacheObjs.entrySet().iterator();
            while(cacheIterator.hasNext())
            {
                Map.Entry<ObjectKey<K>, WeakReference<V>> curEntry = cacheIterator.next();
                if (curEntry.getValue().get() == null)
                {
                    cacheIterator.remove();
                    reverseMap.remove(curEntry.getValue());
                }
            }
        }
        verifyCache();
    }
    
    @Override
    public void finalize()
    {
    	if (!closed)
    	{
            ObjectCacheClearHandler.getObjectCacheClearHandler().removeObjectCache(this);
            try
			{
				flush();
			} catch (IOException e)
			{
				LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
			}
    	}
    }
}
