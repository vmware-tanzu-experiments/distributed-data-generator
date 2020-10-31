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

import com.igeekinc.util.ClientFile;

public class SplitFileSegmentInfo
{
    private long segmentLength;
    private ClientFile segmentFile;
    
    public SplitFileSegmentInfo(long segmentLength, ClientFile segmentFile)
    {
        this.segmentLength = segmentLength;
        this.segmentFile = segmentFile;
    }

    public ClientFile getSegmentFile()
    {
        return segmentFile;
    }

    /**
     * Gets the expected length of the segment.  Returns -1 if the length is unknown
     * @return
     */
    public long getSegmentLength()
    {
        return segmentLength;
    }

    public void setSegmentLength(long newSegmentLength)
    {
        segmentLength = newSegmentLength;
    }
}
