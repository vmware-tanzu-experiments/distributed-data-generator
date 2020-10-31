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

public class BitMask<T extends BitMaskAttr>
{
    private int bitMask;
    
    public BitMask(T...attrs)
    {
        for(T curAttr:attrs)
        {
            bitMask |= curAttr.getAttrBitMask();
        }
    }

    public BitMask(int bitMask)
    {
        this.bitMask = bitMask;
    }
    
    public int getBitMask()
    {
        return bitMask;
    }
    
    public boolean isSet(T checkAttr)
    {
        return((bitMask & checkAttr.getAttrBitMask()) != 0);
    }
    
    @SuppressWarnings("unchecked")
    public BitMask<T> or(BitMask<T>orBitMask)
    {
        return new BitMask(orBitMask.bitMask | bitMask);
    }
    
    @SuppressWarnings("unchecked")
    public BitMask<T> and(BitMask<T>andBitMask)
    {
        return new BitMask(andBitMask.bitMask & bitMask);
    }
    
    @SuppressWarnings("unchecked")
    public BitMask<T> xor(BitMask<T>xorBitMask)
    {
        return new BitMask(xorBitMask.bitMask ^ bitMask);
    }
    
    @SuppressWarnings("unchecked")
    public BitMask<T>complement()
    {
        return new BitMask(~bitMask);
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bitMask;
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
		BitMask<?> other = (BitMask<?>) obj;
		if (bitMask != other.bitMask)
			return false;
		return true;
	}
    
}
