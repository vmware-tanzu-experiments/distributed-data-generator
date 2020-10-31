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

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.igeekinc.util.xmlserial.exceptions.UnexpectedSubElementError;

/**
 * XMLToObjectHandler provides a framework for mapping a portion of
 * an XML stream to an object.
 * Each element name is mapped to a ContentHandler for that
 * field.  The info for the Object is stored internally as a Hashmap of <field name>, <Object> tuples
 *
 */
public abstract class XMLToObjectHandler<T> implements XMLObjectParseHandler<T>
{
    private HashMap<String, XMLFieldParseInfo> fieldMappingHash;
    protected HashMap<String, Object> returnValues;
    private XMLFieldParseInfo curFieldInfo;
    private int elementsPushed;
    
    public XMLToObjectHandler()
    {
        
    }
    
    public XMLToObjectHandler(XMLFieldParseInfo[] fieldMapping)
    {
        setMappings(fieldMapping);
    }
    
    protected void setMappings(XMLFieldParseInfo[] fieldMapping)
    {
        if (fieldMapping == null || fieldMapping.length == 0)
            throw new IllegalArgumentException("null or zero length fieldMapping is not allowed");
        fieldMappingHash = new HashMap<String, XMLFieldParseInfo>();
        for (int curMappingNum = 0; curMappingNum < fieldMapping.length; curMappingNum++)
        {
            XMLFieldParseInfo fieldInfo = fieldMapping[curMappingNum];
            fieldMappingHash.put(fieldInfo.getFieldName(), fieldInfo);
        }

    }

    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (elementsPushed == 1)
        {
            curFieldInfo = (XMLFieldParseInfo)fieldMappingHash.get(qName);
            if (curFieldInfo == null)
                throw new UnexpectedSubElementError("No expected field named "+qName);
            Object curValue = returnValues.get(qName);
            if (!curFieldInfo.isMultipleAllowed() && curValue != null)
                throw new UnexpectedSubElementError("Cannot have multiple elements for "+qName);
            curFieldInfo.getHandler().init(uri, localName, qName, attributes);
        }
        else
        {
            curFieldInfo.getHandler().startElement(uri, localName, qName, attributes);
        }
        elementsPushed++;
    }
    
    
    @SuppressWarnings("unchecked")
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (curFieldInfo != null)
            curFieldInfo.getHandler().endElement(uri, localName, qName);
        elementsPushed --;
        if (elementsPushed < 0)
            throw new InternalError("Got called too many times");
        if (elementsPushed == 1)
        {
            if (!qName.equals(curFieldInfo.getFieldName()))
                throw new InternalError("Got an end to element "+qName+" when expecting end to element "+curFieldInfo.getFieldName());
            Object oldObject = returnValues.get(qName);
            Object newObject = curFieldInfo.getHandler().getObject();
            
            if (curFieldInfo.isMultipleAllowed())
            {
                ArrayList<Object> array;
                if (oldObject != null)
                    array = (ArrayList<Object>)oldObject;
                else
                    array = new ArrayList<Object>();
                array.add(newObject);
                returnValues.put(qName, array);
            }
            else
            {
                returnValues.put(qName, newObject);
            }
            curFieldInfo = null;
        }
    }

    
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (curFieldInfo != null)
            curFieldInfo.getHandler().characters(ch, start, length);
        // Otherwise, discard the characters silently
    }

    /**
     * Override this to convert your hashmap into the appropriate Object
     * @return
     */
    public abstract T getObject();


    public void init(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        // Zap returnValues
        returnValues = new HashMap<String, Object>();
        elementsPushed = 1;
    }
}
