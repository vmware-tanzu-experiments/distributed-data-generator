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

import java.io.IOException;
import java.io.ObjectInputStream;

public class SunJSSE_dqOverride extends SunJSSE_bgOverride
{

	private static final long serialVersionUID = -3795284426387248589L;

	private String a;
	private String [] b;
	private byte [] modulus, publicExponent;
	public SunJSSE_dqOverride() {
		// TODO Auto-generated constructor stub
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
		/*
		// prepare to read the alternate persistent fields
        ObjectInputStream.GetField fields = ois.readFields();
        
        a = (String)fields.get("a", null);
        b = (String [])fields.get("b", null);
        */
		a = (String)ois.readObject();
		b = (String [])ois.readObject();
		modulus = (byte [])ois.readObject();
		publicExponent = (byte [])ois.readObject();
    }

	public byte [] getModulusBytes()
	{
		return modulus;
	}
	
	public byte [] getPublicExponentBytes()
	{
		return publicExponent;
	}
}
