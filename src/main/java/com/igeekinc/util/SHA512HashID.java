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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class SHA512HashID implements Serializable
{
	private static final long serialVersionUID = -2519608777763262350L;
	public static final int kSHA512ByteLength = 64;
    private byte [] sha512Hash;
    private transient MessageDigest sha512MD;
    private transient String stringVersion;
    
    public SHA512HashID()
    {
        initMessageDigest();
    }
    
    public SHA512HashID(byte [] dataToHash)
    {
        initMessageDigest();
        update(dataToHash);
        finalize();
    }
    
    private void initMessageDigest()
    {
        try
        {
            sha512MD = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            throw new InternalError("Can't find SHA-512 algorithm");
        }
    }
    public void update(byte updateData)
    {
        if (sha512MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha512MD.update(updateData);
    }
    
    public void update(byte [] updateData)
    {
        if (sha512MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha512MD.update(updateData);
    }
    
    public void update(byte [] updateData, int offset, int length)
    {
        if (sha512MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha512MD.update(updateData, offset, length);
    }
    
    public void finalize()
    {
        if (sha512MD == null)
            throw new IllegalArgumentException("This hash has already been finalized");
        sha512Hash = sha512MD.digest();
        sha512MD = null;
    }
    
    public byte [] getHashData()
    {
        if (sha512MD != null)
            throw new IllegalArgumentException("This hash has not been finalized yet");
        byte [] returnData = new byte[sha512Hash.length];
        System.arraycopy(sha512Hash, 0, returnData, 0, sha512Hash.length);
        return returnData;
    }

    public String toString()
    {
        if (stringVersion == null)
        {
            StringBuffer returnStringBuffer = new StringBuffer(128+4);
            for (int curByteNum = 0; curByteNum < kSHA512ByteLength; curByteNum++)
            {
                returnStringBuffer.append(BitTwiddle.toHexString(sha512Hash[curByteNum], 2));
            }
            
            stringVersion = returnStringBuffer.toString();
        }
        return stringVersion;
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(sha512Hash);
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
		SHA512HashID other = (SHA512HashID) obj;
		if (!Arrays.equals(sha512Hash, other.sha512Hash))
			return false;
		return true;
	}
}
