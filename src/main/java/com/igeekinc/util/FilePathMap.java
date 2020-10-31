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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class FilePathMapNode<V>
{
    FilePathMapNode<V>parent;
    V value;
    HashMap<String, FilePathMapNode<V>>children;
    
    FilePathMapNode(V value, FilePathMapNode<V>parent)
    {
        this.value = value;
        this.parent = parent;
        children = new HashMap<String, FilePathMapNode<V>>();
    }
    
    FilePathMapNode()
    {
        children = new HashMap<String, FilePathMapNode<V>>();
    }

    public V getValue()
    {
        if (value != null)
            return value;
        // If we're a leaf node, roll up the tree to the first parent that has a value
        FilePathMapNode<V>curParent = parent;
        while (curParent != null)
        {
            if (curParent.value != null)
                return curParent.value;
            curParent = curParent.parent;
        }
        return null;
    }

    public void setValue(V value)
    {
        this.value = value;
    }
    
    public void addChild(String name, FilePathMapNode<V> child)
    {
        children.put(name, child);
    }
    
    public V add(FilePath path, int offset, V value)
    {
        String curNodeName = path.getComponent(offset);
        FilePathMapNode<V>curNode = getChild(curNodeName);
        if (curNode != null)
        {
            if (offset == path.getNumComponents() - 1)
            {
                // We're at the node for the path.
                V returnValue = curNode.getValue();
                curNode.setValue(value);
                return returnValue;
            }
        }
        else
        {
            if (offset == path.getNumComponents() - 1)
                curNode = new FilePathMapNode<V>(value, this);
            else
                curNode = new FilePathMapNode<V>(null, this); // Create non-leaf node
            addChild(curNodeName, curNode);
        }
        if (offset < path.getNumComponents() - 1)
            curNode.add(path, offset + 1, value);
        return null;
    }
    public FilePathMapNode<V> getChild(String name)
    {
        return children.get(name);
    }
    
    /**
     * Returns the last node matching the path - this may be the node that matches the entire path or it may be the last leaf
     * to match the path (e.g. the tree holds /A/B and the path given is /A/B/C/D, the /A/B node will be returned)
     * @param path
     * @param pathOffset
     * @param closestMatch - if this is set, the node farthest along the path will be returned
     * @return
     */
    public FilePathMapNode<V> getChild(FilePath path, int pathOffset, boolean closestMatch)
    {
        if (pathOffset < path.getNumComponents())
        {
            FilePathMapNode<V> nextNode = getChild(path.getComponent(pathOffset));
            if (nextNode != null)
                return nextNode.getChild(path, pathOffset + 1, closestMatch);
            if (!closestMatch)
                return null;
        }
        return this;
    }
    
    public boolean containsValue(Object value)
    {
        if (children.containsValue(value))
            return true;
        for (FilePathMapNode<V>curChild:children.values())
        {
            if (curChild.containsValue(value))
                return true;
        }
        return false;
    }
    
    public V removeNode(FilePath path, int pathOffset)
    {
        if (pathOffset < path.getNumComponents() - 1)
        {
            FilePathMapNode<V> nextNode = getChild(path.getComponent(pathOffset));
            if (nextNode != null)
                return nextNode.removeNode(path, pathOffset++);
        }
        else
        {
            FilePathMapNode<V>removeNode = children.remove(path.getComponent(pathOffset));
            if (removeNode != null)
                return removeNode.getValue();
        }
        return null;
    }
}

public class FilePathMap<V> implements Map<FilePath, V>
{
    FilePathMapNode<V> root;
    
    public FilePathMap()
    {
        root = null;
    }
    public void clear()
    {
        root = null;
    }

    public boolean containsKey(Object key)
    {
        if (!(key instanceof FilePath))
            return false;
        if (root != null)
            return (root.getChild((FilePath)key, 1, false) != null);
        return false;
    }

    public boolean containsValue(Object value)
    {
        if (root != null)
            return root.containsValue(value);
        return false;
    }

    public Set<java.util.Map.Entry<FilePath, V>> entrySet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public V get(Object key)
    {
        if (root != null && key instanceof FilePath)
            return root.getChild((FilePath)key, 1, true).getValue();
        return null;
    }

    public boolean isEmpty()
    {
        if (root == null)
            return true;
        return false;
    }

    public Set<FilePath> keySet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public V put(FilePath key, V value)
    {
        if (root == null)
            root = new FilePathMapNode<V>();
        if (key.getNumComponents() == 1)
        {
            V returnVal = root.getValue();
            root.setValue(value);
            return returnVal;
        }
        return root.add(key, 1, value);
    }

    public void putAll(Map<? extends FilePath, ? extends V> m)
    {
        for (Map.Entry<? extends FilePath, ? extends V>curEntry:m.entrySet())
        {
            root.add(curEntry.getKey(), 1, curEntry.getValue());
        }
    }

    public V remove(Object key)
    {
        if (key instanceof FilePath)
        {
            return root.removeNode((FilePath)key, 1);
        }
        return null;
    }

    public int size()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Collection<V> values()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
