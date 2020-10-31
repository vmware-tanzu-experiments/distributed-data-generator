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

import static com.igeekinc.util.rules.Internationalization._;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateWithinRule extends DateRule
{
	static final long serialVersionUID = -6198729606514113401L;
	public static final int kLastDay = 0;
	public static final int kLast2Days = 1;
	public static final int kLast3Days = 2;
	public static final int kLastWeek = 3;
	public static final int kLast2Weeks = 4;
	public static final int kLast3Weeks = 5;
	public static final int kLastMonth = 6;
	public static final int kLast2Months = 7;
	public static final int kLast3Months = 8;
	public static final int kLast6Months = 9;
	
	int myDateField, period;
	String myDescription;
	
	public DateWithinRule(int period, int dateField)
	{
		myDateField = dateField;
		this.period = period;
		init(0, 0, myDateField);
	}
	
	public void init()
	{
		long startTime, endTime;
		endTime = Long.MAX_VALUE;
		GregorianCalendar workCalendar = new GregorianCalendar();
		workCalendar.setTime(new Date());
		
		// Everybody starts from 12:00 AM of the day we're starting from
		workCalendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		workCalendar.set(GregorianCalendar.MINUTE, 0);
		workCalendar.set(GregorianCalendar.SECOND, 0);
		workCalendar.set(GregorianCalendar.MILLISECOND, 0);
		
		switch (period)
		{
			case kLastDay:
				// No need to set anything to get within the last day
				break;
			case kLast2Days:
				workCalendar.add(GregorianCalendar.DAY_OF_YEAR, -1);
				break;
			case kLast3Days:
				workCalendar.add(GregorianCalendar.DAY_OF_YEAR, -2);
				break;
			case kLastWeek:
				workCalendar.add(GregorianCalendar.WEEK_OF_YEAR, -1);
				break;
			case kLast2Weeks:
				workCalendar.add(GregorianCalendar.WEEK_OF_YEAR, -2);
				break;
			case kLast3Weeks:
				workCalendar.add(GregorianCalendar.WEEK_OF_YEAR, -3);
				break;
			case kLastMonth:
				workCalendar.add(GregorianCalendar.MONTH, -1);
				break;
			case kLast2Months:
				workCalendar.add(GregorianCalendar.MONTH, -2);
				break;
			case kLast3Months:
				workCalendar.add(GregorianCalendar.MONTH, -3);
				break;
			case kLast6Months:
				workCalendar.add(GregorianCalendar.MONTH, -6);
				break;
		}
		startTime = workCalendar.getTime().getTime();
		init(startTime, endTime, myDateField);
	}
	
	public String toString()
	{
		switch(getDateField())
		{
		case kModifiedTime:
			switch (period)
			{
			case kLastDay:
				return(_("Files whose last modified time is within the last day"));  //$NON-NLS-1$
			case kLast2Days:
				return(_("Files whose last modified time is within the last two days"));  //$NON-NLS-1$
			case kLast3Days:
				return(_("Files whose last modified time is within the last three days"));  //$NON-NLS-1$
			case kLastWeek:
				return(_("Files whose last modified time is within the last week"));  //$NON-NLS-1$
			case kLast2Weeks:
				return(_("Files whose last modified time is within the last two weeks"));  //$NON-NLS-1$
			case kLast3Weeks:
				return(_("Files whose last modified time is within the last three weeks"));  //$NON-NLS-1$
			case kLastMonth:
				return(_("Files whose last modified time is within the last month"));  //$NON-NLS-1$
			case kLast2Months:
				return(_("Files whose last modified time is within the last two months"));  //$NON-NLS-1$
			case kLast3Months:
				return(_("Files whose last modified time is within the last three months"));  //$NON-NLS-1$
			case kLast6Months:
				return(_("Files whose last modified time is within the last six months"));  //$NON-NLS-1$
			}
			break;
		case kCreatedTime:
			switch (period)
			{
			case kLastDay:
				return(_("Files whose creation time is within the last day"));  //$NON-NLS-1$
			case kLast2Days:
				return(_("Files whose creation time is within the last two days"));  //$NON-NLS-1$
			case kLast3Days:
				return(_("Files whose creation time is within the last three days"));  //$NON-NLS-1$
			case kLastWeek:
				return(_("Files whose creation time is within the last week"));  //$NON-NLS-1$
			case kLast2Weeks:
				return(_("Files whose creation time is within the last two weeks"));  //$NON-NLS-1$
			case kLast3Weeks:
				return(_("Files whose creation time is within the last three weeks"));  //$NON-NLS-1$
			case kLastMonth:
				return(_("Files whose creation time is within the last month"));  //$NON-NLS-1$
			case kLast2Months:
				return(_("Files whose creation time is within the last two months"));  //$NON-NLS-1$
			case kLast3Months:
				return(_("Files whose creation time is within the last three months"));  //$NON-NLS-1$
			case kLast6Months:
				return(_("Files whose creation time is within the last six months"));  //$NON-NLS-1$
			}
			break;
		}
		throw new InternalError("Unexpected DateField "+getDateField()); //$NON-NLS-1$
	}
}
