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
 
package com.igeekinc.util.xmlserial;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

public class XMLDocSerializer<T>
{
    private String rootName;
    private XMLObjectSerializeHandler<T> rootHandler;
    private boolean omitXMLDeclaration = true;
    private boolean omitDocumentType = true;
    private boolean doIndenting = true;
    public XMLDocSerializer(String rootName, XMLObjectSerializeHandler<T> rootHandler)
    {
        this.rootName = rootName;
        this.rootHandler = rootHandler;
    }

    public boolean isOmitXMLDeclaration()
	{
		return omitXMLDeclaration;
	}

	public void setOmitXMLDeclaration(boolean omitXMLDeclaration)
	{
		this.omitXMLDeclaration = omitXMLDeclaration;
	}

	public boolean isOmitDocumentType()
	{
		return omitDocumentType;
	}

	public void setOmitDocumentType(boolean omitDocumentType)
	{
		this.omitDocumentType = omitDocumentType;
	}

	public boolean isDoIndenting()
	{
		return doIndenting;
	}

	public void setDoIndenting(boolean doIndenting)
	{
		this.doIndenting = doIndenting;
	}

	public void serialize(OutputStream outStream, T rootObject, PauserControlleeIF pauser) 
    throws IOException, SAXException, AbortedException
    {
        OutputFormat defOF = new OutputFormat(Method.XML, "UTF-8", false);
        defOF.setOmitXMLDeclaration(omitXMLDeclaration);
        defOF.setOmitDocumentType(omitDocumentType);
        defOF.setIndenting(doIndenting);
        XMLSerializer serializer;
        serializer = new XMLSerializer(outStream, defOF);
        ContentHandler outputHandler = serializer.asContentHandler();
        rootHandler.serializeObject(rootName, outputHandler, rootObject, pauser);
    }
}
