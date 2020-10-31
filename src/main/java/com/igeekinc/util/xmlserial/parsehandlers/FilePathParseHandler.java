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

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.igeekinc.util.FilePath;
import com.igeekinc.util.xmlserial.XMLObjectParseHandler;
import com.igeekinc.util.xmlserial.exceptions.UnexpectedSubElementError;

public class FilePathParseHandler implements XMLObjectParseHandler<FilePath>
{
    private ArrayList<String> pathComponents;
    private StringParseHandler stringHandler;
    private int depth = 0;
    public void init(String namespaceURI, String localName, String qName,
            Attributes atts) throws SAXException
    {
        pathComponents = new ArrayList<String>();
        stringHandler = new StringParseHandler();
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException
    {
        if (!qName.equals("component"))
            throw new UnexpectedSubElementError("Expected component element, got element named "+qName+" instead");
        depth++;
        if (depth > 1)
            throw new UnexpectedSubElementError("");
        stringHandler.init(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException
    {
        if (depth > 0)
        {
            stringHandler.endElement(namespaceURI, localName, qName);
            pathComponents.add(stringHandler.getObject());
        }
        depth--;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException
    {
        if (depth > 0)   // Pass to the string handler if we're in a component, otherwise just discard (usually whitespace)
            stringHandler.characters(ch, start, length);
    }

    public FilePath getObject()
    {
        return getValue();
    }
    
    public FilePath getValue()
    {
        String [] components = new String[pathComponents.size()];
        components = pathComponents.toArray(components);
        FilePath returnPath = FilePath.getFilePath(components, true);
        return returnPath;
    }
}
