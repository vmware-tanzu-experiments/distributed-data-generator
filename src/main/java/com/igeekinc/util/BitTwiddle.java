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

class EndianType
{
	EndianType()
	{
	}
}
public class BitTwiddle
{
	public static final EndianType kLittleEndian = new EndianType();
	public static final EndianType kBigEndian = new EndianType();
	
	static EndianType nativeEndianType;
	static
	{
		String arch = System.getProperty("os.arch");
		if (arch.equals("ppc"))
			nativeEndianType = kBigEndian;
		if (arch.equals("i386") || arch.equals("universal") || arch.equals("x86_64") || arch.equals("amd64"))
			nativeEndianType = kLittleEndian;
		if (nativeEndianType == null)
			throw new InternalError("Can't determine processor type");
	}
	static public void intToNativeByteArray(int in, byte [] out, int offset)
	{
		intToByteArray(in, out, offset, nativeEndianType);
	}
	
	static public void intToJavaByteArray(int in, byte [] out, int offset)
	{
		intToByteArray(in, out, offset, kBigEndian);
	}
	
	static public void intToByteArray(int in, byte [] out, int offset, EndianType endianType)
	{
		if (endianType == kLittleEndian)
		{
			out[offset] = (byte)(in & 0xff);
			out[offset + 1] = (byte)(in >> 8 & 0xff);
			out[offset + 2] = (byte)(in >> 16 & 0xff);
			out[offset + 3] = (byte)(in >> 24 & 0xff);
		}
		else
		{
			out[offset + 3] = (byte)(in & 0xff);
			out[offset + 2] = (byte)(in >> 8 & 0xff);
			out[offset + 1] = (byte)(in >> 16 & 0xff);
			out[offset] = (byte)(in >> 24 & 0xff);
		}
	}
	
	static public int nativeByteArrayToInt(byte [] in, int offset)
	{
        int retVal=0;
        if (nativeEndianType == kLittleEndian)
        {
            retVal = (((int)in[offset+3])&0xff) << 24;
            retVal |= (((int)in[offset+2])&0xff) << 16;
            retVal |= (((int)in[offset+1])&0xff) << 8;
            retVal |= ((int)in[offset])&0xff;
        }
        else
        {
            retVal = (((int)in[offset])&0xff) << 24;
            retVal |= (((int)in[offset+1])&0xff) << 16;
            retVal |= (((int)in[offset+2])&0xff) << 8;
            retVal |= ((int)in[offset+3])&0xff;
        }
        return retVal;
	}
	
	static public int javaByteArrayToInt(byte [] in, int offset)
	{
		return byteArrayToInt(in, offset, kBigEndian);
	}
	
	static public int byteArrayToInt(byte [] in, int offset, EndianType endianType)
	{
		int retVal=0;
		if (endianType == kLittleEndian)
		{
			retVal = (((int)in[offset+3])&0xff) << 24;
			retVal |= (((int)in[offset+2])&0xff) << 16;
			retVal |= (((int)in[offset+1])&0xff) << 8;
			retVal |= ((int)in[offset])&0xff;
		}
		else
		{
			retVal = (((int)in[offset])&0xff) << 24;
			retVal |= (((int)in[offset+1])&0xff) << 16;
			retVal |= (((int)in[offset+2])&0xff) << 8;
			retVal |= ((int)in[offset+3])&0xff;
		}
		return retVal;
	}
	
	static public void shortToNativeByteArray(short in, byte [] out, int offset)
	{
		shortToByteArray(in, out, offset, nativeEndianType);
	}
	
	static public void shortToJavaByteArray(short in, byte [] out, int offset)
	{
		shortToByteArray(in, out, offset, kBigEndian);
	}
	
	static public void shortToByteArray(short in, byte [] out, int offset, EndianType endianType)
	{
		if (endianType == kLittleEndian)
		{
			out[offset] = (byte)(in & 0xff);
			out[offset + 1] = (byte)(in >> 8 & 0xff);
		}
		else
		{
			out[offset + 1] = (byte)(in & 0xff);
			out[offset] = (byte)(in >> 8 & 0xff);
		}
	}
	
	static public short nativeByteArrayToShort(byte [] in, int offset)
	{
        short retVal=0;
        if (nativeEndianType == kLittleEndian)
        {
            retVal |= (((int)in[offset + 1])&0xff) << 8;
            retVal |= ((int)in[offset])&0xff;
        }
        else
        {
            retVal |= (((int)in[offset])&0xff) << 8;
            retVal |= ((int)in[offset+1])&0xff;
        }
        return retVal;
	}
	
