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
 
package com.igeekinc.util.xmlserial.parsehandlers;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.igeekinc.util.xmlserial.XMLObjectParseHandler;
import com.igeekinc.util.xmlserial.exceptions.MissingElementError;
import com.igeekinc.util.xmlserial.exceptions.UnexpectedSubElementError;

public class ArrayParseHandler<T> implements XMLObjectParseHandler<T[]>
{
    private XMLObjectParseHandler<T> valueParseHandler;
    private IntegerParseHandler posParseHandler;
    private String elementName;
    private XMLObjectParseHandler<?> curHandler;
    private boolean valueSeen, posSeen;
    public ArrayParseHandler(String elementName, XMLObjectParseHandler<T> valueParseHandler)
    {
        this.elementName = elementName;
        this.valueParseHandler = valueParseHandler;
        posParseHandler = new IntegerParseHandler();
    }
    ArrayList<T> values;

    int depth = 0;
    public void init(String namespaceURI, String localName, String qName,
            Attributes atts) throws SAXException
    {
        values = new ArrayList<T>();
        depth++;
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException
    {
        if (depth == 1)
        {
            if (!qName.equals(elementName))
                throw new UnexpectedSubElementError("Expected component element, got element named "+qName+" instead");
            valueSeen = posSeen = false;
        }
        if (depth == 2)
        {
            if (!(qName.equals("pos") || qName.equals("value")))
                throw new UnexpectedSubElementError("Expecting a pos or value element, got "+qName+" instead");
            if (qName.equals("pos"))
            {
                curHandler = posParseHandler;
                posSeen = true;
            }
            if (qName.equals("value"))
            {
                curHandler = valueParseHandler;
                valueSeen = true;
            }
            curHandler.init(namespaceURI, localName, qName, atts);
        }
        if (depth > 2)
            curHandler.startElement(namespaceURI, localName, qName, atts);
        depth++;
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException
    {
        depth--;
        if (curHandler != null)
        {
            curHandler.endElement(namespaceURI, localName, qName);
            if (depth == 2)
                curHandler = null;
        }
        if (qName.equals(elementName) && depth == 1)
        {
            if (!posSeen || !valueSeen)
                throw new MissingElementError("Missing a number or value for element");
            T value = valueParseHandler.getObject();
            int pos = posParseHandler.getValue();
            values.ensureCapacity(pos+1);
            values.add(pos, value);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException
    {
        if (curHandler != null)   // Pass to the string handler if we're in a component, otherwise just discard (usually whitespace)
            curHandler.characters(ch, start, length);
    }

    public T[] getObject()
    {
         return getValue();
    }
    
    @SuppressWarnings("unchecked")
    public T [] getValue()
    {
        if (values.size() == 0)
            return null;
        Object [] objectArray = values.toArray();
        T [] returnArray = (T[])Array.newInstance(objectArray[0].getClass(), objectArray.length);
        System.arraycopy(objectArray, 0, returnArray, 0, objectArray.length);
        return returnArray;
    }
    
    public T [] getValue(T [] prototype)
    {
        T [] returnObjects = values.toArray(prototype);
        return returnObjects;
    }
}
