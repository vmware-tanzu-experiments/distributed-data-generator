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

public interface CachableObjectIF<I>
{
    public abstract I getIDObject();

    public abstract CachableObjectHandle<I, ? extends CachableObjectIF<I>> getHandle();

    /**
     * CachableObject supports ChangeListeners for convenience.  The listeners
     * are not stored persistently so in order to be sure to receive events, keep
     * a reference to the object.  CachableObjectHandles do not maintain a reference.
     * The cache and GC may discard unreferenced objects at any time even if they have
     * ChangeListeners attached and if the object is subsequently reloaded by the cache
     * the ChangeListeners will not be reattached.
     * @param newListener
     */
    public abstract void addChangeListener(ChangeListener newListener);

    public abstract void removeChangeListener(ChangeListener removeListener);

    public abstract void fireChangeEvent();

    public abstract boolean equals(Object obj);

    @SuppressWarnings("unchecked")
    public abstract boolean equals(CachableObject obj);

    public abstract int hashCode();

    public abstract boolean isDirty();
    
    public abstract long getDirtyTime();    // Returns the time when the object was last set dirty
}