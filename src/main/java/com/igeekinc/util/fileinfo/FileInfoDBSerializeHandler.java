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
import com.igeekinc.util.xmlserial.FieldEntry;
import com.igeekinc.util.xmlserial.ObjectToXMLSerializer;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.ArraySerializeHandler;

public class FileInfoDBSerializeHandler extends ObjectToXMLSerializer<FileInfoDB>
{
    public static final String kFileInfoArrayFieldName = "fileInfoArray";
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kFileInfoArrayFieldName, new ArraySerializeHandler<FileInfo>(kFileInfoArrayFieldName, new FileInfoSerializeHandler()))
    };
    public FileInfoDBSerializeHandler()
    {
        super(fieldMappings);
    }
    
    @Override
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            FileInfoDB objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, new AttributesImpl());
        FieldEntry [] entries = new FieldEntry[1];
        entries[0] = new FieldEntry(kFileInfoArrayFieldName, objectToSerialize.allFileInfo.toArray());
        serializeFields(xmlHandler, entries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
