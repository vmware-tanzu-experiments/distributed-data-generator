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
 
package com.igeekinc.util.rules;

import com.igeekinc.util.xmlserial.XMLFieldParseInfo;
import com.igeekinc.util.xmlserial.XMLToObjectHandler;
import com.igeekinc.util.xmlserial.parsehandlers.BooleanParseHandler;
import com.igeekinc.util.xmlserial.parsehandlers.StringParseHandler;

public abstract class NameRuleParseHandler<T extends NameRule> extends XMLToObjectHandler<T>
{
    protected StringParseHandler patternToMatchHandler = new StringParseHandler();
    protected BooleanParseHandler caseSensistiveHandler = new BooleanParseHandler();
    protected BooleanParseHandler excludeSubDirectoriesHandler = new BooleanParseHandler();
    
    protected String nameRuleFieldName;
    
    protected NameRuleParseHandler()
    {
        
    }
    public NameRuleParseHandler(String nameRuleFieldName)
    {
        XMLFieldParseInfo[] fieldMappings = new XMLFieldParseInfo[3];
        fillInEntries(fieldMappings);
        setMappings(fieldMappings);
        this.nameRuleFieldName = nameRuleFieldName;
    }

    protected void fillInEntries(XMLFieldParseInfo [] fieldMappings)
    {
        fieldMappings[0] = new XMLFieldParseInfo(NameRuleSerializeHandler.kPatternToMatchFieldName, patternToMatchHandler, false);
        fieldMappings[1] = new XMLFieldParseInfo(NameRuleSerializeHandler.kCaseSensitiveFieldName, caseSensistiveHandler, false);
        fieldMappings[2] = new XMLFieldParseInfo(NameRuleSerializeHandler.kExcludeSubDirectories, excludeSubDirectoriesHandler, false);
    }
    
    public abstract T getObject();
}
