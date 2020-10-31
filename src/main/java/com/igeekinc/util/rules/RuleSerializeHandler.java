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

public class RuleSerializeHandler extends ObjectToXMLSerializer<Rule>
{
    private final static AttributesImpl attrs = new AttributesImpl();
    public static final String kDateAfterRuleFieldName = "dateAfterRule";
    public static final String kDateBeforeRuleFieldName = "dateBeforeRule";
    public static final String kDateExactlyRuleFieldName = "dateExactlyRule";
    public static final String kDateTodayRuleFieldName = "dateTodayRule";
    public static final String kDateWithinRuleFieldName = "dateWithinRule";
    public static final String kExtensionEqualsRuleFieldName = "extensionEqualsRule";
    public static final String kIncludeExcludeRuleFieldName = "includeExcludeRule";
    public static final String kNameContainsRuleFieldName = "nameContainsRule";
    public static final String kNameEndsWithRuleFieldName = "nameEndsWithRule";
    public static final String kNameEqualsRuleFieldName = "nameEqualsRule";
    public static final String kNameStartsWithRuleFieldName = "nameStartsWithRule";
    public static final String kSizeEqualsRuleFieldName = "sizeEqualsRule";
    public static final String kSizeGreaterThanRuleFieldName = "sizeGreaterThanRule";
    public static final String kSizeLessThanRuleFieldName = "sizeLessThanRule";
    public static final String kSubDirectoryOfRuleFieldName = "subDirectoryOfRule";
    
    private final static XMLObjectSerializeInfo [] fieldMappings = {
        new XMLObjectSerializeInfo(kDateAfterRuleFieldName, new DateRuleSerializeHandler()),
        new XMLObjectSerializeInfo(kDateBeforeRuleFieldName, new DateRuleSerializeHandler()),
        new XMLObjectSerializeInfo(kDateExactlyRuleFieldName, new DateRuleSerializeHandler()),
        new XMLObjectSerializeInfo(kDateTodayRuleFieldName, new DateRuleSerializeHandler()),
        new XMLObjectSerializeInfo(kDateWithinRuleFieldName, new DateWithinRuleSerializeHandler()),
        new XMLObjectSerializeInfo(kExtensionEqualsRuleFieldName, new ExtensionEqualsRuleSerializeHandler())
    };
    
    public RuleSerializeHandler()
    {
        super(fieldMappings);
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            Rule objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, attrs);
        Rule rule = (Rule)objectToSerialize;
        String ruleTypeName = rule.getClass().getName();
        // Strip off package stuff and ending semi-colon
        ruleTypeName = ruleTypeName.substring(ruleTypeName.lastIndexOf('.') + 1, ruleTypeName.length());
        if (ruleTypeName.charAt(ruleTypeName.length() - 1) == ';')  //Strip off the ending semi-colon if it exists
            ruleTypeName = ruleTypeName.substring(0, ruleTypeName.length());
        // lower case the first character
        ruleTypeName = ruleTypeName.substring(0, 1).toLowerCase().concat(ruleTypeName.substring(1));
        
        // Let the mapping take care of what serialize handler to call
        FieldEntry [] fieldEntries = {
                new FieldEntry(ruleTypeName, rule)
        };
        super.serializeFields(xmlHandler, fieldEntries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
}
