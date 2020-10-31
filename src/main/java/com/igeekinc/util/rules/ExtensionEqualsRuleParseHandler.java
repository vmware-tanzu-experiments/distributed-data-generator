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
import com.igeekinc.util.xmlserial.parsehandlers.BooleanParseHandler;
import com.igeekinc.util.xmlserial.parsehandlers.StringParseHandler;


public class ExtensionEqualsRuleParseHandler extends NameRuleParseHandler<ExtensionEqualsRule>
{
    StringParseHandler extensionParseHandler = new StringParseHandler();
    BooleanParseHandler isParseHandler = new BooleanParseHandler();
    public ExtensionEqualsRuleParseHandler(String nameRuleFieldName)
    {
        XMLFieldParseInfo[] fieldMappings = new XMLFieldParseInfo[5];
        fillInEntries(fieldMappings);

        fieldMappings[3] = new XMLFieldParseInfo(ExtensionEqualsRuleSerializeHandler.kExtensionStringName, extensionParseHandler, false);
        fieldMappings[4] = new XMLFieldParseInfo(ExtensionEqualsRuleSerializeHandler.kIsFieldName, isParseHandler, false);
        setMappings(fieldMappings);
        this.nameRuleFieldName = nameRuleFieldName;
    }
    
    @Override
    public ExtensionEqualsRule getObject()
    {
        
        ExtensionEqualsRule returnRule = new ExtensionEqualsRule(extensionParseHandler.getObject(), isParseHandler.getObject(), caseSensistiveHandler.getValue(), excludeSubDirectoriesHandler.getValue());
        return returnRule;
    }
}
