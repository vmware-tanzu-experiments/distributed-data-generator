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

import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;



public class DefaultClientFileIterator implements ClientFileIterator
{
    ClientFile directoryToIterate;
    String [] childrenNames;
    int childrenOffset;
    
    public DefaultClientFileIterator(ClientFile directoryToIterate)
    {
        if (!directoryToIterate.isDirectory())
            throw new IllegalArgumentException("Can only iterate over directories");
        this.directoryToIterate = directoryToIterate;
        childrenNames = directoryToIterate.list();
        if (childrenNames == null)
            childrenNames = new String[0];
        childrenOffset = 0;
    }
    public boolean hasNext()
    {
        return (childrenOffset < childrenNames.length);
    }

    public ClientFile next()
    {
        ClientFile returnFile = null;
        if (childrenOffset < childrenNames.length)
        {
            try
            {
                returnFile = directoryToIterate.getChild(childrenNames[childrenOffset]);
            } catch (IOException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
            }
            childrenOffset ++;
        }
        return returnFile;
    }

    public void remove()
    {
        // TODO Auto-generated method stub
        
    }
}
