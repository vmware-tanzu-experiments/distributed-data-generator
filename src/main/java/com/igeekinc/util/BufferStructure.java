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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class BufferStructure
{
	protected int baseOffset, length;
	protected byte [] buffer;
	protected EndianType endianType;
	
	protected BufferStructure(byte [] buffer)
	{
		this(buffer, 0, buffer.length, BitTwiddle.nativeEndianType);
	}
	
	protected BufferStructure(byte [] buffer, int baseOffset, int length)
	{
		this(buffer, baseOffset, length, BitTwiddle.nativeEndianType);
	}
	
	protected BufferStructure(byte [] buffer, int baseOffset, int length, EndianType endianType)
	{
		this.buffer = buffer;
		this.baseOffset = baseOffset;
		this.length = length;
		this.endianType = endianType;
	}
	
	protected short getShortAtOffset(int offset)
	{
		return BitTwiddle.byteArrayToShort(buffer, baseOffset + offset, endianType);
	}
	
	protected int getIntAtOffset(int offset)
	{
		return BitTwiddle.byteArrayToInt(buffer, baseOffset + offset, endianType);
	}
	
	protected long getLongAtOffset(int offset)
	{
		return BitTwiddle.byteArrayToLong(buffer, baseOffset + offset, endianType);
	}
	
	protected String getUTF8StringAtOffset(int offset, int maxLength)
	{
		return BitTwiddle.nullTerminatedUTF8ByteArrayToString(buffer, baseOffset + offset, maxLength);
	}
	
	protected void setShortAtOffset(short value, int offset)
	{
		BitTwiddle.shortToByteArray(value, buffer, offset, endianType);
	}
	
	protected void setIntAtOffset(int value, int offset)
	{
		BitTwiddle.intToByteArray(value, buffer, offset, endianType);
	}
	
	protected void setLongAtOffset(long value, int offset)
	{
		BitTwiddle.longToByteArray(value, buffer, offset, endianType);
	}
	
	protected void setUTF8StringAtOffset(String value, int offset, int maxLength)
	{
		byte[] utf8Bytes;
		try
		{
			utf8Bytes = value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			throw new InternalError("Got unsupported encoding exception");
		}
		maxLength = maxLength - 1;	// Leave room for the terminating NULL
		int bytesToCopy = utf8Bytes.length > maxLength ? maxLength : utf8Bytes.length;
		System.arraycopy(utf8Bytes, 0, buffer, offset, bytesToCopy);
		buffer[offset + bytesToCopy] = 0;	// NULL terminate!
		
	}
	
	protected ByteBuffer getBuffer()
	{
		return getBuffer(0, length);
	}
	
	protected ByteBuffer getBuffer(int offset, int getLength)
	{
		return ByteBuffer.wrap(buffer, baseOffset + offset, getLength);
	}
	
	protected ByteBuffer getRemainderBuffer(int offset)
	{
		return getBuffer(offset, length - offset);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + baseOffset;
		result = prime * result + Arrays.hashCode(buffer);
		result = prime * result
				+ ((endianType == null) ? 0 : endianType.hashCode());
		result = prime * result + length;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BufferStructure other = (BufferStructure) obj;
		if (baseOffset != other.baseOffset)
			return false;
		if (endianType == null)
		{
			if (other.endianType != null)
				return false;
		} else if (!endianType.equals(other.endianType))
			return false;
		if (length != other.length)
			return false;
		if (!Arrays.equals(buffer, other.buffer))
			return false;

		return true;
	}
}
