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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The LRUQueue maintains a list of objects by key.  Objects are
 * added at the head of the queue and moved back there whenever referenced.
 * The tail of the queue will be the object that has been least recently used (LRU).
 * The LRU queue will automatically remove objects from the tail when the size is over the
 * maximum number of objects to hold.
 * @author David L. Smith-Uchida
 *
 */
class ObjectHolder<K, V>
{
    ObjectHolder<K, V> next, prev;    // Doubly linked list
    K key;
    V object;
}

public class LRUQueue<K, V> implements Map<K, V>
{
    private HashMap<K, ObjectHolder<K, V>> lookupMap;
    private ObjectHolder<K, V> head, tail;    // The LRU queue
    private int maxObjects;
    
    public LRUQueue(int inMaxObjects)
    {
        maxObjects = inMaxObjects;
        lookupMap = new HashMap<K, ObjectHolder<K, V>>(maxObjects);
        head = tail = null;
    }

    public synchronized void clear()
    {
        lookupMap.clear();
        head = tail = null; // Dump the queue
        checkSize();
    }

    public synchronized boolean containsKey(Object key)
    {
        return lookupMap.containsKey(key);
    }

    public synchronized boolean containsValue(Object value)
    {
        ObjectHolder<K, V> curHolder = head;
        while (curHolder != null)
        {
            if (curHolder.object.equals(value))
                return true;
            curHolder = curHolder.next;
        }
        return false;
    }
    
    class LRUQueueEntry<LK, LV> implements Map.Entry<LK, LV>
    {
        LK key;
        HashMap<LK, ObjectHolder<LK, LV>> hashMap;
        LRUQueueEntry(LK key, HashMap<LK, ObjectHolder<LK, LV>> hashMap)
        {
            this.key = key;
            this.hashMap = hashMap;
        }
        public LK getKey()
        {
            return key;
        }

        public LV getValue()
        {
            ObjectHolder<LK, LV> curHolder = hashMap.get(key);
            if (curHolder != null)
                return curHolder.object;
            return null;
        }

        public LV setValue(LV value)
        {
            // TODO Auto-generated method stub
            return null;
        }
        
    }

    public Set<Map.Entry<K, V>> entrySet()
    {
        return (new AbstractSet<Map.Entry<K, V>>()
        {

            public Iterator<Map.Entry<K, V>> iterator()
            {
                return new Iterator<Map.Entry<K, V>>()
                {
                    Iterator<K> keyIterator = lookupMap.keySet().iterator();
                    public boolean hasNext()
                    {
                        return keyIterator.hasNext();
                    }
                    public Map.Entry<K, V> next()
                    {
                        K key = keyIterator.next();
                        //ObjectHolder<K, V> value = lookupMap.get(key);
                        LRUQueueEntry<K, V> returnEntry = new LRUQueueEntry<K, V>(key, lookupMap);
                        return returnEntry;
                    }
                    public void remove()
                    {
                        throw new UnsupportedOperationException("Can't remove via LRUQueue iterator");
                    }
                };
            }

            public int size()
            {
                return lookupMap.size();
            }
            
        });
    }

    /**
     * gets an object by its key value from the queue.  Does not update its used status.
     * In order to move an item to the head of the queue, add it again with put().
     */
    public synchronized V get(Object key)
    {
    	checkSize();
        ObjectHolder<K, V> objectHolder = lookupMap.get(key);
        if (objectHolder != null)
            return (objectHolder).object;
        else
            return null;
    }

    public boolean isEmpty()
    {
        return lookupMap.isEmpty();
    }

    public Set<K> keySet()
    {
        return lookupMap.keySet();
    }

    public synchronized V put(K key, V value)
    {
    	checkSize();
        ObjectHolder<K, V> holder;
        holder = lookupMap.get(key);
        V oldValue = null;
        if (holder != null)
        {
            if (holder.next != null)
                holder.next.prev = holder.prev;
            if (holder.prev != null)
                holder.prev.next = holder.next;
            if (head == holder)
                head = holder.next;
            if (tail == holder)
                tail = holder.prev;
            oldValue = holder.object;
        }
        else
        {
            holder = new ObjectHolder<K, V>();
            lookupMap.put(key, holder);
        }
        holder.object = value;
        holder.key = key;
        holder.next = head;
        if (holder.next != null)
            holder.next.prev = holder;
        holder.prev = null;
        head = holder;
        if (tail == null)
            tail = head;
        int curSize = lookupMap.size(); // Call directly and avoid our locking overhead
        while (curSize > maxObjects)
        {
        	checkSize();
            K removeKey = tail.key;
			ObjectHolder<K, V> removeHolder = lookupMap.remove(removeKey);
			if (removeHolder != tail)
				throw new InternalError(removeKey.toString()+" not found in remove, but in list");
            curSize--;
            tail = tail.prev;
            if (tail != null)
                tail.next = null;
            else
                head = null;    // Someone must have set maxObjects to 0
            checkSize();
        }
        checkSize();
        return oldValue;
    }

    public synchronized void putAll(Map<? extends K, ? extends V> insertMap)
    {
        Set<? extends Map.Entry<? extends K, ? extends V>> insertSet = insertMap.entrySet();
        Iterator<? extends Map.Entry<? extends K, ? extends V>> insertIterator = insertSet.iterator();
        while(insertIterator.hasNext())
        {
            Map.Entry<? extends K, ? extends V> curEntry = insertIterator.next();
            put(curEntry.getKey(), curEntry.getValue());
        }
        checkSize();
    }

    public synchronized V remove(Object key)
    {
        ObjectHolder<K, V> holder;
        holder = lookupMap.get(key);
        V oldValue = null;
        if (holder != null)
        {
            if (head == holder)
                head = holder.next;
            if (tail == holder)
                tail = holder.prev;
            if (holder.next != null)
                holder.next.prev = holder.prev;
            if (holder.prev != null)
                holder.prev.next = holder.next;
            oldValue = holder.object;
            lookupMap.remove(holder.key);
        }
        checkSize();
        return oldValue;
    }

    public synchronized int size()
    {
        return lookupMap.size();
    }

    private boolean doCheckSize = false;
    private void checkSize()
    {
    	if (doCheckSize)
    	{
    		int listSize = 0;
    		ObjectHolder<K,V>countHolder = head;
    		while (countHolder != null)
    		{
    			if (!lookupMap.containsKey(countHolder.key))
    				throw new InternalError("Can't find "+countHolder.key+" in lookup map but is present in list");
    			countHolder = countHolder.next;
    			listSize ++;
    		}
    		if (listSize != lookupMap.size())
    			throw new InternalError("Size mismatch");
    	}
    }
    public synchronized Collection<V> values()
    {
        Collection<ObjectHolder<K, V>> holdersCollection = lookupMap.values();
        
        ArrayList<V> valuesList = new ArrayList<V>(holdersCollection.size());
        Iterator<ObjectHolder<K, V>> valuesIterator = holdersCollection.iterator();
        while (valuesIterator.hasNext())
        {
            ObjectHolder<K, V> curHolder = valuesIterator.next();
            valuesList.add(curHolder.object);
        }
        return valuesList;
    }
    
}
