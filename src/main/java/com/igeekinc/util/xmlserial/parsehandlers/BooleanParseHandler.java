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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.igeekinc.util.xmlserial.XMLObjectParseHandler;
import com.igeekinc.util.xmlserial.exceptions.UnexpectedSubElementError;

public class BooleanParseHandler implements XMLObjectParseHandler<Boolean>
{
    private boolean returnBoolean;
    private StringBuffer charInfo;
    private String elementName;
    
    public BooleanParseHandler()
    {
        charInfo = new StringBuffer(20);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        throw new UnexpectedSubElementError("Got a sub element for an Integer "+qName+"("+namespaceURI+":"+localName+")");
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        if (charInfo.charAt(0) == 'Y' || charInfo.charAt(0) == 'y')
            returnBoolean = true;
        else 
            returnBoolean = false;
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        charInfo.append(ch, start, length);
    }
    
    public boolean getValue()
    {
        return returnBoolean;
    }
    
    public Boolean getObject()
    {
        return Boolean.valueOf(returnBoolean);
    }

    public void init(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
    {
        charInfo = new StringBuffer(20);
        elementName = qName;
    }

    public String getElementName()
    {
        return elementName;
    }
}
