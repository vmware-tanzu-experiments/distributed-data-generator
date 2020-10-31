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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SplitFileSegmentOutputStream extends FilterOutputStream
{
    protected long createTime;
    protected int segmentNumber;
    protected String name;
    protected boolean firstForkSeen, forkOpen;
    protected String forkName;
    protected long forkSegmentLength, segmentOffset;
    protected long lastForkHeaderOffset, lastForkTrailerOffset;
    
    public SplitFileSegmentOutputStream(OutputStream outStream, int segmentNumber, long createTime, String name) throws IOException
    {
        super(outStream);
        this.createTime = createTime;
        this.segmentNumber = segmentNumber;
        this.name = name;
        forkName = "";
        firstForkSeen = false;
        segmentOffset = 0;
        SplitFileSegmentHeader segmentHeader = new SplitFileSegmentHeader(segmentNumber, createTime, name);
        byte[] segmentHeaderBytes = segmentHeader.toByteArray();
        out.write(segmentHeaderBytes);
        segmentOffset += segmentHeaderBytes.length;
        lastForkTrailerOffset = -1;
    }
    
    public void startFork(long offsetInFork, String forkName)
    throws IOException
    {
        if (offsetInFork != 0 && firstForkSeen)
            throw new IllegalArgumentException("Can only have a fork offset at the beginning of the segment");
        if (forkOpen)
            throw new IllegalArgumentException("Must close fork before opening a new one");
        this.forkName = forkName;

        lastForkHeaderOffset = segmentOffset;
        
        SplitFileForkSegmentHeader forkHeader = new SplitFileForkSegmentHeader(offsetInFork, forkName);
        byte[] forkHeaderBytes = forkHeader.toByteArray();
        out.write(forkHeaderBytes);
        segmentOffset += forkHeaderBytes.length;
        forkSegmentLength = 0;
        firstForkSeen = true;
        forkOpen = true;
    }
    
    public void closeFork(long totalForkLength, boolean finalSegment) throws IOException
    {
        SplitFileForkSegmentTrailer forkTrailer = new SplitFileForkSegmentTrailer(forkSegmentLength, lastForkHeaderOffset, lastForkTrailerOffset, totalForkLength, finalSegment, forkName);
        lastForkTrailerOffset = segmentOffset;
        byte [] forkTrailerBytes = forkTrailer.toByteArray();
        out.write(forkTrailerBytes);
        segmentOffset += forkTrailerBytes.length;
        forkOpen = false;
        
    }

    public void close(boolean finalSegment) throws IOException
    {
        if (forkOpen)
            throw new IOException("Must call closeFork before calling close");
        SplitFileSegmentTrailer segmentTrailer = new SplitFileSegmentTrailer(segmentNumber, createTime, lastForkTrailerOffset, finalSegment, name);
        out.write(segmentTrailer.toByteArray());    
        super.close();
    }

    public void close() throws IOException
    {
        close(true);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        if (!forkOpen)
            throw new IOException("No fork opened");
        out.write(b, off, len);
        forkSegmentLength += len;
        segmentOffset += len;
    }

    public void write(byte[] b) throws IOException
    {
        if (!forkOpen)
            throw new IOException("No fork opened");
        out.write(b);
        forkSegmentLength += b.length;
        segmentOffset += b.length;
    }

    public void write(int b) throws IOException
    {
        if (!forkOpen)
            throw new IOException("No fork opened");
        super.write(b);
        forkSegmentLength ++;
        segmentOffset ++;
    }
}
