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


public class ClientFileMetaDataCompareAttr implements BitMaskAttr 
{
    public static final ClientFileMetaDataCompareAttr kFileLength = new ClientFileMetaDataCompareAttr(0x000000001);
    public static final ClientFileMetaDataCompareAttr kCreateTime = new ClientFileMetaDataCompareAttr(0x000000002);
    public static final ClientFileMetaDataCompareAttr kModifyTime = new ClientFileMetaDataCompareAttr(0x000000004);
    public static final ClientFileMetaDataCompareAttr kOwner = new ClientFileMetaDataCompareAttr(0x00000008);
    public static final ClientFileMetaDataCompareAttr kFileType = new ClientFileMetaDataCompareAttr(0x00000010);
    public static final ClientFileMetaDataCompareAttr kAllAttrs = new ClientFileMetaDataCompareAttr(0x0fffffff);
    
    private int attrBitMask;

    protected ClientFileMetaDataCompareAttr(int attrBitMask)
    {
        this.attrBitMask = attrBitMask;
    }

    public int getAttrBitMask()
    {
        return attrBitMask;
    }
}
