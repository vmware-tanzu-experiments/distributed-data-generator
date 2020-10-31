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

/**
 * FileClass defines a class of file.  FileClass has a hierarchy of types.  All file classes
 * need to descend from the kRootClass.  New top level classes can be defined as necessary.
 */
public class FileClass extends TreeInfoBase implements Serializable
{
    private static final long serialVersionUID = -4343319755961939063L;
    
    public static final FileClass kRootClass = new FileClass();
    
    // Top level classes
    public static final FileClass kSystemFile = new FileClass(kRootClass, "System");
    public static final FileClass kTemporaryFile = new FileClass(kRootClass, "Temporary");
    public static final FileClass kApplication = new FileClass(kRootClass, "Application");
    public static final FileClass kDocument = new FileClass(kRootClass, "Document");
    
    // Temporary classes
    public static final FileClass kLogFile = new FileClass(kTemporaryFile, "Log");

    // Document classes
    public static final FileClass kRichText = new FileClass(kDocument, "RichText");
    

    /**
     * Should only be called to create the root element
     * @param classID
     */
    private FileClass()
    {
        super();
    }
    
    public FileClass(FileClass parentClass, String classID)
    {
        super(parentClass, classID);
    }

    protected FileClass(String parentPath, String classID)
    {
        super((FileClass) FileClass.getElementForPath(kRootClass, parentPath, true), classID);
    }
    
    protected TreeInfoBase newObject(TreeInfoBase parent, String childID)
    {
        return new FileClass((FileClass)parent, childID);
    }
}
