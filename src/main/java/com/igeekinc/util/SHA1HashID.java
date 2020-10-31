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

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SHA1HashID implements Serializable
{
    private static final long serialVersionUID = 4120855460170445368L;
    public static final int kSHA1ByteLength = 20;
    private byte [] sha1Hash;
    private transient MessageDigest sha1MD;
    private transient String stringVersion;
    
    public SHA1HashID()
    {
        initMessageDigest();
    }
    
    /**
     * This constructor is called with a pre-existing hash value in the hashString and decodes it
     * @param hashString
     */
    public SHA1HashID(String hashString)
    {
        if (hashString.length() != kSHA1ByteLength * 2)
            throw new IllegalArgumentException("Length of input string must be "+(kSHA1ByteLength * 2));
        sha1Hash = new byte[kSHA1ByteLength];
        for (int curByteNum = 0; curByteNum < sha1Hash.length; curByteNum++)
        {
            sha1Hash[curByteNum] = (byte)Integer.parseInt(hashString.substring(2*curByteNum, (2*curByteNum)+2), 16);
        }
    }
    
    /**
     * This constructor is called with a pre-existing hash value in the bytes - DO NOT CONFUSE with SHA1HashID(byte [] dataToHash)!
     * @param hashIDBytes
     * @param offset
     */
    public SHA1HashID(byte [] hashIDBytes, int offset)
    {
    	sha1Hash = new byte[kSHA1ByteLength];
    	System.arraycopy(hashIDBytes, offset, sha1Hash, 0, kSHA1ByteLength);
    }
    
    /**
     * This constructor is called with a single buffer to hash.  If you have more than a 
     * single buffer or need an offset, use the SHA1HashID() constructor and the update() methods
     * @param dataToHash
     */
    public SHA1HashID(byte [] dataToHash)
    {
        initMessageDigest();
        update(dataToHash);
        finalizeHash();
    }
    
    /**
     * This constructor is called with a single buffer to hash.  If you have more than a 
     * single buffer or need an offset, use the SHA1HashID() constructor and the update() methods
     * @param dataToHash
     */
    public SHA1HashID(ByteBuffer dataToHash)
    {
        initMessageDigest();
        update(dataToHash);
        finalizeHash();
    }
    
    private void initMessageDigest()
    {
        try
        {
            sha1MD = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            throw new InternalError("Can't find SHA-1 algorithm");
        }
    }
    public void update(byte updateData)
    {
        if (sha1MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha1MD.update(updateData);
    }
    
    public void update(byte [] updateData)
    {
        if (sha1MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha1MD.update(updateData);
    }
    
    public void update(byte [] updateData, int offset, int length)
    {
        if (sha1MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha1MD.update(updateData, offset, length);
    }
    
    
    public void update(ByteBuffer buffer)
    {
    	if (sha1MD == null)
    		throw new IllegalArgumentException("This hash has already been finalized");
    	sha1MD.update(buffer);
    }
    
    public void finalizeHash()
    {
        if (sha1MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha1Hash = sha1MD.digest();
        sha1MD = null;
    }
    
    public byte [] getHashData()
    {
        if (sha1MD != null)
            throw new IllegalArgumentException("This hash has not been finalized yet");
        byte [] returnData = new byte[sha1Hash.length];
        System.arraycopy(sha1Hash, 0, returnData, 0, sha1Hash.length);
        return returnData;
    }
    
    /*
    public boolean equals(Object obj)
    {
        if (obj instanceof SHA1HashID)
            return equals((SHA1HashID)obj);
        return false;
    }
    
    public boolean equals(SHA1HashID checkID)
    {
    	if (sha1MD != null || checkID.sha1MD != null)	// Not finalized, can't compare
    		return false;
        return(Arrays.equals(checkID.sha1Hash, sha1Hash));
    }
    */
    
    public String toString()
    {
        if (stringVersion == null)
        {
            StringBuffer returnStringBuffer = new StringBuffer(128+4);
            for (int curByteNum = 0; curByteNum < sha1Hash.length; curByteNum++)
            {
                returnStringBuffer.append(BitTwiddle.toHexString(sha1Hash[curByteNum], 2));
            }
            
            stringVersion = returnStringBuffer.toString();
        }
        return stringVersion;
    }
    
    @Override
	public int hashCode()
	{
    	if (sha1MD != null)
    		throw new IllegalArgumentException("Hash has not been finalized");
		return Arrays.hashCode(sha1Hash);
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
		SHA1HashID other = (SHA1HashID) obj;
		if (!Arrays.equals(sha1Hash, other.sha1Hash))
			return false;
		return true;
	}

	public String toString(int radix)
    {
        byte [] convertBytes = sha1Hash;
        if (sha1Hash[0] < 0)
        {
            // Damn Java and no unsigned types!
            convertBytes = new byte[sha1Hash.length + 1];
            convertBytes[0] = 0;
            System.arraycopy(sha1Hash, 0, convertBytes, 1, sha1Hash.length);
        }
        BigInteger hashInt = new BigInteger(convertBytes);
        return hashInt.toString(radix);
    }
    
    public byte [] getBytes()
    {
    	byte [] returnBytes = new byte[sha1Hash.length];
    	getBytes(returnBytes, 0);
    	return returnBytes;
    }
    
    public void getBytes(byte [] buffer, int offset)
    {
    	System.arraycopy(sha1Hash, 0, buffer, offset, sha1Hash.length);
    }
    
    
}
