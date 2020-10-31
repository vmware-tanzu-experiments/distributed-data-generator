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
import java.util.ArrayList;
import java.util.Iterator;

import com.igeekinc.util.ClientFileMetaData;
import com.igeekinc.util.FilePath;

public class SimpleSplitFileDescriptor implements SplitFileDescriptor
{
    private ArrayList<SplitFileSegmentInfo> segmentList;
    private Iterator<SplitFileSegmentInfo> segmentIterator;
    public SimpleSplitFileDescriptor(SplitFileSegmentInfo singleSegment)
    {
        ArrayList<SplitFileSegmentInfo> newSegmentList = new ArrayList<SplitFileSegmentInfo>();
        newSegmentList.add(singleSegment);
        init(newSegmentList);
    }
    
    @SuppressWarnings("unchecked")
    public SimpleSplitFileDescriptor(ArrayList<SplitFileSegmentInfo> segmentList)
    {
        init((ArrayList<SplitFileSegmentInfo>)segmentList.clone());
    }
    
    private void init(ArrayList<SplitFileSegmentInfo> segmentList)
    {
        if (segmentList.size() < 1)
            throw new IllegalArgumentException("Must have at least one segment in the list");
        this.segmentList = segmentList;
        segmentIterator = this.segmentList.iterator();
    }
    public boolean hasNext()
    {
        return segmentIterator.hasNext();
    }
    
    public SplitFileSegmentInfo next() throws IOException
    {
        return(segmentIterator.next());
    }

    public FilePath getFilePath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ClientFileMetaData getMetaData()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void segmentFinished(SplitFileSegmentInfo finishedSegment) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
    
    
}