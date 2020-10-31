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

public class ExistingFileSegmentInfo extends SplitFileSegmentInfo
{

    public ExistingFileSegmentInfo(long segmentLength, ClientFile segmentFile)
    {
        super(-1, segmentFile);
        if (!segmentFile.exists())
            throw new IllegalArgumentException("File "+segmentFile+" must exist");
    }

    public long getSegmentLength()
    {  
        return(getSegmentFile().length());
    }
}