	static public short javaByteArrayToShort(byte [] in, int offset)
	{
		return byteArrayToShort(in, offset, kBigEndian);
	}
	
	static public short byteArrayToShort(byte [] in, int offset, EndianType endianType)
	{
		short retVal=0;
		if (endianType == kLittleEndian)
		{
			retVal |= (((int)in[offset + 1])&0xff) << 8;
			retVal |= ((int)in[offset])&0xff;
		}
		else
		{
			retVal |= (((int)in[offset])&0xff) << 8;
			retVal |= ((int)in[offset+1])&0xff;
		}
		return retVal;
	}
	
	static public void longToNativeByteArray(long in, byte [] out, int offset)
	{
		longToByteArray(in, out, offset, nativeEndianType);
	}
	
	static public void longToJavaByteArray(long in, byte [] out, int offset)
	{
		longToByteArray(in, out, offset, kBigEndian);
	}
	
	static public void longToByteArray(long in, byte [] out, int offset, EndianType endianType)
	{
		if (endianType == kLittleEndian)
		{
			out[offset] = (byte)(in & 0xff);
			out[offset + 1] = (byte)(in >> 8 & 0xff);
			out[offset + 2] = (byte)(in >> 16 & 0xff);
			out[offset + 3] = (byte)(in >> 24 & 0xff);
			out[offset + 4] = (byte)(in >> 32 & 0xff);
			out[offset + 5] = (byte)(in >> 40 & 0xff);
			out[offset + 6] = (byte)(in >> 48 & 0xff);
			out[offset + 7] = (byte)(in >> 56 & 0xff);
		}
		else
		{
			// Stride forward through the array - better performance??
			out[offset] = (byte)(in >> 56 & 0xff);
			out[offset + 1] = (byte)((in >> 48) & 0xff);
			out[offset + 2] = (byte)((in >> 40) & 0xff);
			out[offset + 3] = (byte)((in >> 32) & 0xff);
			out[offset + 4] = (byte)((in >> 24) & 0xff);
			out[offset + 5] = (byte)((in >> 16) & 0xff);
			out[offset + 6] = (byte)((in >> 8) & 0xff);
			out[offset + 7] = (byte)(in & 0xff);
		}
	}
	
	static public long nativeByteArrayToLong(byte [] in, int offset)
	{
		return byteArrayToLong(in, offset, nativeEndianType);
	}
	
	static public long javaByteArrayToLong(byte [] in, int offset)
	{
		return byteArrayToLong(in, offset, kBigEndian);
	}
	
	static public long byteArrayToLong(byte [] in, int offset, EndianType endianType)
	{
		long retVal=0;
		if (endianType == kLittleEndian)
		{
			retVal = (long)(in[offset + 7] & 0xff) << 56;
			retVal |= (long)(in[offset + 6] & 0xff) << 48;
			retVal |= (long)(in[offset + 5] & 0xff) << 40;
			retVal |= (long)(in[offset + 4] & 0xff) << 32;
			retVal |= (long)(in[offset + 3] & 0xff) << 24;
			retVal |= (long)(in[offset + 2] & 0xff) << 16;
			retVal |= (long)(in[offset + 1] & 0xff) << 8;
			retVal |= (long)(in[offset] & 0xff);
		}
		else
		{
			retVal = (long)(in[offset] & 0xff) << 56;
			retVal |= (long)(in[offset + 1] & 0xff) << 48;
			retVal |= (long)(in[offset + 2] & 0xff) << 40;
			retVal |= (long)(in[offset + 3] & 0xff) << 32;
			retVal |= (long)(in[offset + 4] & 0xff) << 24;
			retVal |= (long)(in[offset + 5] & 0xff) << 16;
			retVal |= (long)(in[offset + 6] & 0xff) << 8;
			retVal |= (long)(in[offset + 7] & 0xff);
		}
		return retVal;
	}
	
	static String zeroes="000000000000000000000000000000"; //$NON-NLS-1$
	
