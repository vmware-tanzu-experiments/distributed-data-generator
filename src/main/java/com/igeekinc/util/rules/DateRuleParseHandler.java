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

import java.util.Date;

import com.igeekinc.util.xmlserial.XMLFieldParseInfo;
import com.igeekinc.util.xmlserial.XMLToObjectHandler;
import com.igeekinc.util.xmlserial.parsehandlers.IntegerParseHandler;
import com.igeekinc.util.xmlserial.parsehandlers.LongParseHandler;

public class DateRuleParseHandler extends XMLToObjectHandler<DateRule>
{
    private LongParseHandler startTimeHandler = new LongParseHandler();
    private LongParseHandler endTimeHandler = new LongParseHandler();
    private IntegerParseHandler dateFieldHandler = new IntegerParseHandler();
    
    private String dateRuleFieldName;
    
    public DateRuleParseHandler(String dateRuleFieldName)
    {
        XMLFieldParseInfo [] fieldMappings = {
                new XMLFieldParseInfo(DateRuleSerializeHandler.kStartDateFieldName, startTimeHandler, false),
                new XMLFieldParseInfo(DateRuleSerializeHandler.kEndDateFieldName, endTimeHandler, false),
                new XMLFieldParseInfo(DateRuleSerializeHandler.kDateFieldFieldName, dateFieldHandler, false)
        };
        setMappings(fieldMappings);
        this.dateRuleFieldName = dateRuleFieldName;
    }
    
    public DateRule getObject()
    {
        DateRule returnRule = null;
        Date startDate = new Date(((Long)returnValues.get(DateRuleSerializeHandler.kStartDateFieldName)).longValue());
        Date endDate= new Date(((Long)returnValues.get(DateRuleSerializeHandler.kEndDateFieldName)).longValue());
        if (dateRuleFieldName.equals(RuleSerializeHandler.kDateAfterRuleFieldName))
        {

            returnRule = new DateAfterRule(startDate,
                ((Integer)returnValues.get(DateRuleSerializeHandler.kDateFieldFieldName)).intValue());
        }
        
        if (dateRuleFieldName.equals(RuleSerializeHandler.kDateBeforeRuleFieldName))
        {
            returnRule = new DateBeforeRule(endDate,
                ((Integer)returnValues.get(DateRuleSerializeHandler.kDateFieldFieldName)).intValue());
        }
        
        if (dateRuleFieldName.equals(RuleSerializeHandler.kDateExactlyRuleFieldName))
        {
            returnRule = new DateExactlyRule(endDate,
                    ((Integer)returnValues.get(DateRuleSerializeHandler.kDateFieldFieldName)).intValue());
        }
        if (dateRuleFieldName.equals(RuleSerializeHandler.kDateTodayRuleFieldName))
        {
            returnRule = new DateTodayRule(((Integer)returnValues.get(DateRuleSerializeHandler.kDateFieldFieldName)).intValue());
        }
        return returnRule;
    }
    
    
}
