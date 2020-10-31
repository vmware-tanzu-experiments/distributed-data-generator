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

import java.io.UnsupportedEncodingException;

import com.igeekinc.util.BitTwiddle;

public class SplitFileForkSegmentTrailer
{
    private long segmentLength;
    private long forkHeaderOffset;
    private long previousForkTrailerOffset;
    private long forkTotalLength;
    private boolean finalSegment;
    private byte [] nameBytes;
    private String name;
    
    private static final byte [] magicNumber = {0x49, 0x6E, 0x46, 0x54};
    
    public static final int kMagicNumberOffset = 0;
    public static final int kMagicNumberLength = 4;
    public static final int kSegmentLengthOffset = kMagicNumberOffset + kMagicNumberLength;
    public static final int kSegmentLengthLength = 8;
    public static final int kForkHeaderOffset = kSegmentLengthOffset + kSegmentLengthLength;
    public static final int kForkHeaderLength = 8;
    public static final int kPreviousForkTrailerOffset = kForkHeaderOffset + kForkHeaderLength;
    public static final int kPreviousForkTrailerLength = 8;
    public static final int kForkTotalLengthOffset = kPreviousForkTrailerOffset + kPreviousForkTrailerLength;
    public static final int kForkTotalLengthLength = 8;
    public static final int kFinalSegmentFlagOffset = kForkTotalLengthOffset + kForkTotalLengthLength;
    public static final int kFinalSegmentFlagLength = 1;
    public static final int kNameLengthOffset = kFinalSegmentFlagOffset + kFinalSegmentFlagLength;
    public static final int kNameBytesOffset = 256;
    public static final int kNameBytesLength = 256;
    public static final int kSplitFileForkTrailerLength = kNameBytesOffset + kNameBytesLength;
    
    public SplitFileForkSegmentTrailer(long segmentLength, long forkHeaderOffset, long previousForkTrailerOffset,
            long forkTotalLength, boolean finalSegment, String name)
    {
        this.segmentLength = segmentLength;
        this.forkHeaderOffset = forkHeaderOffset;
        this.previousForkTrailerOffset = previousForkTrailerOffset;
        this.forkTotalLength = forkTotalLength;
        this.finalSegment = finalSegment;
        try
        {
            nameBytes = name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            throw new InternalError("Can't find UTF-8 encoding");
        }
        if (nameBytes.length > 255)
            throw new IllegalArgumentException("Max name length is 255 UTF-8 bytes ('"+name+"' = "+nameBytes.length+")");
        this.name = name;
    }
    
    public SplitFileForkSegmentTrailer(byte [] inBytes)
    {
        if (inBytes.length != kSplitFileForkTrailerLength)
            throw new IllegalArgumentException("Expected "+kSplitFileForkTrailerLength+" bytes, got "+inBytes.length+" instead");
        if (inBytes[0] != magicNumber[0] || inBytes[1] != magicNumber[1] || inBytes[2] != magicNumber[2] || inBytes[3] != magicNumber[3])
            throw new IllegalArgumentException("Magic number corrupted");
        segmentLength = BitTwiddle.javaByteArrayToLong(inBytes, kSegmentLengthOffset);
        forkHeaderOffset = BitTwiddle.javaByteArrayToLong(inBytes, kForkHeaderOffset);
        previousForkTrailerOffset = BitTwiddle.javaByteArrayToLong(inBytes, kPreviousForkTrailerOffset);
        forkTotalLength = BitTwiddle.javaByteArrayToLong(inBytes, kForkTotalLengthOffset);
        if (inBytes[kFinalSegmentFlagOffset] != 0)
            finalSegment = true;
        else
            finalSegment = false;
        int nameLength = ((int)inBytes[kNameLengthOffset]) & 0xff;
        try
        {
            name = new String(inBytes, kNameBytesOffset, nameLength, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            throw new InternalError("Can't find UTF-8 encoding");
        }
    }
    
    public byte [] toByteArray()
    {
        byte [] returnBytes = new byte[512];
        System.arraycopy(magicNumber, 0, returnBytes, kMagicNumberOffset, kMagicNumberLength);
        BitTwiddle.longToJavaByteArray(segmentLength, returnBytes, kSegmentLengthOffset);
        BitTwiddle.longToJavaByteArray(forkHeaderOffset, returnBytes, kForkHeaderOffset);
        BitTwiddle.longToJavaByteArray(previousForkTrailerOffset, returnBytes, kPreviousForkTrailerOffset);
        BitTwiddle.longToJavaByteArray(forkTotalLength, returnBytes, kForkTotalLengthOffset);
        if (finalSegment)
            returnBytes[kFinalSegmentFlagOffset] = 1;
        else
            returnBytes[kFinalSegmentFlagOffset] = 0;
        returnBytes[kNameLengthOffset] = (byte)nameBytes.length;
        System.arraycopy(nameBytes, 0, returnBytes, kNameBytesOffset, nameBytes.length);
        return returnBytes;
    }

    public String getName()
    {
        return name;
    }

    public long getForkTotalLength()
    {
        return forkTotalLength;
    }

    public long getSegmentLength()
    {
        return segmentLength;
    }

    public long getForkHeaderOffset()
    {
        return forkHeaderOffset;
    }

    public long getPreviousForkTrailerOffset()
    {
        return previousForkTrailerOffset;
    }

    public boolean isFinalSegment()
    {
        return finalSegment;
    }
}
