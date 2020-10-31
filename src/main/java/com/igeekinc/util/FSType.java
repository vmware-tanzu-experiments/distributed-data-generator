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

public class FSType
{
    private String fsTypeString, description;
    protected FSType(String fsTypeString, String description)
    {
        this.fsTypeString = fsTypeString;
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }
    public String getFsTypeString()
    {
        return fsTypeString;
    }
    public boolean equals(Object obj)
    {
        if (obj instanceof FSType)
        {
            FSType checkFSType = (FSType)obj;
            return fsTypeString.equals(checkFSType.fsTypeString);
        }
        return false;
    }
    
    public int hashCode()
    {
        return fsTypeString.hashCode();
    }
    
    public String toString()
    {
        // TODO Auto-generated method stub
        return super.toString();
    }

    
}
