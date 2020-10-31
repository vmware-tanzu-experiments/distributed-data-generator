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
 
package com.igeekinc.util.rules;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.FieldEntry;
import com.igeekinc.util.xmlserial.ObjectToXMLSerializer;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.BooleanSerializeHandler;
import com.igeekinc.util.xmlserial.serializers.StringSerializeHandler;

public class NameRuleSerializeHandler<T extends NameRule> extends ObjectToXMLSerializer<T>
{
    public final static String kPatternToMatchFieldName = "patternToMatch";
    public final static String kCaseSensitiveFieldName = "caseSensistive";
    public final static String kExcludeSubDirectories = "excludeSubDirectories";

    protected final static AttributesImpl attrs = new AttributesImpl();
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kPatternToMatchFieldName, new StringSerializeHandler()),
        new XMLObjectSerializeInfo(kCaseSensitiveFieldName, new BooleanSerializeHandler()),
        new XMLObjectSerializeInfo(kExcludeSubDirectories, new BooleanSerializeHandler()),
    };
    public NameRuleSerializeHandler()
    {
        super(fieldMappings);
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            T objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, attrs);
        FieldEntry[] fieldEntries = new FieldEntry[3];
        loadFieldEntries(objectToSerialize, fieldEntries, 0);
        super.serializeFields(xmlHandler, fieldEntries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
    
    protected void loadFieldEntries(T ruleToSerialize, FieldEntry [] fieldEntries, int startOffset)
    {
        fieldEntries[startOffset] = new FieldEntry(kPatternToMatchFieldName, ruleToSerialize.getPattern());
        fieldEntries[startOffset + 1] = new FieldEntry(kCaseSensitiveFieldName, ruleToSerialize.caseSensitive);
        fieldEntries[startOffset + 2] = new FieldEntry(kExcludeSubDirectories, ruleToSerialize.excludeSubDirectories);
    }
    
}
