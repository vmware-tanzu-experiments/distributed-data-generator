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
 
package com.igeekinc.util.xmlserial.serializers;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.XMLObjectSerializeHandler;

public class ArraySerializeHandler<T> implements XMLObjectSerializeHandler<T[]>
{
    private String elementName;
    private XMLObjectSerializeHandler<T> elementSerializeHandler;
    private static AttributesImpl attrs = new AttributesImpl();
    private static IntegerSerializeHandler numberHandler = new IntegerSerializeHandler();
    public ArraySerializeHandler(String elementName, XMLObjectSerializeHandler<T> elementSerializeHandler)
    {
        this.elementName = elementName;
        this.elementSerializeHandler = elementSerializeHandler;
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            T[] objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        if (pauser != null)
            pauser.checkPauseAndAbort();
        T [] arrayToSerialize = objectToSerialize;
        xmlHandler.startElement("", "", fieldName, attrs);
        for (int curElementNum = 0; curElementNum < arrayToSerialize.length; curElementNum++)
        {
            if (arrayToSerialize[curElementNum] == null)
                continue;
            xmlHandler.startElement("", "", elementName, attrs);
            numberHandler.serializeObject("pos", xmlHandler, new Integer(curElementNum), pauser);
            elementSerializeHandler.serializeObject("value", xmlHandler, arrayToSerialize[curElementNum], pauser);
            xmlHandler.endElement("", "", elementName);
        }
        xmlHandler.endElement("", "", fieldName);
    }

}
