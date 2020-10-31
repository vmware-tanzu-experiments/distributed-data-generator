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

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class MonitoredArrayList<T> extends ArrayList<T>
{
    private static final long serialVersionUID = 8836601481869492184L;
    private ChangeModel changeModel;
    
    public MonitoredArrayList(CheckCorrectDispatchThread checker)
    {
        changeModel = new ChangeModel(checker);
    }

    public MonitoredArrayList(Collection<? extends T> c, CheckCorrectDispatchThread checker)
    {
        super(c);
        changeModel = new ChangeModel(checker);
    }

    public MonitoredArrayList(int initialCapacity, CheckCorrectDispatchThread checker)
    {
        super(initialCapacity);
        changeModel = new ChangeModel(checker);
    }
    
    public void addListDataListener(ListDataListener listener)
    {
        changeModel.addListDataListener(listener);
    }
    
    public void removeListDataListener(ListDataListener listener)
    {
        changeModel.removeListDataListener(listener);
    }

    @Override
    public void add(int index, T element)
    {
        super.add(index, element);
        ListDataEvent addedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
        changeModel.fireListDataEvent(addedEvent);
    }

    @Override
    public boolean add(T e)
    {
        boolean wasAdded = super.add(e);
        if (wasAdded)
        {
            ListDataEvent addedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, size()-1, size()-1);
            changeModel.fireListDataEvent(addedEvent);
        }
        return wasAdded;
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        int startIndex = size();
        boolean wasAdded = super.addAll(c);
        if (wasAdded)
        {
            ListDataEvent addedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIndex, size()-1);
            changeModel.fireListDataEvent(addedEvent);
        }
        return wasAdded;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        int startIndex = index;
        boolean wasAdded = super.addAll(index, c);
        if (wasAdded)
        {
            ListDataEvent addedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIndex, startIndex+c.size());
            changeModel.fireListDataEvent(addedEvent);
        }
        return wasAdded;
    }

    @Override
    public void clear()
    {
        int endIndex = size() - 1;
        super.clear();
        ListDataEvent removedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, endIndex);
        changeModel.fireListDataEvent(removedEvent);
    }

    @Override
    public T remove(int index)
    {
        T removedObject = super.remove(index);

        // Always fire an event even if removedObject is null - null entries are allowed in the list
        ListDataEvent removedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
        changeModel.fireListDataEvent(removedEvent);

        return removedObject;
    }

    @Override
    public boolean remove(Object o)
    {
        int removeIndex = indexOf(o);
        if (removeIndex >= 0)
        {
            remove(removeIndex);
            return true;
        }
        return false;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
        ListDataEvent removedEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, fromIndex, toIndex);
        changeModel.fireListDataEvent(removedEvent);
    }

    @Override
    public T set(int index, T element)
    {
        T replacedObject = super.set(index, element);
        ListDataEvent changedEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
        changeModel.fireListDataEvent(changedEvent);
        return replacedObject;
    }
    
    
}
