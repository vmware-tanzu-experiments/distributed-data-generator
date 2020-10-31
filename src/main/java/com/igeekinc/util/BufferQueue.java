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

public class BufferQueue extends ObjectQueue<byte []>
{
    private boolean inputStreamRetrieved = false, outputStreamRetrieved = false;
    public BufferQueue(int maxBuffers)
    {
        super(maxBuffers);
    }
    
    public BufferQueueInputStream getInputStream()
    {
        if (inputStreamRetrieved)
            return null;
        inputStreamRetrieved = true;
        return new BufferQueueInputStream(this);
    }
    
    public BufferQueueOutputStream getOutputStream()
    {
        if (outputStreamRetrieved)
            return null;
        outputStreamRetrieved = true;
        return new BufferQueueOutputStream(this);
    }
    
    protected void addBuffer(byte [] buffer)
    {
        byte [] copyBuffer = new byte[buffer.length];
        System.arraycopy(buffer, 0, copyBuffer, 0, buffer.length);
        addObject(copyBuffer);
    }
    
    protected void addBuffer(byte [] buffer, int offset, int len)
    {
        byte [] copyBuffer = new byte[len];
        System.arraycopy(buffer, offset, copyBuffer, 0, len);
        addObject(copyBuffer);
    }
    
    protected void addBufferNoCopy(byte [] buffer)
    {
        addObject(buffer);
    }
    
    protected byte [] getBuffer() throws InterruptedException, BuffersDroppedException
    {
        return getObject();
    }
}
