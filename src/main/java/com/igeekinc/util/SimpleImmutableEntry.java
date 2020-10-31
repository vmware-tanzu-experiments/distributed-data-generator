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

import java.util.Map.Entry;

/**
 * An Entry maintaining an immutable key and value.  This class
 * does not support method <tt>setValue</tt>.  
 */
public class SimpleImmutableEntry<K,V>
implements Entry<K,V>, java.io.Serializable
{
    private static final long serialVersionUID = -3257331089800816074L;
    private final K key;
    private final V value;

    /**
     * Creates an entry representing a mapping from the specified
     * key to the specified value.
     *
     * @param key the key represented by this entry
     * @param value the value represented by this entry
     */
    public SimpleImmutableEntry(K key, V value) 
    {
        this.key   = key;
        this.value = value;
    }

    /**
     * Creates an entry representing the same mapping as the
     * specified entry.
     *
     * @param entry the entry to copy
     */
    public SimpleImmutableEntry(Entry<? extends K, ? extends V> entry) 
    {
        this.key   = entry.getKey();
        this.value = entry.getValue();
    }

    /**
     * Returns the key corresponding to this entry.
     *
     * @return the key corresponding to this entry
     */
    public K getKey() 
    {
        return key;
    }

    /**
     * Returns the value corresponding to this entry.
     *
     * @return the value corresponding to this entry
     */
    public V getValue() 
    {
        return value;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  This implementation simply throws
     * <tt>UnsupportedOperationException</tt>, as this class implements
     * an <i>immutable</i> map entry.
     *
     * @param value new value to be stored in this entry
     * @return (Does not return)
     * @throws UnsupportedOperationException always
     */
    public V setValue(V value) 
    {
        throw new UnsupportedOperationException();
    }

   
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleImmutableEntry other = (SimpleImmutableEntry) obj;
        if (key == null)
        {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null)
        {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public String toString() {
        return key + "=" + value;
    }

}