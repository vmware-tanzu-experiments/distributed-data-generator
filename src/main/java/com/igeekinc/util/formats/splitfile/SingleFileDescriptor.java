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
 
package com.igeekinc.util.formats.splitfile;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.igeekinc.util.ClientFile;
import com.igeekinc.util.ClientFileMetaData;
import com.igeekinc.util.FilePath;

public class SingleFileDescriptor implements SplitFileDescriptor
{
    private ClientFile file;
    
    public SingleFileDescriptor(ClientFile file)
    {
        this.file = file;
    }
    public synchronized boolean hasNext()
    {
        if (file != null)
            return true;
        return false;
    }

    public synchronized SplitFileSegmentInfo next() throws IOException
    {
        if (file != null)
        {
            SplitFileSegmentInfo returnInfo = new SplitFileSegmentInfo(1, file);
            file = null;
            return returnInfo;
        }
        throw new NoSuchElementException("No more files");
    }
    public FilePath getFilePath()
    {
        return file.getFilePath();
    }
    public ClientFileMetaData getMetaData()
    throws IOException
    {
        return file.getMetaData();
    }
    
    public ClientFile getClientFile()
    {
        return file;
    }
    public void segmentFinished(SplitFileSegmentInfo finishedSegment) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}
