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
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.GenericTuple;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.rules.Rule;
import com.igeekinc.util.rules.RuleSerializeHandler;
import com.igeekinc.util.xmlserial.FieldEntry;
import com.igeekinc.util.xmlserial.ObjectToXMLSerializer;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.ArraySerializeHandler;

public class FileInfoSerializeHandler extends ObjectToXMLSerializer<FileInfo>
{
    public static final String kFileClassFieldName = "fileClass";
    public static final String kFileGroupFieldName = "fileGroup";
    public static final String kRulesFieldName = "rules";
    public static final String kDescriptionsFieldName = "descriptions";
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kFileClassFieldName, new FileClassSerializeHandler()),
        new XMLObjectSerializeInfo(kFileGroupFieldName, new FileGroupSerializeHandler()),
        new XMLObjectSerializeInfo(kRulesFieldName, new ArraySerializeHandler<Rule>(kRulesFieldName, new RuleSerializeHandler())),
        new XMLObjectSerializeInfo(kDescriptionsFieldName, new ArraySerializeHandler<GenericTuple<Locale, String>>(kDescriptionsFieldName, new DescriptionSerializeHandler()))
    };
    public FileInfoSerializeHandler()
    {
        super(fieldMappings);
    }
    @SuppressWarnings("unchecked")
    @Override
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            FileInfo objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, new AttributesImpl());
        FieldEntry [] entries = new FieldEntry[4];
        entries[0] = new FieldEntry(kFileClassFieldName, objectToSerialize.getFileClass());
        entries[1] = new FieldEntry(kFileGroupFieldName, objectToSerialize.getFileGroup());
        entries[2] = new FieldEntry(kRulesFieldName, objectToSerialize.getMatchRules());
        GenericTuple<Locale, String>[] descriptions = new GenericTuple[objectToSerialize.descriptions.size()];
        int curDescriptionNum = 0;
        for (Map.Entry<Locale, String>curEntry:objectToSerialize.descriptions.entrySet())
        {
            descriptions[curDescriptionNum] = new GenericTuple<Locale, String>(curEntry.getKey(), curEntry.getValue());
            curDescriptionNum++;
        }
        entries[3] = new FieldEntry(kDescriptionsFieldName, descriptions);
        serializeFields(xmlHandler, entries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
