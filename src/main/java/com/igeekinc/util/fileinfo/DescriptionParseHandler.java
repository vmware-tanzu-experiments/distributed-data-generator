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
 
package com.igeekinc.util.fileinfo;

import java.util.Locale;

import com.igeekinc.util.GenericTuple;
import com.igeekinc.util.xmlserial.XMLFieldParseInfo;
import com.igeekinc.util.xmlserial.XMLToObjectHandler;
import com.igeekinc.util.xmlserial.parsehandlers.StringParseHandler;

public class DescriptionParseHandler extends XMLToObjectHandler<GenericTuple<Locale, String>>
{
    protected StringParseHandler localeParseHandler = new StringParseHandler();
    protected StringParseHandler descriptionParseHandler = new StringParseHandler();
    public DescriptionParseHandler()
    {
        setMappings(new XMLFieldParseInfo[]{
           new XMLFieldParseInfo(DescriptionSerializeHandler.kLocaleFieldName, localeParseHandler, false),
           new XMLFieldParseInfo(DescriptionSerializeHandler.kDescriptionFieldName, descriptionParseHandler, false)
        });
    }
    @Override
    public GenericTuple<Locale, String> getObject()
    {
        return new GenericTuple<Locale, String>(new Locale(localeParseHandler.getObject()), descriptionParseHandler.getObject());
    }
}
