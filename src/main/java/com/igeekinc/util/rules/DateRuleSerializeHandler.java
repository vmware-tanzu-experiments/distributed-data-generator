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
import com.igeekinc.util.xmlserial.serializers.IntegerSerializeHandler;
import com.igeekinc.util.xmlserial.serializers.LongSerializeHandler;

public class DateRuleSerializeHandler extends ObjectToXMLSerializer<DateRule>
{
    public final static String kStartDateFieldName = "startDate"; //$NON-NLS-1$
    public final static String kEndDateFieldName = "endDate"; //$NON-NLS-1$
    public final static String kDateFieldFieldName = "dateField"; //$NON-NLS-1$

    private final static AttributesImpl attrs = new AttributesImpl();
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kStartDateFieldName, new LongSerializeHandler()),
        new XMLObjectSerializeInfo(kEndDateFieldName, new LongSerializeHandler()),
        new XMLObjectSerializeInfo(kDateFieldFieldName, new IntegerSerializeHandler()),
    };
    public DateRuleSerializeHandler()
    {
        super(fieldMappings);
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            DateRule ruleToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, attrs);
        FieldEntry [] fieldEntries = {
                new FieldEntry(kStartDateFieldName, ruleToSerialize.getStartTime()),
                new FieldEntry(kEndDateFieldName, ruleToSerialize.getEndTime()),
                new FieldEntry(kDateFieldFieldName, ruleToSerialize.getDateField()),
        };
        super.serializeFields(xmlHandler, fieldEntries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
    
}
