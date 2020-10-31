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

import java.util.Iterator;

import com.igeekinc.util.xmlserial.XMLFieldParseInfo;
import com.igeekinc.util.xmlserial.XMLToObjectHandler;

public class RuleParseHandler extends XMLToObjectHandler<Rule>
{
    private DateRuleParseHandler dateAfterRuleHandler = new DateRuleParseHandler(RuleSerializeHandler.kDateAfterRuleFieldName);
    private DateRuleParseHandler dateBeforeRuleHandler = new DateRuleParseHandler(RuleSerializeHandler.kDateBeforeRuleFieldName);
    private DateRuleParseHandler dateExactlyRuleHandler = new DateRuleParseHandler(RuleSerializeHandler.kDateExactlyRuleFieldName);
    private DateRuleParseHandler dateTodayRuleHandler = new DateRuleParseHandler(RuleSerializeHandler.kDateTodayRuleFieldName);
    private DateWithinRuleParseHandler dateWithinRuleHandler = new DateWithinRuleParseHandler();
    private ExtensionEqualsRuleParseHandler extensionEqualsRuleHandler = new ExtensionEqualsRuleParseHandler(RuleSerializeHandler.kExtensionEqualsRuleFieldName);
    public RuleParseHandler()
    {
        XMLFieldParseInfo [] fieldMappings = {
                new XMLFieldParseInfo(RuleSerializeHandler.kDateAfterRuleFieldName, dateAfterRuleHandler, false),
                new XMLFieldParseInfo(RuleSerializeHandler.kDateBeforeRuleFieldName, dateBeforeRuleHandler, false),
                new XMLFieldParseInfo(RuleSerializeHandler.kDateExactlyRuleFieldName, dateExactlyRuleHandler, false),
                new XMLFieldParseInfo(RuleSerializeHandler.kDateTodayRuleFieldName, dateTodayRuleHandler, false),
                new XMLFieldParseInfo(RuleSerializeHandler.kDateWithinRuleFieldName, dateWithinRuleHandler, false),
                new XMLFieldParseInfo(RuleSerializeHandler.kExtensionEqualsRuleFieldName, extensionEqualsRuleHandler, false)
        };
        setMappings(fieldMappings);
    }
    @Override
    public Rule getObject()
    {
        Iterator<Object> iterator = returnValues.values().iterator();
        if (iterator.hasNext())
            return (Rule)iterator.next();
        return null;
    }
}
