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
 
package com.igeekinc.util.jdk14keycompat;

import java.math.BigInteger;

public class RSAPublicKeyOverride extends JS_PublicKeyOverride implements java.security.interfaces.RSAPublicKey 
{

	private static final long serialVersionUID = -4603577243802113781L;

	//private byte [] a, b;
	public RSAPublicKeyOverride() 
	{
		
	}

	/*
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
		// prepare to read the alternate persistent fields
        ObjectInputStream.GetField fields = ois.readFields();
        a = (byte [])fields.get("a", null);
        b = (byte [])fields.get("b", null);
    }
*/
	public BigInteger getPublicExponent() 
	{
		return new BigInteger(1, thePublicKey.getPublicExponentBytes());
	}

	public String getAlgorithm() 
	{
		return "RSA";
	}

	public byte[] getEncoded() 
	{
		throw new UnsupportedOperationException();
	}

	public String getFormat() 
	{
		return "X509";
	}

	public BigInteger getModulus() 
	{
		return new BigInteger(1, thePublicKey.getModulusBytes());
	}
}
