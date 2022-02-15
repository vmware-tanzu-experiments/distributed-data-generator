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

import com.igeekinc.util.exceptions.ForkNotFoundException;
import com.igeekinc.util.exceptions.UserNotFoundException;
import com.igeekinc.util.msgpack.ClientFileMetaDataMsgPack;
import java.io.Serializable;
import java.util.Date;


public abstract class ClientFileMetaData implements Cloneable, Serializable
{
    private static final long serialVersionUID = 3309617452494199009L;
    protected static Class<?> xmlParserClass;
    protected static Class<?> xmlSerializerClass;

    public ClientFileMetaData()
    {
    }

    public abstract long getFileLength();
    
    /**
     * Gets the space actually allocated to the file (e.g. rounded up to 4K block, etc.)
     * @return
     */
    public long getSpaceUsed()
    {
        return getFileLength();
    }
    public abstract long getForkLength(String forkName) throws ForkNotFoundException;
    public abstract Date getCreateTime();
    public abstract Date getModifyTime();
    public abstract void setOwner(User userID) throws UserNotFoundException;
    public abstract void setOwnerReadOnly();
    public abstract User getOwner();
    public abstract boolean isOwnerReadOnly();
    public abstract int getFileType();
    public abstract void setFileType(int fileType);
    public abstract boolean isDirectory();
    
    @Override
    public ClientFileMetaData clone()
    {
        try {
            return (ClientFileMetaData)super.clone();
        } catch (CloneNotSupportedException e) {
            //TODO Auto-generated catch block
            org.apache.logging.log4j.Logger exceptionLogger = org.apache.logging.log4j.LogManager.getLogger(this.getClass());
            exceptionLogger.error("Caught exception CloneNotSupportedException", e);
            return null;
        }
    }
    public static Class<?> getXMLParserClass()
    {
        return xmlParserClass;
    }

    public static Class<?> getXMLSerializerClass()
    {
        return xmlSerializerClass;
    }
    
    public abstract ClientFileMetaDataProperties getProperties();
    
    public abstract boolean looseEquals(ClientFileMetaData checkMD);
    
    public boolean compare(ClientFileMetaData checkMD, BitMask<ClientFileMetaDataCompareAttr>compareAttrs)
    {
        if (compareAttrs.isSet(ClientFileMetaDataCompareAttr.kModifyTime) &&
                !checkMD.getModifyTime().equals(getModifyTime()))
            return false;
        if (compareAttrs.isSet(ClientFileMetaDataCompareAttr.kFileLength) &&
                checkMD.getFileLength() != getFileLength())
            return false;
        if (compareAttrs.isSet(ClientFileMetaDataCompareAttr.kFileType) &&
                checkMD.getFileType() != getFileType())
            return false;
        if (compareAttrs.isSet(ClientFileMetaDataCompareAttr.kOwner) &&
                !checkMD.getOwner().equals(getOwner()))
            return false;
        return true;
            
    }

	public abstract boolean isSymlink();
	
	public abstract String getSymlinkTarget();

	public abstract boolean isRegularFile();
	
	public boolean isSocket()
	{
		return false;
	}
	
	public boolean isPipe()
	{
		return false;
	}
	
	/*
	 * Get the MessagePack object for this meta data
	 */
	public abstract ClientFileMetaDataMsgPack getMDMsgPack();
}
