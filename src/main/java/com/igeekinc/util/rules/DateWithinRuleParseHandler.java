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
import com.igeekinc.util.xmlserial.parsehandlers.IntegerParseHandler;

public class DateWithinRuleParseHandler extends XMLToObjectHandler<DateWithinRule>
{
    private IntegerParseHandler periodFieldHandler = new IntegerParseHandler();
    private IntegerParseHandler dateFieldHandler = new IntegerParseHandler();
    
    public DateWithinRuleParseHandler()
    {
        XMLFieldParseInfo [] fieldMappings = {
                 new XMLFieldParseInfo(DateWithinRuleSerializeHandler.kPeriodFieldName, periodFieldHandler, false),
                new XMLFieldParseInfo(DateWithinRuleSerializeHandler.kDateFieldFieldName, dateFieldHandler, false)
        };
        setMappings(fieldMappings);
    }

    public DateWithinRuleParseHandler(XMLFieldParseInfo[] fieldMapping)
    {
        super(fieldMapping);
    }

    
    public DateWithinRule getObject()
    {
        DateWithinRule returnRule = new DateWithinRule(((Integer)returnValues.get(DateWithinRuleSerializeHandler.kPeriodFieldName)).intValue(),
                ((Integer)returnValues.get(DateWithinRuleSerializeHandler.kDateFieldFieldName)).intValue());

        return returnRule;
    }
    
    
}
