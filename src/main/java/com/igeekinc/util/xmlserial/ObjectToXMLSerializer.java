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
 
package com.igeekinc.util.xmlserial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.exceptions.UnknownFieldError;

public abstract class ObjectToXMLSerializer<T> implements XMLObjectSerializeHandler<T>
{
    HashMap<String, XMLObjectSerializeInfo> fieldMappingHash;
    
    public ObjectToXMLSerializer(XMLObjectSerializeInfo [] fieldMapping)
    {
        fieldMappingHash = new HashMap<String, XMLObjectSerializeInfo>(fieldMapping.length);
        for (int curMappingNum = 0; curMappingNum < fieldMapping.length; curMappingNum++)
        {
            XMLObjectSerializeInfo curInfo = fieldMapping[curMappingNum];
            fieldMappingHash.put(curInfo.getFieldName(), curInfo);
        }
    }
    
    protected void addMapping(XMLObjectSerializeInfo mappingToAdd)
    {
        fieldMappingHash.put(mappingToAdd.getFieldName(), mappingToAdd);
    }
    
    public abstract void serializeObject(String fieldName, ContentHandler xmlHandler, T objectToSerialize, PauserControlleeIF pauser)
    throws SAXException, AbortedException, IOException;
    
    protected void serializeFields(ContentHandler xmlHandler, ArrayList<FieldEntry> entries, PauserControlleeIF pauser)
    throws SAXException, AbortedException, IOException
    {
        FieldEntry [] entriesArray = new FieldEntry[entries.size()];
        entriesArray = entries.toArray(entriesArray);
        serializeFields(xmlHandler, entriesArray, pauser);
    }
    /**
     * Convenience method - put all of the fields to be serialized into an array of FieldEntries and call
     * to get basic serialization
     * @param xmlHandler
     * @param fieldEntries - an ArrayList filled with FieldEntry pairs
     * @throws SAXException 
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
    protected void serializeFields(ContentHandler xmlHandler, FieldEntry[] fieldEntries, PauserControlleeIF pauser) 
    throws SAXException, AbortedException, IOException
    {
        for (int curFieldEntryNum = 0; curFieldEntryNum < fieldEntries.length; curFieldEntryNum++)
        {
            if (pauser != null)
                pauser.checkPauseAndAbort();
            FieldEntry curEntry = fieldEntries[curFieldEntryNum];
            if (curEntry == null)
                continue;
            String fieldName = curEntry.getFieldName();
            XMLObjectSerializeInfo curInfo = fieldMappingHash.get(fieldName);
            if (curInfo == null)
                throw new UnknownFieldError("Can't find handler for field "+fieldName);
            XMLObjectSerializeHandler handler = curInfo.getHandler();
            if (curEntry.getFieldValue() != null)
            	handler.serializeObject(fieldName, xmlHandler, curEntry.getFieldValue(), pauser);
        }
    }
}
