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

public class RSAPrivateKeyOverride extends JS_PrivateKeyOverride implements java.security.interfaces.RSAPrivateKey
{
	private static final long serialVersionUID = 6630190575158124905L;

	public BigInteger getPrivateExponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	public BigInteger getModulus() {
		// TODO Auto-generated method stub
		return null;
	}

}
