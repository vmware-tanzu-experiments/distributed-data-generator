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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

class ForkInfo
{
    long dataStartOffset;
    long dataEndOffset;
    SplitFileForkSegmentHeader header;
    SplitFileForkSegmentTrailer trailer;
}
public class SplitFileSegmentInputStream extends InputStream
{
    protected SplitFileSegmentHeader segmentHeader;
    protected SplitFileSegmentTrailer segmentTrailer;
    protected RandomAccessFile segmentReader;
    protected ArrayList<ForkInfo> forkInfoList;
    protected int curForkNum;
    protected ForkInfo curForkInfo;
    
    public SplitFileSegmentInputStream(File segmentFile) throws IOException
    {
        forkInfoList = new ArrayList<ForkInfo>();
        segmentReader = new RandomAccessFile(segmentFile, "r");
        byte [] segmentHeaderBytes = new byte[SplitFileSegmentTrailer.kSplitFileSegmentTrailerLength];
        segmentReader.seek(0);
        if (segmentReader.read(segmentHeaderBytes) != SplitFileSegmentTrailer.kSplitFileSegmentTrailerLength)
            throw new IOException("Got back short read reading header");
        segmentHeader = new SplitFileSegmentHeader(segmentHeaderBytes);
        byte [] segmentTrailerBytes = new byte[SplitFileSegmentTrailer.kSplitFileSegmentTrailerLength];
        segmentReader.seek(segmentReader.length() - SplitFileSegmentTrailer.kSplitFileSegmentTrailerLength);
        if (segmentReader.read(segmentTrailerBytes) != SplitFileSegmentTrailer.kSplitFileSegmentTrailerLength)
            throw new IOException("Got back short read reading trailer");
        segmentTrailer = new SplitFileSegmentTrailer(segmentTrailerBytes);
        
        long curForkTrailerOffset = segmentTrailer.getOffsetOfLastForkTrailer();
        while (curForkTrailerOffset >= 0)
        {
            segmentReader.seek(curForkTrailerOffset);
            byte [] curForkTrailerBytes = new byte[SplitFileForkSegmentTrailer.kSplitFileForkTrailerLength];
            if (segmentReader.read(curForkTrailerBytes) != SplitFileForkSegmentTrailer.kSplitFileForkTrailerLength)
                throw new IOException("Got back short read reading fork trailer");
            
            SplitFileForkSegmentTrailer curForkTrailer = new SplitFileForkSegmentTrailer(curForkTrailerBytes);
            long curForkHeaderOffset = curForkTrailer.getForkHeaderOffset();
            byte [] curForkHeaderBytes = new byte[SplitFileForkSegmentHeader.kSplitFileForkHeaderLength];
            segmentReader.seek(curForkHeaderOffset);
            if (segmentReader.read(curForkHeaderBytes) != SplitFileForkSegmentHeader.kSplitFileForkHeaderLength)
                throw new IOException("Got back short read reading fork header");
            SplitFileForkSegmentHeader curForkHeader = new SplitFileForkSegmentHeader(curForkHeaderBytes);
            ForkInfo newForkInfo = new ForkInfo();
            newForkInfo.dataStartOffset = curForkHeaderOffset+SplitFileForkSegmentHeader.kSplitFileForkHeaderLength;
            newForkInfo.dataEndOffset = newForkInfo.dataStartOffset + curForkTrailer.getSegmentLength();
            newForkInfo.header = curForkHeader;
            newForkInfo.trailer = curForkTrailer;
            forkInfoList.add(0, newForkInfo);
            curForkTrailerOffset = curForkTrailer.getPreviousForkTrailerOffset();   // work our way back to the first trailer
        }
        if (forkInfoList.size() == 0)
            throw new IOException("No forks found in segment file");
        curForkNum = -1;
    }
    
    public String getNextFork() throws IOException
    {
        curForkNum++;
        if (curForkNum >= forkInfoList.size())
            return null;
        curForkInfo = (ForkInfo)forkInfoList.get(curForkNum);
        segmentReader.seek(curForkInfo.dataStartOffset);
        
        return curForkInfo.header.getName();
    }
    
    public String peekNextFork()
    {
        int peekForkNum = curForkNum + 1;
        if (peekForkNum >= forkInfoList.size())
            return null;
        return ((ForkInfo)forkInfoList.get(peekForkNum)).header.getName();
    }
    public int read() throws IOException
    {
        if (segmentReader.getFilePointer() < curForkInfo.dataEndOffset)
            return segmentReader.read();
        else
            return -1;
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
        long filePointer = segmentReader.getFilePointer();
        if (filePointer < curForkInfo.dataEndOffset)
        {
            if (filePointer + len > curForkInfo.dataEndOffset)
                len = (int)(curForkInfo.dataEndOffset - filePointer);
            return segmentReader.read(b, off, len);
        }
        else
            return -1;
    }

    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }
    
    public boolean hasMoreForks()
    {
        return (curForkNum < forkInfoList.size() - 1);
    }

    public void close() throws IOException
    {
        super.close();
        segmentReader.close();
    }
}
