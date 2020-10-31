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
import java.io.OutputStream;

public class BufferQueueOutputStream extends OutputStream
{
    private BufferQueue bufferQueue;
    protected BufferQueueOutputStream(BufferQueue bufferQueue)
    {
        this.bufferQueue = bufferQueue;
    }
    public void write(int b) throws IOException
    {
        byte [] buf = new byte[1];
        buf[0] = (byte)b;
        write(buf);
    }
    public void close() throws IOException
    {
        bufferQueue.close();
    }
    
    public void write(byte[] b, int off, int len) throws IOException
    {
        bufferQueue.addBuffer(b, off, len);
    }
    public void write(byte[] b) throws IOException
    {
        bufferQueue.addBuffer(b);
    }
    
    public void writeNoCopy(byte [] b)
    {
        bufferQueue.addBufferNoCopy(b);
    }
}
