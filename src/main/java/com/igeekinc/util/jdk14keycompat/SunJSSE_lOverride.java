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

import java.io.Serializable;

public class SunJSSE_lOverride implements Serializable 
{
	private static final long serialVersionUID = 1436026662190709030L;
	
	public SunJSSE_lOverride()
	{
		
	}

	/*
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
		
		Object obj1 = ois.readObject();
		Object obj2 = ois.readObject();
		
    }
    */
}
