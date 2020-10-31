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
import java.io.OutputStream;

import com.igeekinc.util.DataStreamer;
import com.igeekinc.util.FileCopyProgressIndicatorIF;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

public class SplitFileDataStreamer implements DataStreamer
{
    private SplitFileDescriptor descriptor;
    public SplitFileDataStreamer(SplitFileDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
    public void streamData(OutputStream dataOutputStream,
            OutputStream rsrcOutputStream, PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress) throws IOException,
            AbortedException
    {
        
    }

}
