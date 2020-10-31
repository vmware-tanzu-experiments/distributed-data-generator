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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

/**
 * Always encodes String using URLEncoder and UTF-8 character set
 *
 */
public class EncodedStringSerializeHandler extends StringSerializeHandler
{
    public EncodedStringSerializeHandler()
    {
        super(false);
    }
    public void serializeObject(String fieldName, ContentHandler xmlHandler, String objectToSerialize, PauserControlleeIF pauser) throws SAXException, AbortedException
    {
        if (objectToSerialize == null)
            objectToSerialize="";
        try
        {
            objectToSerialize = URLEncoder.encode(objectToSerialize, "UTF-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        super.serializeObject(fieldName, xmlHandler, objectToSerialize, pauser);
    }
}
