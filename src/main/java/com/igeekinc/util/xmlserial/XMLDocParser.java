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
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.igeekinc.util.pauseabort.AbortedError;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.exceptions.UnexpectedSubElementError;

public class XMLDocParser<T> extends DefaultHandler
{
    private String rootName;
    private XMLObjectParseHandler<T> handler;
    private int elementDepth;
    private PauserControlleeIF pauser;
    
    public XMLDocParser(String rootName, XMLObjectParseHandler<T> handler)
    {
        this.rootName = rootName;
        this.handler = handler;
    }

    public T parse(String xmlToParse, PauserControlleeIF pauser)
    throws AbortedException
    {
        StringReader reader = new StringReader(xmlToParse);
        return parse(new InputSource(reader), pauser);
    }
    
    public T parse(InputSource sourceToParse, PauserControlleeIF pauser)
    throws AbortedException
    {
        this.pauser = pauser;
        elementDepth = 0;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            
            //parse the file and also register this class for call backs
            sp.parse(sourceToParse, this);

        }catch(SAXException se) {
            se.printStackTrace();
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch (IOException ie) {
            ie.printStackTrace();
        }
        catch (AbortedError ae)
        {
            throw ae.getAbortedException(); // Re-throw our aborted exception and look normal
        }
        return getObject();
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (pauser != null)
        {
            try
            {
                pauser.checkPauseAndAbort();
            } catch (AbortedException e)
            {
                throw new AbortedError(e);
            }
        }
        if (elementDepth > 0)
            handler.characters(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (pauser != null)
        {
            try
            {
                pauser.checkPauseAndAbort();
            } catch (AbortedException e)
            {
                throw new AbortedError(e);
            }
        }
        elementDepth--;
        handler.endElement(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (pauser != null)
        {
            try
            {
                pauser.checkPauseAndAbort();
            } catch (AbortedException e)
            {
                throw new AbortedError(e);
            }
        }
        if (elementDepth == 0)
        {
            if (!qName.equals(rootName))
                throw new UnexpectedSubElementError("Document root expected to be "+rootName+", got "+qName+" instead");
            handler.init(uri, localName, qName, attributes);
        }
        else
        {
            handler.startElement(uri, localName, qName, attributes);
        }
        elementDepth++;
    }
    
    public T getObject()
    {
        return handler.getObject();
    }
}
