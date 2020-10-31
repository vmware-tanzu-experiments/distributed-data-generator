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
 
package com.igeekinc.util.xmlserial.serializers;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.igeekinc.util.BitTwiddle;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.XMLObjectSerializeHandler;

public class ByteArraySerializeHandler implements XMLObjectSerializeHandler<byte []>
{

    public void serializeObject(String fieldName, ContentHandler xmlHandler,
            byte [] objectToSerialize, PauserControlleeIF pauser) 
    throws SAXException, AbortedException
    {
        if (pauser != null)
            pauser.checkPauseAndAbort();
        xmlHandler.startElement("", "", fieldName, new AttributesImpl());
        String hexString = BitTwiddle.toHexString(objectToSerialize);
        char [] outputChars = hexString.toCharArray();
        xmlHandler.characters(outputChars, 0, outputChars.length);
        xmlHandler.endElement("", "", fieldName);
    }

}
