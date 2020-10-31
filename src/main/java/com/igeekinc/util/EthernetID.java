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

import java.util.StringTokenizer;

import org.bouncycastle.util.Arrays;

public class EthernetID
{
	byte []  id;
	public EthernetID(byte [] inID)
	{
		if (inID.length != 6)
		{
			throw new IllegalArgumentException("Ethernet ID's must be 6 digits in length");
		}
		id = Arrays.clone(inID);
	}

	public EthernetID(String idString)
	{
		int numBytes = 0;
		String  curByteStr;

		id = new byte[6];


		if (idString.indexOf('-')>0 || idString.indexOf(':') > 0)
		{

			StringTokenizer   addrTokenizer = null;

			if (idString.indexOf('-')>0)
				addrTokenizer = new StringTokenizer(idString, "-");
			if (idString.indexOf(':')>0)
				addrTokenizer = new StringTokenizer(idString, ":");
			if (addrTokenizer.countTokens() != 6)
				throw new IllegalArgumentException("Ethernet address must have 6 bytes, not "+
						addrTokenizer.countTokens()+" in "+idString);
			while (addrTokenizer.hasMoreTokens())
			{
				curByteStr = addrTokenizer.nextToken();

				if (curByteStr.length() > 2)
					throw new IllegalArgumentException("Ethernet address must be formatted in form XX-XX-XX-XX-XX-XX, "+curByteStr+" breaks this format");

				id[numBytes] = (byte)Integer.parseInt(curByteStr, 16);
				numBytes ++;
			}
		}
		else
			throw new IllegalArgumentException("Unknown Ethernet address format "+ idString);
	}
	public String toString()
	{
		int           curByteNum;
		StringBuffer  returnString = new StringBuffer();
		for (curByteNum = 0; curByteNum < 5; curByteNum++)
		{
			returnString.append(Integer.toHexString(((int)id[curByteNum])&0xff));
			returnString.append(':');
		}
		returnString.append(Integer.toHexString(((int)id[5])&0xff));
		return(returnString.toString());
	}

	public void getBytes(byte [] dest, int offset)
	{
		System.arraycopy(id, 0, dest, offset, 6);
	}

	public byte [] getBytes()
	{
		return Arrays.clone(id);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + java.util.Arrays.hashCode(id);
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
		EthernetID other = (EthernetID) obj;
		if (!java.util.Arrays.equals(id, other.id))
			return false;
		return true;
	}
}