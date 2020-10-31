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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class PublicKeyWorkaroundInputStream extends ObjectInputStream 
{
	protected PublicKeyWorkaroundInputStream() throws IOException,
			SecurityException
	{
		super();
	}

	public PublicKeyWorkaroundInputStream(InputStream in) throws IOException
	{
		super(in);
	}

	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
        Class<?> overrideClass = null;
        String className = resultClassDescriptor.getName();
        if (className.equals("com.sun.net.ssl.internal.ssl.JSA_RSAPublicKey"))
        	overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.RSAPublicKeyOverride");
        if (className.equals("com.sun.net.ssl.internal.ssl.JS_PublicKey"))
        	overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.JS_PublicKeyOverride");
        if (className.equals("COM.rsa.jsafe.SunJSSE_dq"))
        	overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.SunJSSE_dqOverride");
    	if (className.equals("COM.rsa.jsafe.SunJSSE_bg"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.SunJSSE_bgOverride");
    	if (className.equals("com.sun.net.ssl.internal.ssl.JSA_RSAPrivateKey"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.RSAPrivateKeyOverride");
    	if (className.equals("com.sun.net.ssl.internal.ssl.JS_PrivateKey"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.JS_PrivateKeyOverride");
    	if (className.equals("COM.rsa.jsafe.SunJSSE_bi"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.SunJSSE_biOverride");
    	if (className.equals("COM.rsa.jsafe.SunJSSE_dn"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.SunJSSE_dnOverride");
    	if (className.equals("COM.rsa.jsafe.SunJSSE_l"))
    		overrideClass = getClass().getClassLoader().loadClass("com.igeekinc.util.jdk14keycompat.SunJSSE_lOverride");
        if (overrideClass != null)
        {
        	long origUID = resultClassDescriptor.getSerialVersionUID();
        	/*
        	ObjectStreamField [] fields = resultClassDescriptor.getFields();
        	System.out.println("Source class = "+resultClassDescriptor.getName());
        	for (int curFieldNum = 0; curFieldNum < fields.length; curFieldNum ++)
        	{
        		System.out.println(fields[curFieldNum].getName() + " = "+fields[curFieldNum].getTypeString());
        	}
        	*/
        	resultClassDescriptor = ObjectStreamClass.lookup(overrideClass);
        	
        	if (resultClassDescriptor == null)
        		System.out.println("No ObjectStreamClass for "+overrideClass);
        	else
        		if (resultClassDescriptor.getSerialVersionUID() != origUID)
        			System.out.println("UID for "+overrideClass.getName()+" needs to be set to "+origUID);
        			
        }
        
        return resultClassDescriptor;
    }
}
