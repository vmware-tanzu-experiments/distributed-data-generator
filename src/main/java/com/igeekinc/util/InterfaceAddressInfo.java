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

import java.math.BigInteger;
import java.net.InetAddress;

/*
 * A similar class appears in 1.6 but we need to support back to 1.5.  Also, it doesn't
 * have a handy check for same network
 */
public class InterfaceAddressInfo
{
	private InetAddress address;
	private int networkPrefixBits;
	private BigInteger addressMask;			// networkPrefixBits as a bitmask
	private BigInteger networkPrefixInt;	// Just the network number as a BigInteger
	
	public InterfaceAddressInfo(InetAddress address, int networkPrefixBits)
	{
		this.address = address;
		this.networkPrefixBits = networkPrefixBits;
		int addressBits = address.getAddress().length * 8;
		addressMask = BigInteger.valueOf(2).pow(networkPrefixBits).subtract(BigInteger.valueOf(1));
		addressMask = addressMask.shiftLeft(addressBits - networkPrefixBits);
		
		BigInteger addressInteger = new BigInteger(address.getAddress());
		networkPrefixInt = addressInteger.and(addressMask);
	}
	
	public boolean sameNetwork(InetAddress checkAddress)
	{
		if (networkPrefixBits < 8)
			return false;		// There aren't any real networks with so few network address bits
		BigInteger checkAddressInt = new BigInteger(checkAddress.getAddress());
		BigInteger checkNetworkAddr = checkAddressInt.and(addressMask);
		return checkNetworkAddr.equals(networkPrefixInt);
	}
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public int getNetworkPrefixBits()
	{
		return networkPrefixBits;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + networkPrefixBits;
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
		InterfaceAddressInfo other = (InterfaceAddressInfo) obj;
		if (address == null)
		{
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (networkPrefixBits != other.networkPrefixBits)
			return false;
		return true;
	}
	
	public String toString()
	{
		return address.toString()+" mask = "+addressMask.toString(16);
	}
}
