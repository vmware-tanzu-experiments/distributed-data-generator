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

/**
 * Makes keys unique for the object that is managing them.  This
 * allows the ObjectCache to be shared even though there may be keys with
 * the same value in it.
 */
public final class ObjectKey<K>
{
    private ObjectManager<K, ?> parent;
    private K childKey;
    private int hashCode;
    public ObjectKey(ObjectManager<K, ?> inParent, K inChildKey)
    {
        parent = inParent;
        childKey = inChildKey;
        if (parent != null && childKey != null)
            hashCode = parent.hashCode ^ childKey.hashCode();
        else
        {
            if (parent != null)
                hashCode = parent.hashCode;
            else
                hashCode = childKey.hashCode();
        }
    }
    
    /*
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ObjectKey<?> checkKey = (ObjectKey<?>)obj;
        if (checkKey.parent == parent &&
                checkKey.childKey.equals(childKey))
            return true;

        return false;
    }
*/
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ObjectKey<?>))
            return false;
        ObjectKey<?> checkKey = (ObjectKey<?>)obj;
        if (checkKey.parent == parent &&
                checkKey.childKey.equals(childKey))
            return true;

        return false;
    }
    
    /*
    public boolean equals(Object obj)
    {
        // ObjectKey equals gets beat to hell and gone for maintaining the LRU queue.  This has
        // been optimized for the mainline case which is the important one for our performance
        // Assume that we're normally comparing against a valid reference of the same class and 
        // catch the class cast exception or null pointer exception
        // for correctness (but slow in the abnormal case)
        try
        {
            ObjectKey<?> checkKey = (ObjectKey<?>)obj;
            if (checkKey.parent == parent &&
                    checkKey.childKey.equals(childKey))
                return true;
        }
        catch (ClassCastException e)
        {
            // Different classes - we usually don't get here
        }
        catch (NullPointerException e1)
        {
            // Null pointer - again, not the usual path so we're on the slower but less travelled path
        }
        return false;
    }
    */
    public int hashCode()
    {
        return hashCode;
    }

    
    public K getChildKey()
    {
        return childKey;
    }


    public ObjectManager<K, ?> getParent()
    {
        return parent;
    }
}
