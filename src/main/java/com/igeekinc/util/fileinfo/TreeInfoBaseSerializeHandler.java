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
 
package com.igeekinc.util.fileinfo;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.ObjectToXMLSerializer;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.StringSerializeHandler;


public class TreeInfoBaseSerializeHandler<T extends TreeInfoBase> extends ObjectToXMLSerializer<T>
{
    public static final String kParentPathFieldName = "parentPath";
    public static final String kElementIDFieldName = "elementID";
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kParentPathFieldName, new StringSerializeHandler()),
        new XMLObjectSerializeInfo(kElementIDFieldName, new StringSerializeHandler()),
    };
    
    public TreeInfoBaseSerializeHandler()
    {
        super(fieldMappings);
    }
    @Override
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            T objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, new AttributesImpl());
        StringSerializeHandler parentPathSerializeHandler = new StringSerializeHandler();
        StringSerializeHandler elementIDSerializeHandler = new StringSerializeHandler();
        parentPathSerializeHandler.serializeObject(kParentPathFieldName, xmlHandler, objectToSerialize.parentPath, pauser);
        elementIDSerializeHandler.serializeObject(kElementIDFieldName, xmlHandler, objectToSerialize.elementID, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