	static public String toHexString(byte b, int places)
	{
		int i = (int)b & 0xff;
		String returnString=Integer.toHexString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	
	static public String toHexString(int i, int places)
	{
		String returnString=Integer.toHexString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	static public String toHexString(long i, int places)
	{
		String returnString=Long.toHexString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	static public String toHexString(byte [] bytes)
	{
		StringBuffer returnBuffer = new StringBuffer(bytes.length * 2);
		for (int curByteNum = 0; curByteNum < bytes.length; curByteNum++)
		{
			returnBuffer.append(toHexString(bytes[curByteNum], 2));
		}
		return(returnBuffer.toString());
	}
	static public String toDecString(byte b, int places)
	{
		int i = (int)b & 0xff;
		String returnString=Integer.toString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	
	static public String toDecString(int i, int places)
	{
		String returnString=Integer.toString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	static public String toDecString(long i, int places)
	{
		String returnString=Long.toString(i);
		if (returnString.length() < places)
			returnString=zeroes.substring(0, places-returnString.length())+returnString;
		return(returnString);
	}
	
	static public String nullTerminatedUTF8ByteArrayToString(byte [] array, int offset, int count)
	{
		int numChars = 0;
		while (numChars < count && array [offset + numChars] != 0)
			numChars++;
		if (numChars == 0)
			return(""); //$NON-NLS-1$
		byte [] convChars = new byte[numChars];
		System.arraycopy(array, offset, convChars, 0, numChars);
		String returnString=""; //$NON-NLS-1$
		try
		{
			returnString = new String(convChars, "UTF-8"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			//TODO Auto-generated catch block
			org.apache.logging.log4j.Logger exceptionLogger = org.apache.logging.log4j.LogManager.getLogger(BitTwiddle.class);
			exceptionLogger.error("Caught exception UnsupportedEncodingException", e); //$NON-NLS-1$
		}
		return(returnString);
	}
	
	static public int intConstantFromString(String constString)
	{
		if (constString.length() != 4)
			throw new InternalError("Must be 4 character string"); //$NON-NLS-1$
		byte [] bytes;
		try
		{
			bytes = constString.getBytes("ASCII"); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			throw new InternalError("Caught unsupported encoding exception for ASCII!!"); //$NON-NLS-1$
		}
		int returnInt = BitTwiddle.javaByteArrayToInt(bytes, 0);
		return(returnInt);
	}
	
	static public boolean bitSet(int checkInt, int bitNum)
	{
		long checkLong = ((long)checkInt)& 0xffffffff;
		long checkMask = 0x1L << bitNum;
		return ((checkLong & checkMask) != 0);
	}
	
	static public int setBit(int setInt, int bitNum)
	{
		long setLong = ((long)setInt) & 0xffffffff;
		long setMask = 0x1L << bitNum;
		return((int)(setLong | setMask));
	}
	
	static public int clearBit(int clearInt, int bitNum)
	{
		long clearLong = ((long)clearInt) & 0xffffffff;
		long setMask = ~(0x1L << bitNum);
		
		return((int)(clearLong & setMask));
	}
	
	static public long longFromHighLowInt(int highInt, int lowInt)
	{
		// Java does not have unsigned types.  We need to convert to a long
		// first.  If the high bit is set, this will be sign extended so
		// we have to mask off the high 32 bits then shift/add
		long highLong = (((long)highInt) & 0x00000000ffffffffL) << 32;
		long lowLong = (((long)lowInt) & 0x00000000ffffffffL);
		return(highLong | lowLong);
	}
	
	static public int lowIntFromLong(long longIn)
	{
		return ((int)(longIn & 0xffffffff));
	}
	
	static public int highIntFromLong(long longIn)
	{
		return ((int)((longIn >> 32) & 0xffffffff));
	}
	
	static public int andIntWithMask(int bits, long mask)
	{
		long returnLong = (long)bits & mask;
		int returnInt = (int)(returnLong & 0x00000000ffffffffL);
		return returnInt;
	}
	
	static public int orIntWithMask(int bits, long mask)
	{
		long returnLong = (long)bits | mask;
		int returnInt = (int)(returnLong & 0x00000000ffffffffL);
		return returnInt;
	}
	
	static public boolean bitSet(short checkShort, int bitNum)
	{
		int checkInt = ((int)checkShort)& 0x0000ffff;
		int checkMask = 0x1 << bitNum;
		return ((checkInt & checkMask) != 0);
	}
	
	static public short setBit(short setShort, int bitNum)
	{
		int setInt = ((int)setShort) & 0x0000ffff;
		int setMask = 0x1 << bitNum;
		return((short)(setInt | setMask));
	}
	
	static public short clearBit(short clearShort, int bitNum)
	{
		int clearInt = ((int)clearShort) & 0xffffffff;
		int setMask = (0x1 << bitNum);
		
		return((short)(clearInt & setMask));
	}
	
	static public short andShortWithMask(short bits, int mask)
	{
		int returnInt = (int)bits & mask;
		short returnShort = (short)(returnInt & 0x0000ffff);
		return returnShort;
	}
	
	static public short orShortWithMask(short bits, int mask)
	{
		int returnInt = (int)bits | mask;
		short returnShort = (short)(returnInt & 0x0000ffff);
		return returnShort;
	}
}
