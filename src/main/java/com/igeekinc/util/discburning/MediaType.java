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
 
package com.igeekinc.util.discburning;

import java.io.Serializable;

public class MediaType implements Serializable
{
    private static final long serialVersionUID = -4461227725576646194L;
    public static final int kMediaTypeCDRInt = 0;
    public static final int kMediaTypeCDROMInt = 1;
    public static final int kMediaTypeCDRWInt = 2;
    public static final int kMediaTypeDVDPlusRInt = 3;
    public static final int kMediaTypeDVDPlusRDoubleLayerInt = 4;
    public static final int kMediaTypeDVDPlusRWInt = 5;
    public static final int kMediaTypeDVDRInt = 6;
    public static final int kMediaTypeDVDRAMInt = 7;
    public static final int kMediaTypeDVDROMInt = 8;
    public static final int kMediaTypeDVDRWInt = 9;
    public static final int kMediaTypeNoneInt = 10;
    public static final int kMediaTypeUnknownInt = 11;
    
    public static final MediaType kMediaTypeCDR = new MediaType(kMediaTypeCDRInt);
    public static final MediaType kMediaTypeCDROM = new MediaType(kMediaTypeCDROMInt);
    public static final MediaType kMediaTypeCDRW = new MediaType(kMediaTypeCDRWInt);
    public static final MediaType kMediaTypeDVDPlusR = new MediaType(kMediaTypeDVDPlusRInt);
    public static final MediaType kMediaTypeDVDPlusRDoubleLayer = new MediaType(kMediaTypeDVDPlusRDoubleLayerInt);
    public static final MediaType kMediaTypeDVDPlusRW = new MediaType(kMediaTypeDVDPlusRWInt);
    public static final MediaType kMediaTypeDVDR = new MediaType(kMediaTypeDVDRInt);
    public static final MediaType kMediaTypeDVDRAM = new MediaType(kMediaTypeDVDRAMInt);
    public static final MediaType kMediaTypeDVDROM = new MediaType(kMediaTypeDVDROMInt);
    public static final MediaType kMediaTypeDVDRW = new MediaType(kMediaTypeDVDRWInt);
    public static final MediaType kMediaTypeNone = new MediaType(kMediaTypeNoneInt);
    public static final MediaType kMediaTypeUnknown = new MediaType(kMediaTypeUnknownInt);
    private int mediaType;
    
    private MediaType(int inMediaType)
    {
        mediaType = inMediaType;
    }
    
    public int getMediaType()
    {
        return mediaType;
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + mediaType;
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MediaType other = (MediaType) obj;
        if (mediaType != other.mediaType)
            return false;
        return true;
    }
    
    public String toString()
    {
        switch(mediaType)
        {
        case kMediaTypeCDRInt:
            return "CDR";
        case kMediaTypeCDROMInt:
            return "CDROM";
        case kMediaTypeCDRWInt:
            return "CDRW";
        case kMediaTypeDVDPlusRInt:
            return "DVD+R";
        case kMediaTypeDVDPlusRDoubleLayerInt:
            return "DVD+RDL";
        case kMediaTypeDVDPlusRWInt:
            return "DVD+RW";
        case kMediaTypeDVDRInt:
            return "DVDR";
        case kMediaTypeDVDRAMInt:
            return "DVD RAM";
        case kMediaTypeDVDROMInt:
            return "DVDROM";
        case kMediaTypeDVDRWInt:
            return "DVDRW";
        case kMediaTypeNoneInt:
            return "None";
        case kMediaTypeUnknownInt:
            return "Unknown";
        }
        return ("Unknown media type = "+mediaType);
    }
    
    public boolean isCDMedia()
    {
        switch(mediaType)
        {
        case kMediaTypeCDRInt:
            return true;
        case kMediaTypeCDROMInt:
            return true;
        case kMediaTypeCDRWInt:
            return true;
        case kMediaTypeDVDPlusRInt:
            return false;
        case kMediaTypeDVDPlusRDoubleLayerInt:
            return false;
        case kMediaTypeDVDPlusRWInt:
            return false;
        case kMediaTypeDVDRInt:
            return false;
        case kMediaTypeDVDRAMInt:
            return false;
        case kMediaTypeDVDROMInt:
            return false;
        case kMediaTypeDVDRWInt:
            return false;
        case kMediaTypeNoneInt:
            return false;
        case kMediaTypeUnknownInt:
            return false;
        }
        return false;
    }
    
    public boolean isDVDMedia()
    {
        switch(mediaType)
        {
        case kMediaTypeCDRInt:
            return false;
        case kMediaTypeCDROMInt:
            return false;
        case kMediaTypeCDRWInt:
            return false;
        case kMediaTypeDVDPlusRInt:
            return true;
        case kMediaTypeDVDPlusRDoubleLayerInt:
            return true;
        case kMediaTypeDVDPlusRWInt:
            return true;
        case kMediaTypeDVDRInt:
            return true;
        case kMediaTypeDVDRAMInt:
            return true;
        case kMediaTypeDVDROMInt:
            return true;
        case kMediaTypeDVDRWInt:
            return true;
        case kMediaTypeNoneInt:
            return false;
        case kMediaTypeUnknownInt:
            return false;
        }
        return false;
    }
    
    public boolean isWritable()
    {
        switch(mediaType)
        {
        case kMediaTypeCDRInt:
            return true;
        case kMediaTypeCDROMInt:
            return false;
        case kMediaTypeCDRWInt:
            return true;
        case kMediaTypeDVDPlusRInt:
            return true;
        case kMediaTypeDVDPlusRDoubleLayerInt:
            return true;
        case kMediaTypeDVDPlusRWInt:
            return true;
        case kMediaTypeDVDRInt:
            return true;
        case kMediaTypeDVDRAMInt:
            return true;
        case kMediaTypeDVDROMInt:
            return false;
        case kMediaTypeDVDRWInt:
            return true;
        case kMediaTypeNoneInt:
            return false;
        case kMediaTypeUnknownInt:
            return false;
        }
        return false;
    }
}
