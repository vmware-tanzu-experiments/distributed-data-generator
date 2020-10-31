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
 
package com.igeekinc.util.fileinfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.StringTokenizer;

public abstract class TreeInfoBase implements Serializable
{
    private static final long serialVersionUID = 3557988868471899747L;
    public static final String kPathSeparator = ".";
    protected transient TreeInfoBase parentElem;
    protected String parentPath;
    protected String elementID;
    protected transient HashMap<String, TreeInfoBase>children = new HashMap<String, TreeInfoBase>();

    protected TreeInfoBase()
    {
        parentElem = null;
        elementID = "";
    }
    
    protected TreeInfoBase(TreeInfoBase parentGroup, String groupID)
    {
        this.parentElem = parentGroup;
        this.elementID = groupID;
        parentElem.addChild(this);
        parentPath = parentElem.toString();
    }
    
    public static TreeInfoBase getElementForPath(TreeInfoBase root, String elementPath, boolean create)
    {
        StringTokenizer tokenizer = new StringTokenizer(elementPath, kPathSeparator);
        TreeInfoBase treeInfo = root;
        while(tokenizer.hasMoreElements())
        {
            String curElemID = tokenizer.nextToken();
            TreeInfoBase parent = treeInfo;
            treeInfo = parent.getChild(curElemID);
            if (treeInfo == null)
            {
                if (!create)
                    break;
                treeInfo = root.newObject(parent, curElemID);
            }
        }
        return treeInfo;
    }
    
    protected abstract TreeInfoBase newObject(TreeInfoBase parent, String childID);
    protected void addChild(TreeInfoBase child)
    {
        children.put(child.elementID, child);
    }
    
    public TreeInfoBase getChild(String childID)
    {
        return children.get(childID);
    }
    
    public TreeInfoBase getParent()
    {
        return parentElem;
    }

    public String getElementID()
    {
        return elementID;
    }

    
    public String toString()
    {
        if (parentElem == null)
            return "";
        return parentElem.toString()+kPathSeparator+elementID;
    }
    
    
}
