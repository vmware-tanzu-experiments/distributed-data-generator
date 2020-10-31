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

import java.io.IOException;
import java.io.InputStream;

public class ProgressIndicatingInputStream extends InputStream
{
    private InputStream wrappedStream;
    private InputStreamProgressIndicator progress;
    public ProgressIndicatingInputStream(InputStream wrappedStream, InputStreamProgressIndicator progress)
    {
        this.wrappedStream = wrappedStream;
        this.progress = progress;
    }

    public int read() throws IOException
    {
        int returnByte = wrappedStream.read();
        if (returnByte >= 0)
            progress.updateProgress(1);
        return returnByte;
    }


    public int read(byte[] arg0, int arg1, int arg2) throws IOException
    {
        int bytesRead = wrappedStream.read(arg0, arg1, arg2);
        if (bytesRead >= 0)
            progress.updateProgress(bytesRead);
        return bytesRead;
    }

    public int read(byte[] arg0) throws IOException
    {
        int bytesRead = wrappedStream.read(arg0);
        if (bytesRead >= 0)
            progress.updateProgress(bytesRead);
        return bytesRead;
    }

    public long skip(long arg0) throws IOException
    {
        
        long skippedBytes = wrappedStream.skip(arg0);
        progress.updateProgress(skippedBytes);
        return skippedBytes;
    }
    
    
}
