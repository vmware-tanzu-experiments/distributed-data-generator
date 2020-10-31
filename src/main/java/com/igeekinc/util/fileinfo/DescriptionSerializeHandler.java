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
import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.GenericTuple;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.FieldEntry;
import com.igeekinc.util.xmlserial.ObjectToXMLSerializer;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.StringSerializeHandler;

public class DescriptionSerializeHandler extends ObjectToXMLSerializer<GenericTuple<Locale, String>>
{
    public static final String kLocaleFieldName="locale";
    public static final String kDescriptionFieldName="description";
    public DescriptionSerializeHandler()
    {
        super(new XMLObjectSerializeInfo[]{
                new XMLObjectSerializeInfo(kLocaleFieldName, new StringSerializeHandler()),
                new XMLObjectSerializeInfo(kDescriptionFieldName, new StringSerializeHandler())
        });
    }
    @Override
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            GenericTuple<Locale, String> objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, new AttributesImpl());
        FieldEntry [] entries = new FieldEntry[2];
        entries[0] = new FieldEntry(kLocaleFieldName, objectToSerialize.getKey().toString());
        entries[1] = new FieldEntry(kDescriptionFieldName, objectToSerialize.getValue());
        serializeFields(xmlHandler, entries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
