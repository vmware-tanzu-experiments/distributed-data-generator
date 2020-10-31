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
 
package com.igeekinc.util;

import static com.igeekinc.util.rules.Internationalization._;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

// TODO - Yes, this stuff actually pre-dates enums.  Figure out how to replace this with
// an enum while maintaining backwards compatibility

class IntervalDateUnit implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3747824314001753419L;
	int unitType;
	IntervalDateUnit(int inUnitType)
	{
		unitType = inUnitType;
	}
}

public class IntervalDate extends RepeatingDate
{
	static final long serialVersionUID = 816479147927992711L;
	
	public static final int kDaysInt = 1;
	public static final int kWeeksInt = 2;
	public static final int kHoursInt = 3;
	public static final int kMinutesInt = 4;
	public static final IntervalDateUnit kDays = new IntervalDateUnit(kDaysInt);
	public static final IntervalDateUnit kWeeks = new IntervalDateUnit(kWeeksInt);
	public static final IntervalDateUnit kHours = new IntervalDateUnit(kHoursInt);
	public static final IntervalDateUnit kMinutes = new IntervalDateUnit(kMinutesInt);

	
	IntervalDateUnit intervalUnits;
	long interval;
	long intervalMillis;
	long startTime;
	boolean skipWeekends;
	/**
	 * 
	 */
	public IntervalDate(Date inStartDate, IntervalDateUnit inIntervalUnits, int inInterval, boolean inSkipWeekends)
	{
		startTime = inStartDate.getTime();

		intervalUnits = inIntervalUnits;
		interval = inInterval;
		switch (intervalUnits.unitType)
		{
			case kDaysInt:
				intervalMillis = inInterval * 24L*3600L*1000L;
				break;
			case kWeeksInt:
				intervalMillis = inInterval * 24L * 7L*3600L*1000L;
				break;
			case kHoursInt:
				intervalMillis = inInterval * 3600L*1000L;
				break;
			case kMinutesInt:
				intervalMillis = inInterval * 60L * 1000L;
				break;
		}

		skipWeekends = inSkipWeekends;
	}

	public long getStartTime()
	{
		return startTime;
	}
	
	public long getNextTime(long baseTime, boolean useGranularity)
	{
		return getNextTime(baseTime, useGranularity, TimeZone.getDefault());
	}
	
	public int getIntervalUnits()
	{
		return intervalUnits.unitType;
	}
	
	public long getInterval()
	{
		return interval;
	}
	
	public long getNextTime(long baseTime, boolean useGranularity, TimeZone currentTimeZone)
	{
		long returnTime;
		if (baseTime < startTime)
			return startTime;
		long totalInterval = baseTime - startTime;
		GregorianCalendar checkCalendar = new GregorianCalendar();
		returnTime = startTime + ((totalInterval / intervalMillis) +1) * intervalMillis;

			
		if (skipWeekends)
		{
			long stepMillis;
			stepMillis = intervalMillis;
			if (stepMillis == 7 * 24 * 3600) // If it's a one week interval we'll always wind up on a weekend, so change our increment to 1 day instead
				stepMillis = 24 * 3600;
			checkCalendar.setTime(new Date(returnTime));
			while (checkCalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
				checkCalendar.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
				{
					returnTime += stepMillis;
					checkCalendar.setTime(new Date(returnTime));
				}
		}

		if (currentTimeZone.inDaylightTime(new Date(startTime)) && !currentTimeZone.inDaylightTime(new Date(returnTime)))
			returnTime = returnTime + currentTimeZone.getDSTSavings();
		if (!currentTimeZone.inDaylightTime(new Date(startTime)) && currentTimeZone.inDaylightTime(new Date(returnTime)))
			returnTime = returnTime - currentTimeZone.getDSTSavings();
		return(returnTime);
	}
	
	public String toString()
	{
		String returnString = ""; //$NON-NLS-1$
		Date startDate = new Date(startTime);
		switch(intervalUnits.unitType)
		{
			case kDaysInt:
				if (interval == 1)
					returnString = MessageFormat.format(_("every day at {0,time,short}"), new Object[]{startDate}); //$NON-NLS-1$
				else
					returnString = MessageFormat.format(_("every {0} days at {1,time,short} starting on {1,date}"), new Object[]{new Long(interval), startDate}); //$NON-NLS-1$
				break;
			case kWeeksInt:
				if (interval == 1)
					returnString = MessageFormat.format(_("every week at {0, time} on {0, time,EEEEEEEE}"), new Object[]{startDate}); //$NON-NLS-1$
				else
					returnString = MessageFormat.format(_("every {0} weeks at {1, time} on {1, time,EEEEEEEE}"), new Object[]{new Long(interval), startDate}); //$NON-NLS-1$
				break;
			case kHoursInt:
				if (interval == 1)
					returnString = MessageFormat.format(_("every hour at {0, time,mm} minutes after the hour"), new Object[]{startDate}); //$NON-NLS-1$
				else
					returnString = MessageFormat.format(_("every {0} hours at {1, time,mm} minutes after the hour"), new Object[]{new Long(interval), startDate}); //$NON-NLS-1$
				break;
			case kMinutesInt:
				if (interval == 1)
					returnString = _("every minute"); //$NON-NLS-1$
				else
					returnString = MessageFormat.format(_("every {0} minutes"), new Object[]{new Long(interval)}); //$NON-NLS-1$
				break;
		}
		return(returnString);
		
	}
	/*
	public String toString()
	{
		String returnString = Messages.getString("com.igeekinc.indelible.client.IntervalDate.0"); //$NON-NLS-1$
		if (interval > 1)
			returnString = returnString +Long.toString(interval);
		SimpleDateFormat formatter = new SimpleDateFormat(Messages.getString("com.igeekinc.indelible.client.IntervalDate.1")); //$NON-NLS-1$
		switch(intervalUnits.unitType)
		{
		case 1:
			if (interval > 1)
				returnString = returnString + Messages.getString("com.igeekinc.indelible.client.IntervalDate.2"); //$NON-NLS-1$
			else
				returnString = returnString + Messages.getString("com.igeekinc.indelible.client.IntervalDate.3");  //$NON-NLS-1$
			returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.4")+formatter.format(new Date(startTime)); //$NON-NLS-1$
			break;
		case 2:
			if (interval > 1)
				returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.5"); //$NON-NLS-1$
			else
				returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.6"); //$NON-NLS-1$
			returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.7")+formatter.format(new Date(startTime)); //$NON-NLS-1$
			break;
		case 3:
			if (interval > 1)
				returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.8"); //$NON-NLS-1$
			else
				returnString = returnString+Messages.getString("com.igeekinc.indelible.client.IntervalDate.9"); //$NON-NLS-1$
			break;
		case 4:
			if (interval >1)
				returnString = returnString+" minutes";
			else
				returnString = returnString+" minute";
		}
		return(returnString);
		
	}
	*/
}
