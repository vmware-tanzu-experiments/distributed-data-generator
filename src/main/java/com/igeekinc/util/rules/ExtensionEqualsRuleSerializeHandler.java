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

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.FieldEntry;
import com.igeekinc.util.xmlserial.XMLObjectSerializeInfo;
import com.igeekinc.util.xmlserial.serializers.BooleanSerializeHandler;
import com.igeekinc.util.xmlserial.serializers.StringSerializeHandler;

public class ExtensionEqualsRuleSerializeHandler extends
        NameRuleSerializeHandler<ExtensionEqualsRule>
{
    public static final String kIsFieldName = "is";
    public static final String kExtensionStringName = "extension";
    
    public ExtensionEqualsRuleSerializeHandler()
    {
        addMapping(new XMLObjectSerializeInfo(kIsFieldName, new BooleanSerializeHandler()));
        addMapping(new XMLObjectSerializeInfo(kExtensionStringName, new StringSerializeHandler()));
    }
    
    @Override
    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            ExtensionEqualsRule objectToSerialize, PauserControlleeIF pauser)
            throws SAXException, AbortedException, IOException
    {
        xmlHandler.startElement("", "", fieldName, attrs);
        FieldEntry[] fieldEntries = new FieldEntry[5];
        loadFieldEntries(objectToSerialize, fieldEntries, 0);
        fieldEntries[3] = new FieldEntry(kIsFieldName, objectToSerialize.is);
        fieldEntries[4] = new FieldEntry(kExtensionStringName, objectToSerialize.extensionString);
        super.serializeFields(xmlHandler, fieldEntries, pauser);
        xmlHandler.endElement("", "", fieldName);
    }
    
}
