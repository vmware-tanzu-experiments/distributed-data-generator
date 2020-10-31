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

import javax.swing.event.ChangeListener;

import com.igeekinc.util.ChangeEventSupport;


public abstract class CachableObject<I> implements CachableObjectIF<I>
{

    protected I id;
    protected ObjectKey<I> objectKey;
    protected CachableObjectHandle<I, ? > handle;
    protected ChangeEventSupport changeSupport;
    protected ObjectManager<I, ? extends CachableObjectIF<I>> parentTable;
    protected ObjectCache<I, ? extends CachableObjectIF<I>> parentCache;
    private long dirtyTime;
    
    protected CachableObject()
    {
        // For Hibernate
    }
    
    protected CachableObject(I inID, ObjectManager<I, ? extends CachableObject<I>> inObjectManager)
    {
        id = inID;
        parentTable = inObjectManager;
        parentCache = inObjectManager.objectCache;
        changeSupport = new ChangeEventSupport();
        handle = (CachableObjectHandle<I, ? extends CachableObjectIF<I>>)setupHandle();
        objectKey = new ObjectKey<I>(parentTable, id);
    }
    
    protected abstract CachableObjectHandle<I, ? extends CachableObjectIF<I>> setupHandle();
            /*
    {
        return new CachableObjectHandle<I, ? extends CachableObject<I>>(this, parentTable);
    }*/

    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#getIDObject()
     */
    public I getIDObject()
    {
        return id;
    }

    protected void setIDObject(I newID)
    {
        id = newID;
    }
    
    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#getHandle()
     */
    public CachableObjectHandle<I, ? extends CachableObjectIF<I>> getHandle()
    {
        if (handle == null)
            throw new InternalError("Handle not set");
        return handle;
    }
    
    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(ChangeListener newListener)
    {
        changeSupport.addChangeListener(newListener);
    }

    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener(ChangeListener removeListener)
    {
        changeSupport.removeChangeListener(removeListener);
    }

    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#fireChangeEvent()
     */
    public void fireChangeEvent()
    {
        changeSupport.fireChangeEvent(this);
    }

    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
        if (obj instanceof CachableObject)
            return equals((CachableObject)obj);
        else
            return super.equals(obj);
    }

    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#equals(com.igeekinc.util.objectcache.CachableObject)
     */
    @SuppressWarnings("unchecked")
    public boolean equals(CachableObject obj)
    {
        if (obj != null && (( obj == this) || (obj.id != null && obj.id.equals(id) && getClass().isInstance(obj))))
            return true;
        return false;
    }
    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#hashCode()
     */
    public int hashCode()
    {
        return id.hashCode();
    }

    
    protected void setDirty()
    {
        synchronized(this)
        {
            dirtyTime = System.currentTimeMillis();
        }
        parentCache.notifyDirty(this);
    }
    
    protected synchronized void clearDirty()
    {
        dirtyTime = 0L;
    }
    
    /* (non-Javadoc)
     * @see com.igeekinc.util.objectcache.CachableObjectIF#isDirty()
     */
    public boolean isDirty()
    {
        return dirtyTime != 0;
    }
    
    public long getDirtyTime()
    {
        return dirtyTime;
    }
    
    public ObjectManager<I, ? extends CachableObject<I>> getParentTable()
    {
        return parentTable;
    }
    
    public ObjectKey<I> getObjectKey()
    {
        return objectKey;
    }
}
