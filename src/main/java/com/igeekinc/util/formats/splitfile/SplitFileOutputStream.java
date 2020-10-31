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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SplitFileOutputStream extends OutputStream
{
    private long createTime;
    private String name;
    private SplitFileDescriptor fileDescriptor;
    private SplitFileSegmentOutputStream curSegmentOS;
    private SplitFileSegmentInfo curSegmentInfo;
    private long totalOffset, curSegmentOffset;
    private int curSegmentNum;
    
    private String forkName;
    private boolean forkOpen;
    private long offsetInFork;
    public SplitFileOutputStream(long createTime, String name, SplitFileDescriptor fileDescriptor)
    throws IOException
    {
        this.createTime = createTime;
        this.name = name;
        this.fileDescriptor = fileDescriptor;
        totalOffset = 0;
        curSegmentNum = -1;
        forkOpen = false;
        forkName = "";
        moveToNextSegment();
    }
    
    private void moveToNextSegment() throws IOException, FileNotFoundException
    {
        if (curSegmentOS != null)
        {
            if (forkOpen)
                curSegmentOS.closeFork(0, false);
            curSegmentOS.close(false);
            curSegmentOS = null;
        }
        
        if (curSegmentInfo != null)
        {
            curSegmentInfo.setSegmentLength(curSegmentOffset);
            fileDescriptor.segmentFinished(curSegmentInfo);
            
        }
        curSegmentInfo = fileDescriptor.next();

        curSegmentNum++;
        curSegmentOS = new SplitFileSegmentOutputStream(new FileOutputStream(curSegmentInfo.getSegmentFile()), curSegmentNum, createTime, name);
        curSegmentOffset = 0;
        if (forkOpen)
        {
            curSegmentOS.startFork(offsetInFork, forkName);
        }
    }
    
    public void write(int b) throws IOException
    {
        byte [] buf = new byte[1];
        buf[0] = (byte)b;
        write(buf, 0, 1);
    }
    
    public void write(byte [] b)
    throws IOException
    {
        write(b, 0, b.length);
    }
    
    public void write(byte[] b,
            int off,
            int len)
    throws IOException
    {
        if (curSegmentOS != null && curSegmentInfo.getSegmentLength() < 0)
        {
            curSegmentOS.write(b, off, len);
            curSegmentOffset += len;
            offsetInFork += len;
        }
        else
        {
            int bytesWritten = 0;
            while (curSegmentOS != null && bytesWritten < len)
            {
                // Note - "-1" is an infinite length segment so we will never
                // have our offset == -1 so we will go on writing to it indefinitely
                if (curSegmentOffset == curSegmentInfo.getSegmentLength())
                {
                    if (fileDescriptor.hasNext())
                    {
                        moveToNextSegment();
                    }
                    else
                    {
                        close();    // close things up
                        if (bytesWritten < len)
                            throw new IOException("No more segments to write to");
                        break;
                    }
                }
                int bytesToWrite = len-bytesWritten;
                long segmentBytesRemaining = curSegmentInfo.getSegmentLength() - curSegmentOffset;
                if (bytesToWrite > segmentBytesRemaining)
                    bytesToWrite = (int)segmentBytesRemaining;
                curSegmentOS.write(b, bytesWritten, bytesToWrite);
                curSegmentOffset += bytesToWrite;
                offsetInFork += bytesToWrite;
                bytesWritten += bytesToWrite;
            }
        }
    }
    
    public void startFork(String forkName) throws IOException
    {
        offsetInFork = 0;
        this.forkName = forkName;
        forkOpen = true;
        curSegmentOS.startFork(offsetInFork, forkName);
    }
    
    public void closeFork() throws IOException
    {
        if (curSegmentOS != null)
            curSegmentOS.closeFork(offsetInFork, true);
        forkOpen = false;
    }
    
    public void close()
    throws IOException
    {
        if (curSegmentOS != null)
        {
            if (forkOpen)
            {
                curSegmentOS.closeFork(offsetInFork, true);
                forkOpen = false;
            }
            curSegmentOS.close(true);
            curSegmentOS = null;
        }
        if (curSegmentInfo != null)
        {
            curSegmentInfo.setSegmentLength(curSegmentOffset);
            fileDescriptor.segmentFinished(curSegmentInfo);
            curSegmentInfo = null;
        }
    }
    
    public void flush() throws IOException
    {
        if (curSegmentOS != null)
            curSegmentOS.flush();
        else
            throw new IOException("Can't flush closed stream!");
    }
}
