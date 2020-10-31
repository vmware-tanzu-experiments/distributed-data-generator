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
import java.io.IOException;
import java.io.InputStream;

public class SplitFileInputStream extends InputStream
{
    private SplitFileDescriptor fileDescriptor;

    // curSegmentIS will always be set to an input stream unless the stream has been closed or
    // EOF has been hit
    private SplitFileSegmentInputStream curSegmentIS;
    private SplitFileSegmentInfo curSegmentInfo;
    private String curForkName;
    
    public SplitFileInputStream(SplitFileDescriptor fileDescriptor)
    throws IOException
    {

        this.fileDescriptor = fileDescriptor;
        moveToNextSegment();
    }

    private void moveToNextSegment() throws IOException, FileNotFoundException
    {
        if (curSegmentIS != null)
        {
            curSegmentIS.close();
            curSegmentIS = null;
        }
        curSegmentInfo = fileDescriptor.next();
        curSegmentIS = new SplitFileSegmentInputStream(curSegmentInfo.getSegmentFile());
    }

    public int read() throws IOException
    {
        byte [] buf = new byte[1];
        int result = read(buf, 0, 1);
        if (result != 1)
            return -1;
        return buf[0] & 0xff;
    }

    public int read(byte[] b)
    throws IOException
    {
        return read(b, 0, b.length);
    }
    
    public int read(byte[] b,
            int off,
            int len)
     throws IOException
     {
        int bytesRead = 0;
        boolean errorSeen = false;
        while (curSegmentIS != null && bytesRead < len)
        {
            int bytesToRead = len-bytesRead;
            int bytesReadNow = curSegmentIS.read(b, off + bytesRead, bytesToRead);
            if (bytesReadNow < 0)
            {
                bytesReadNow = 0;   // Clear out an error return
                errorSeen = true;
            }
            bytesRead += bytesReadNow;
            if (bytesReadNow < bytesToRead)
            {
                if (curSegmentIS.hasMoreForks())
                    break;  // End of the fork, don't move to the next segment
                if (fileDescriptor.hasNext())
                {
                    moveToNextSegment();
                    if (!curSegmentIS.peekNextFork().equals(curForkName))
                        break;  // No more for this fork in the next segment file
                    curSegmentIS.getNextFork();
                }
                else
                {
                    close();    // close things up
                    break;
                }

            }
        }
        if (errorSeen && bytesRead == 0)
            return -1;
        return bytesRead;
     }

    public void close() throws IOException
    {
        if (curSegmentIS != null)
        {
            curSegmentIS.close();
            curSegmentIS = null;
        }
    }

    
    public String getNextFork() throws IOException
    {
        String newForkName;
        if (curSegmentIS == null)
            return null;
        do
        {
            newForkName = curSegmentIS.getNextFork();
            if (newForkName == null)
            {
                moveToNextSegment();
                if (curSegmentIS == null)
                    return null;    // No more forks
                newForkName = curSegmentIS.getNextFork();
            }
        } while (newForkName != null && newForkName.equals(curForkName));
        curForkName = newForkName;
        return curForkName;
    }
}
