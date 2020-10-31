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

public class SplitFileSegmentHeader
{
    private int segmentNumber;
    private long createTime;
    private byte [] nameBytes;
    private String name;
    
    private static final byte [] magicNumber = {0x49, 0x6E, 0x53, 0x48};
    
    public static final int kMagicNumberOffset = 0;
    public static final int kMagicNumberLength = 4;
    public static final int kSegmentNumberOffset = kMagicNumberOffset + kMagicNumberLength;
    public static final int kSegmentNumberLength = 4;
    public static final int kCreateTimeOffset = kSegmentNumberOffset + kSegmentNumberLength;
    public static final int kCreateTimeLength = 8;
    public static final int kNameLengthOffset = kCreateTimeOffset + kCreateTimeLength;
    public static final int kNameBytesOffset = 256;
    public static final int kNameBytesLength = 256;
    public static final int kSplitFileSegmentHeaderLength = kNameBytesOffset + kNameBytesLength;
    
    public SplitFileSegmentHeader(int segmentNumber, long createTime, String name)
    {
        this.segmentNumber = segmentNumber;
        this.createTime = createTime;
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
    
    public SplitFileSegmentHeader(byte [] inBytes)
    {
        if (inBytes.length != kSplitFileSegmentHeaderLength)
            throw new IllegalArgumentException("Expected "+kSplitFileSegmentHeaderLength+" bytes, got "+inBytes.length+" instead");
        if (inBytes[0] != magicNumber[0] || inBytes[1] != magicNumber[1] || inBytes[2] != magicNumber[2] || inBytes[3] != magicNumber[3])
            throw new IllegalArgumentException("Magic number corrupted");
        segmentNumber = BitTwiddle.javaByteArrayToInt(inBytes, kSegmentNumberOffset);
        createTime = BitTwiddle.javaByteArrayToLong(inBytes, kCreateTimeOffset);
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
        BitTwiddle.intToJavaByteArray(segmentNumber, returnBytes, kSegmentNumberOffset);
        BitTwiddle.longToJavaByteArray(createTime, returnBytes, kCreateTimeOffset);
        returnBytes[kNameLengthOffset] = (byte)nameBytes.length;
        
        System.arraycopy(nameBytes, 0, returnBytes, kNameBytesOffset, nameBytes.length);
        return returnBytes;
    }

    public String getName()
    {
        return name;
    }

    public int getSegmentNumber()
    {
        return segmentNumber;
    }

    public long getCreateTime()
    {
        return createTime;
    }

}
