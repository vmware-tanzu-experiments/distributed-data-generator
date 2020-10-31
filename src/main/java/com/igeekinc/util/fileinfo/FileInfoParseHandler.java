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
import com.igeekinc.util.rules.Rule;
import com.igeekinc.util.rules.RuleParseHandler;
import com.igeekinc.util.xmlserial.XMLFieldParseInfo;
import com.igeekinc.util.xmlserial.XMLToObjectHandler;
import com.igeekinc.util.xmlserial.parsehandlers.ArrayParseHandler;

public class FileInfoParseHandler extends XMLToObjectHandler<FileInfo>
{
    protected FileClassParseHandler fileClassHandler = new FileClassParseHandler();
    protected FileGroupParseHandler fileGroupHandler = new FileGroupParseHandler();
    protected ArrayParseHandler<Rule> rulesHandler = new ArrayParseHandler<Rule>(FileInfoSerializeHandler.kRulesFieldName, new RuleParseHandler());
    protected ArrayParseHandler<GenericTuple<Locale, String>> descriptionsHandler = new ArrayParseHandler<GenericTuple<Locale, String>>(FileInfoSerializeHandler.kDescriptionsFieldName, 
            new DescriptionParseHandler());
    
    public FileInfoParseHandler()
    {
        XMLFieldParseInfo [] fieldMappings = {
                new XMLFieldParseInfo(FileInfoSerializeHandler.kFileClassFieldName, fileClassHandler, false),
                new XMLFieldParseInfo(FileInfoSerializeHandler.kFileGroupFieldName, fileGroupHandler, false),
                new XMLFieldParseInfo(FileInfoSerializeHandler.kRulesFieldName, rulesHandler, false),
                new XMLFieldParseInfo(FileInfoSerializeHandler.kDescriptionsFieldName, descriptionsHandler, false)
        };
        setMappings(fieldMappings);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public FileInfo getObject()
    {
        Rule [] protoRulesArray = new Rule[0];
        GenericTuple<Locale, String>[] protoDescriptionsArray = new GenericTuple[0];
        return new FileInfo(fileClassHandler.getObject(), fileGroupHandler.getObject(), rulesHandler.getValue(protoRulesArray), 
                descriptionsHandler.getValue(protoDescriptionsArray));
    }
}
