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

import com.igeekinc.util.ChangeModel;
import com.igeekinc.util.CheckCorrectDispatchThread;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ObjectManager<K, V extends CachableObject<K>> extends ChangeModel
{
    protected ObjectCache<K, V> objectCache;
    protected int hashCode = hashCode();
    public ObjectManager(CheckCorrectDispatchThread dispatcher)
    {
        super(dispatcher);
    }
    
    /*
    protected void setHandle(CachableObject object, CachableObjectHandle handleToSet)
    {
        object.handle = handleToSet;
    }
    */
    
    @SuppressWarnings("unchecked")
    protected void updateHandle(V object)
    {
        object.parentTable = (ObjectManager<K, CachableObject<K>>) this;
        object.setupHandle();
    }
    protected void setID(V object, K newID)
    {
        object.setIDObject(newID);
    }
    
    public V getObjectByID(K objectID)
    {
    	return getObjectByID(objectID, false);
    }
    
    public V getObjectByID(K objectID, boolean cacheOnly)
    {
        if (objectID instanceof Long)
        {
            if (((Long)objectID).longValue() == 0)
                throw new IllegalArgumentException("Object ID 0 is illegal");
        }
        ObjectKey<K> key = new ObjectKey<K>(this, objectID);
        return(objectCache.internalGet(key, cacheOnly));
    }
    
    protected void addObjectToCache(ObjectKey<K> objectKey, V objectToCache)
    {
        objectCache.put(objectKey, objectToCache);
    }
    @SuppressWarnings("unchecked")
    public HashMap<K, V> getObjectsByID(List<K> objectIDs)
    {
        ObjectKey<K> [] keys = new ObjectKey[objectIDs.size()];
        Iterator<K> idIterator = objectIDs.iterator();
        int curIDNum = 0;
        while(idIterator.hasNext())
        {
            K objectID = idIterator.next();
            if (objectID instanceof Long)
            {
                if (((Long)objectID).longValue() == 0)
                    throw new IllegalArgumentException("Object ID 0 is illegal");
            }
            keys[curIDNum++] = new ObjectKey<K>(this, objectID);
        }
        return getObjectsByID(keys);
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<K, V> getObjectsByID(K [] objectIDs)
    {
        ObjectKey<K> [] keys = new ObjectKey[objectIDs.length];
        for(int curIDNum = 0; curIDNum < objectIDs.length; curIDNum++)
        {
            K objectID = objectIDs[curIDNum];
            if (objectID instanceof Long)
            {
                if (((Long)objectID).longValue() == 0)
                    throw new IllegalArgumentException("Object ID 0 is illegal");
            }
            keys[curIDNum] = new ObjectKey<K>(this, objectID);
        }
        return getObjectsByID(keys);
    }
    
    protected HashMap<K, V> getObjectsByID(ObjectKey<K> [] keys)
    {
        return(objectCache.internalBulkGet(keys));
    }
    public void flush() throws IOException
    {
        objectCache.flush(true);
    }
    
    protected void notifyUsed(V usedObject)
    {
        objectCache.notifyUsed(usedObject.objectKey, usedObject);
    }
}
