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

public class BufferQueueInputStream extends InputStream
{
    private BufferQueue bufferQueue;
    private byte [] currentBuffer;
    private int currentOffset;
    private boolean eof;
    
    protected BufferQueueInputStream(BufferQueue bufferQueue)
    {
        this.bufferQueue = bufferQueue;
        currentBuffer = null;
        currentOffset = 0;
        eof = false;
    }
    public int read() throws IOException
    {
        if (eof)
            return -1;
        if (currentBuffer == null)
        {
            try
            {
                currentBuffer = bufferQueue.getBuffer();
                if (currentBuffer == null)
                {
                    eof = true;
                    return -1;
                }
                currentOffset = 0;
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        int returnByte = ((int)currentBuffer[currentOffset]) & 0xff;
        currentOffset ++;
        if (currentOffset == currentBuffer.length)
            currentBuffer = null;
        return returnByte;
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (eof)
            return -1;
        int outputOffset, remainingToOutput;
        outputOffset = 0;
        remainingToOutput = len;
        while (remainingToOutput > 0)
        {
            if (currentBuffer == null)
            {
                try
                {
                    currentBuffer = bufferQueue.getBuffer();
                    if (currentBuffer == null)
                    {
                        eof = true;
                        break;  // End of the line pilgrim
                    }
                    currentOffset = 0;
                } catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            int bytesToCopy;
            if (remainingToOutput < currentBuffer.length - currentOffset)
                bytesToCopy = remainingToOutput;
            else
                bytesToCopy = currentBuffer.length - currentOffset;
            System.arraycopy(currentBuffer, currentOffset,  b, outputOffset, bytesToCopy);
            currentOffset += bytesToCopy;
            if (currentOffset == currentBuffer.length)
                currentBuffer = null;
            outputOffset += bytesToCopy;
            remainingToOutput -= bytesToCopy;
        }
        return outputOffset;
    }
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
}
