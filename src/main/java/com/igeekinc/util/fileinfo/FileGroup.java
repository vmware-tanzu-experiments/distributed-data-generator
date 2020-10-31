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

/**
 * A FileGroup identifies the "group" a file should be placed in.  For example, Microsoft Office documents could
 * be classed as a group.  The FileGroup is a hierarchy and should be based on the reverse DNS of the manufacturer.
 */
public class FileGroup extends TreeInfoBase
{
    private static final long serialVersionUID = 7202921891716933169L;
    private static FileGroup root = new FileGroup();
    static 
    {
        root = new FileGroup();
    }
    
    protected FileGroup()
    {
        super();
    }
    
    public FileGroup(FileGroup parentGroup, String groupID)
    {
        super(parentGroup, groupID);
    }
    
    protected FileGroup(String parentPath, String classID)
    {
        super((FileGroup) FileGroup.getElementForPath(root, parentPath, true), classID);
    }
    
    protected FileGroup newObject(TreeInfoBase parent, String childID)
    {
        return new FileGroup((FileGroup)parent, childID);
    }
    
    @Override
    public FileGroup getChild(String childID)
    {
        return (FileGroup)super.getChild(childID);
    }

    public static FileGroup getElementForPath(String fileGroupPath, boolean create)
    {
        return (FileGroup)TreeInfoBase.getElementForPath(root, fileGroupPath, create);
    }
    
    @Override
    public FileGroup getParent()
    {
        return (FileGroup)super.getParent();
    }
}
