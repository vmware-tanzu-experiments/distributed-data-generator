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

import java.lang.ref.SoftReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachableObjectHandle<K, V extends CachableObject<K>> implements ChangeListener
{
    protected K objectID;
    protected ObjectManager<K, V> objectManager;
    protected V ref;   // Used for objects which are not yet stored in a table (id == -1)
    protected SoftReference<V> cachedRef;

    public CachableObjectHandle(K inObjectID, ObjectManager<K, V> inObjectTable)
    {
        if (inObjectID == null)
            throw new InternalError("Cant't create a CachableObject for invalid/unassigned ID unless original object is passed");
        objectID = inObjectID;
        objectManager = inObjectTable;
    }
    
    public CachableObjectHandle(V object, ObjectManager<K, V> inObjectTable)
    {
        objectID = object.getIDObject();
        objectManager = inObjectTable;
        if (objectID == null)
        {
            ref = object;
        }
        else
        {
            cachedRef = new SoftReference<V>(object);
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
        return equals((CachableObjectHandle<K, V>)obj);
    }
    
    public boolean equals(CachableObjectHandle<K, V> obj)
    {
        if (obj == null)
            return false;
        if (obj == this || (ref != null && obj.ref == ref))
            return true;
        if (obj.objectManager == null || obj.objectID == null)
            return false;
        if (obj.objectManager.equals(objectManager) && obj.objectID.equals(objectID))
            return true;
        return false;
    }

    public V getCachableObject()
    {
        return getCachableObject(false);
    }
    
    public V getCachableObject(boolean cacheOnly)
    {
        if (objectID == null)
            return ref;
        V returnObject = null;
        if (cachedRef != null)
        {
            returnObject = cachedRef.get();
        }
        if (returnObject == null)
        {
            returnObject = (V)objectManager.getObjectByID(objectID, cacheOnly);
            if (returnObject != null)
                cachedRef = new SoftReference<V>(returnObject);
        }
        else
        {
//          We were already hanging on to it so let the cache know that we've used it
            objectManager.notifyUsed(returnObject);
        }
        return returnObject;
    }

    public int hashCode()
    {
        ObjectKey<K> key = new ObjectKey<K>(objectManager, objectID);
        
        return key.hashCode();
    }

    /**
     * Primarily used to notify the handle when its object has been
     * stored in a table and the handle can release its reference
     * @param e
     */
    public void stateChanged(ChangeEvent e)
    {
        if (ref != null)
        {
            if (ref.getIDObject() != null)
            {
                objectID = ref.getIDObject();
                ref.removeChangeListener(this);
                ref = null;
            }
        }
    }
    
    public K getObjectID()
    {
        return objectID;
    }
}
