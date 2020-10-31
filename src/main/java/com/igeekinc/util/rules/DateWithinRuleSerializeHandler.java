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

public class DateWithinRuleSerializeHandler extends ObjectToXMLSerializer<DateWithinRule>
{
    public final static String kPeriodFieldName = "period";
    public final static String kDateFieldFieldName = "dateField";

    private final static AttributesImpl attrs = new AttributesImpl();
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kPeriodFieldName, new IntegerSerializeHandler()),
        new XMLObjectSerializeInfo(kDateFieldFieldName, new IntegerSerializeHandler()),
    };
    public DateWithinRuleSerializeHandler()
    {
        super(fieldMappings);
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            DateWithinRule objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        DateWithinRule ruleToSerialize = (DateWithinRule)objectToSerialize;
        xmlHandler.startElement("", "", fieldName, attrs);
        FieldEntry [] fieldEntries = {
                new FieldEntry(kPeriodFieldName, ruleToSerialize.period),
                new FieldEntry(kDateFieldFieldName, ruleToSerialize.getDateField()),
        };
        super.serializeFields(xmlHandler, fieldEntries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
