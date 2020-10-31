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
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * <p>Title: RepeatingDate</p>
 * <p>Description: Manages a repeating date/time such as every Monday, or everyday at 3:00 PM.  This object
 * contains the algorithms for calculating the next date, the parameters to the algorithm and the last computed date.
 * A granularity can be applied to the calculations.  For example, if the repeating sequence is every Monday, the granularity
 * will be days.  This means that anytime during Monday, if getTime() is called, the current time will be returned.
 * After a time has been used, to be sure of moving forward to the next time, call <code>getNextTime</code> or
 * <code>getTime</code>with <code>useGranularity</code> set to false</p>
 */

public class RepeatingDate extends Date
{
  static final long serialVersionUID=-5738179443474983701L;
  
  private boolean dayOfWeek [], dayOfMonth [];
  private boolean hour [], minute [];
  private boolean hoursSet, minutesSet;
  private static Calendar gCheckCalendar = new GregorianCalendar(), gCalcCalendar = new GregorianCalendar();

  private long nextTime;
  private int granularity;

  public static final int kNoGranularity = 0;
  public static final int kMinutesGranularity = 1;
  public static final int kHoursGranularity = 2;
  public static final int kDaysGranularity = 3;

  public RepeatingDate()
  {
    dayOfWeek = new boolean[8];  // If "0" is set, day of week is ignored
    dayOfWeek [0] = true; // ignore day of week
    for (int curDay = 1; curDay <= 7; curDay++)
      dayOfWeek[curDay] = false;
    dayOfMonth = new boolean[32];  // If "0" is set, day of month is ignored
    dayOfMonth [0] = true; // ignore day of month
    for (int curDay = 1; curDay <= 31; curDay++)
      dayOfMonth[curDay] = false;
    hoursSet = false;
    hour = new boolean[24];
    for (int curHour = 0; curHour <= 23; curHour++)
      hour[curHour] = false;
    minutesSet = false;
    minute = new boolean[60];

    for (int curMinute = 0; curMinute <= 59; curMinute++)
      minute[curMinute] = false;

    nextTime = -1;
  }

  public boolean isRepeatingDayOfWeek()
  {
    return(!dayOfWeek[0]);
  }

  public boolean isRepeatingDayOfMonth()
  {
    return(!dayOfMonth[0]);
  }

  public boolean [] getDayOfWeekArray()
  {
    return(dayOfWeek);
  }

  public boolean [] getDayOfMonthArray()
  {
    return(dayOfMonth);
  }

  public boolean [] getHourArray()
  {
    return(hour);
  }

  public boolean [] getMinuteArray()
  {
    return(minute);
  }
  public void setGranularity(int newGranularity)
  {
    granularity = newGranularity;
  }
  /**
   * Returns the next time this repeating date specifies, after the current time
   * @returns Next time in milliseconds
   */
  public long getNextTime()
  {
    return getNextTime(System.currentTimeMillis(), false);
  }
  /**
   * Returns the next time this repeating date specifies, after/on the current time
   * @returns Next time in milliseconds
   */
  public long getTime()
  {
    return getNextTime(System.currentTimeMillis(), true);
  }
  /**
   * Returns the next time this repeating date specifies, after baseTime
   * @param baseTime Base time in milliseconds
   * @returns Next time in milliseconds
   */
  public long getNextTime(long baseTime)
  {
    return getNextTime(baseTime, false);
  }
  
  int getSpecifiedHour()
  {
  	for (int curHour = 0; curHour <24; curHour++)
  		if (hour[curHour])
  			return curHour;
  	throw new InternalError("Unsupported configuration no hour specified"); //$NON-NLS-1$
  }
  
  int getSpecifiedMinute()
  {
  	for (int curMinute = 0; curMinute <60; curMinute++)
  		if (minute[curMinute])
  			return curMinute;
  	throw new InternalError("Unsupported configuration no minute specified"); //$NON-NLS-1$
  }
  /**
   * Returns the next time this repeating date specifies, after/on baseTime
   * @param baseTime Base time in milliseconds
   * @param useGranularity If true, return baseTime if baseTime is within the same granularity as the previously
   * calculated date.  If false or if baseTime is before or after the granularity period, calculate the next date after baseTime
   * @returns Next time in milliseconds
   */
  public long getNextTime(long baseTime, boolean useGranularity)
  {
  	synchronized(gCalcCalendar)
	{
  		gCalcCalendar.setTime(new Date(baseTime));
  		// We want a specific hour and minute.  If today is after that hour and minute, roll
  		// forward a day and clear the hours and minutes
  		if (!hoursSet)
  			throw new InternalError("Unsupported configuration no hour specified"); //$NON-NLS-1$
  		int desiredHour = getSpecifiedHour();
  		if (!minutesSet)
  			throw new InternalError("Unsupported configuration no minute specified"); //$NON-NLS-1$
  		int desiredMinute = getSpecifiedMinute();
  		int baseHour = gCalcCalendar.get(Calendar.HOUR_OF_DAY);
  		int baseMinute = gCalcCalendar.get(Calendar.MINUTE);
  		if (baseHour > desiredHour ||
  				(baseHour == desiredHour && baseMinute >= desiredMinute))
  		{
  			gCalcCalendar.add(Calendar.DAY_OF_YEAR, 1);	// Roll forward one day
  		}
  		// OK, we're now in a day where the time is less than what we want
  		// Set the time to what we want
  		gCalcCalendar.set(Calendar.HOUR_OF_DAY, desiredHour);
  		gCalcCalendar.set(Calendar.MINUTE, desiredMinute);
  		
  		// Now, let's see if we're on a day that we want
  		
  		if (dayOfWeek[0])  // No day of week set
  			throw new InternalError("Unsupported configuration - no day of week specified"); //$NON-NLS-1$
  		int startDayOfWeek = gCalcCalendar.get(Calendar.DAY_OF_WEEK);
  		boolean dayFound = false;
  		do
  		{
  			if (dayOfWeek[gCalcCalendar.get(Calendar.DAY_OF_WEEK)])
  			{
  				dayFound = true;
  				break;
  			}
  			gCalcCalendar.add(Calendar.DAY_OF_YEAR, 1);
  		} while(gCalcCalendar.get(Calendar.DAY_OF_WEEK) != startDayOfWeek);
  		if (!dayFound)
  			throw new InternalError("Could not find matching day of week"); //$NON-NLS-1$
  		gCalcCalendar.set(Calendar.SECOND, 0);
  		return(gCalcCalendar.getTime().getTime());}
  	
/*    if (useGranularity && baseTime > nextTime)
    {
      if (nextTime != -1L )
      {
        synchronized(gCalcCalendar)
        {
          gCalcCalendar.setTime(new Date(baseTime));
          gCalcCalendar.set(Calendar.MILLISECOND, 0);
          switch(granularity)
          {
            case kDaysGranularity:
              gCalcCalendar.set(Calendar.HOUR, 0);
            case kHoursGranularity:
              gCalcCalendar.set(Calendar.MINUTE, 0);
            case kMinutesGranularity:
              gCalcCalendar.set(Calendar.SECOND, 0);
          }
          long granTime = gCalcCalendar.getTime().getTime();
          if (granTime <= nextTime)
            return baseTime;   // Falls within our granularity, go ahead and return the time that was passed in
        }
      }
    }
    calcNextTime(baseTime, useGranularity);
    return nextTime;
    */
}

  /**
   * Set or clear a repeating time on the day of month specified (1 = first)
   * If this clears the last day of month set, the day of month calculation will be cleared
   */
  public void setDayOfMonth(int setDayOfMonth, boolean set)
  {
    if (setDayOfMonth < 1 || setDayOfMonth > 31)
      throw new IllegalArgumentException("Day of month must be between 1 and 31 (inclusive)"); //$NON-NLS-1$
    if (!dayOfWeek[0]) // If dayOfWeek[0] is false this means that a day of week was specified
      throw new IllegalArgumentException("Cannot specify a day of month if a day of week is specified"); //$NON-NLS-1$
    dayOfMonth[setDayOfMonth] = set;
    if (set)
    {
      dayOfMonth[0] = false;
    }
    else
    {
      boolean dayOfMonthSet = false;
      for (int checkDay = 1; checkDay <= 31; checkDay++)
        if (dayOfMonth[checkDay])
          dayOfMonthSet = true;
      dayOfMonth[0] = !dayOfMonthSet;
    }
    nextTime = -1; // Force recalculation
  }

  /**
   * Set or clear a repeating time on the day of week specified (1 = Sunday, 7 = Saturday or use {@link Calendar} days)
   * If this clears the last day of month set, the day of month calculation will be cleared
   */
  public void setDayOfWeek(int setDayOfWeek, boolean set)
  {
    if (setDayOfWeek < 1 || setDayOfWeek > 7)
      throw new IllegalArgumentException("Day of week must be between 1(Sunday) and 7(Saturday) (inclusive)"); //$NON-NLS-1$
    if (!dayOfMonth[0]) // If dayOfMonth[0] is false this means that a day of month was specified
      throw new IllegalArgumentException("Cannot specify a day of week if a day of month is specified"); //$NON-NLS-1$
    dayOfWeek[setDayOfWeek] = set;
    if (set)
    {
      dayOfWeek[0] = false;
    }
    else
    {
      boolean dayOfWeekSet = false;
      for (int checkDay = 1; checkDay <= 7; checkDay++)
        if (dayOfWeek[checkDay])
          dayOfWeekSet = true;
      dayOfWeek[0] = !dayOfWeekSet;
    }
    nextTime = -1; // Force recalculation
  }

  /**
   * Set or clear a repeating time on the hour specified (00 = 12 AM, 23 = 11 PM)
   * If this clears the last hour set, the hour calculation will be cleared
   */
  public void setHour(int setHour, boolean set)
  {
    if (setHour < 0 || setHour > 23)
      throw new IllegalArgumentException("Hour must be between 0 (12 AM) and 23 (11 PM) (inclusive)"); //$NON-NLS-1$
    hour[setHour] = set;
    if (set)
    {
      hoursSet = true;
    }
    else
    {
      boolean hourSet = false;
      for (int checkHour = 0; checkHour <= 23; checkHour++)
        if (hour[checkHour])
          hourSet = true;
      hoursSet = hourSet;
    }
    nextTime = -1; // Force recalculation
  }

  /**
   * Set or clear a repeating time on the minute specified (0 - 59 inclusive)
   * If this clears the last hour set, the hour calculation will be cleared
   */
  public void setMinute(int setMinute, boolean set)
  {
    if (setMinute < 0 || setMinute > 59)
      throw new IllegalArgumentException("Minute must be between 0 and 59 (inclusive)"); //$NON-NLS-1$
    minute[setMinute] = set;
    if (set)
    {
      minutesSet = true;
    }
    else
    {
      boolean minuteSet = false;
      for (int checkMinute = 0; checkMinute <= 23; checkMinute++)
        if (minute[checkMinute])
          minuteSet = true;
      minutesSet = minuteSet;
    }
    nextTime = -1; // Force recalculation
  }

/*
  long calcNextTime(long baseTime, boolean useCurrentTime)
  {
    boolean timeSelected;
    boolean daySelected, hourSelected, minuteSelected;
    int offset;
    Date startDate = new Date(baseTime);
    if (useCurrentTime)
      offset = 0;
    else
      offset = 1;
    timeSelected = false;
    synchronized (gCalcCalendar)
    {
      gCheckCalendar.clear();
      gCalcCalendar.clear();

      gCheckCalendar.setTime(startDate);
      gCalcCalendar.setTime(startDate);

      if (!dayOfMonth[0] || !dayOfWeek[0])
      {
        while (!timeSelected)
        {
          if (!dayOfMonth[0])
            daySelected = calcDayOfMonth(gCheckCalendar, gCalcCalendar, offset);
          else
            daySelected = calcDayOfWeek(gCheckCalendar, gCalcCalendar, offset);

          if (hoursSet)
          {
            hourSelected = calcHours(gCalcCalendar, gCalcCalendar, 0, false);
          }
          else
            hourSelected = true;

          if (minutesSet)
          {
            minuteSelected = calcMinutes(gCalcCalendar, gCalcCalendar, 0, false);
          }
          else
            minuteSelected = true;
          if (!hourSelected || !minuteSelected)
          {
            offset = 1; // Force roll forward and try it again
          }
          timeSelected = daySelected && hourSelected && minuteSelected;
        }
      }
      else
      {
        if (hoursSet)
        {
          while (!timeSelected)
          {
            // If we get here, hours and possibly minutes have been specified, but not days.  We can
            // freely rollover
            hourSelected = calcHours(gCheckCalendar, gCalcCalendar, offset, true);
            if (minutesSet)
              minuteSelected = calcHours(gCheckCalendar, gCalcCalendar, offset, false);
            else
              minuteSelected =true;
            timeSelected = hourSelected && minuteSelected;
            offset = 1; // force forward motion
          }
        }
        else
          if (minutesSet)
          {
            minuteSelected = calcHours(gCheckCalendar, gCalcCalendar, offset, true);
            timeSelected = minuteSelected;
          }
      }
    }
    if (timeSelected)
    {
      nextTime = gCalcCalendar.getTime().getTime();
      return nextTime;
    }
    else
      return -1L;
  }
  boolean calcDayOfMonth(Calendar checkCalendar, Calendar calcCalendar, int offset)
  {
    boolean daySelected = false, clearHoursMinutes = false;
    if (dayOfMonth[0] != true)
    {
      int startDay = checkCalendar.get(Calendar.DAY_OF_MONTH)+offset;
      int endDay = checkCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
      while (!daySelected)
      {
        for (int checkDay = startDay; checkDay <= endDay; checkDay++)
        {
          if (dayOfMonth[checkDay]==true)
          {
            // Nothing left in the current month, so let's roll forward - we may have to roll forward more than one month
            // consider the case of running set only for 30th of month and date is currently Jan 31st.
            calcCalendar.set(Calendar.DAY_OF_MONTH, checkDay);
            daySelected = true;
            if (checkDay != startDay)
              clearHoursMinutes = true; // We've moved off the current day so clear the hours and minutes
            break;
          }
        }
        if (!daySelected)
        {
          calcCalendar.add(Calendar.MONTH, 1);
          startDay = 1;
          endDay = calcCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
          clearHoursMinutes = true; // We've moved off the current day so clear the hours and minutes
        }
      }
    }
    if (clearHoursMinutes)
    {
      calcCalendar.set(Calendar.HOUR_OF_DAY, 0);
      calcCalendar.set(Calendar.MINUTE, 0);
      calcCalendar.set(Calendar.SECOND, 0);
      calcCalendar.set(Calendar.MILLISECOND, 0);
    }
    return daySelected;
  }

  boolean calcDayOfWeek(Calendar checkCalendar, Calendar calcCalendar, int offset)
  {
    boolean daySelected = false, clearHoursMinutes=false;

    if (dayOfWeek[0] != true)
    {
      daySelected = false;
      int startDay = checkCalendar.get(Calendar.DAY_OF_WEEK)+offset;
      while (!daySelected)
      {
        for (int checkDay = startDay; checkDay <= Calendar.SATURDAY; checkDay++)
        {
          if (dayOfWeek[checkDay])
          {
            calcCalendar.set(Calendar.DAY_OF_WEEK, checkDay);
            daySelected = true;
            if (checkDay != startDay)
              clearHoursMinutes = true; // We've moved off the current day so clear the hours and minutes
            break;
          }
        }
        if (!daySelected)
        {
          // Should only come through here once, unless things are really screwed up.
          calcCalendar.add(Calendar.WEEK_OF_MONTH, 1);
          startDay = Calendar.SUNDAY;
          clearHoursMinutes = true; // We've moved off the current day so clear the hours and minutes
        }
      }
    }
    if (clearHoursMinutes || offset > 0)
    {
      calcCalendar.set(Calendar.HOUR_OF_DAY, 0);
      calcCalendar.set(Calendar.MINUTE, 0);
      calcCalendar.set(Calendar.SECOND, 0);
      calcCalendar.set(Calendar.MILLISECOND, 0);
    }
    return(daySelected);
  }

  boolean calcHours(Calendar checkCalendar, Calendar calcCalendar, int offset, boolean rollover)
  {
    boolean hourSelected = false, clearMinutes = false;
    if (hoursSet == true)
    {
      int startHour = checkCalendar.get(Calendar.HOUR_OF_DAY)+offset;
      while (!hourSelected)
      {
        for (int checkHour = startHour; checkHour <= 23; checkHour++)
        {
          if (hour[checkHour])
          {
            calcCalendar.set(Calendar.HOUR_OF_DAY, checkHour);
            // If we're dealing with another day or it's today and we're not dealing with this hour, set the minutes to 0
            if (checkHour > checkCalendar.get(Calendar.HOUR_OF_DAY))
              clearMinutes = true;
            hourSelected = true;
            break;
          }
        }
        if (!rollover)
          break; // no matter what, don't go around again if we can't rollover
        if (!hourSelected)
        {
          calcCalendar.add(Calendar.DATE, 1);
          startHour = 0;
          clearMinutes = true;
        }
      }
    }
    if (clearMinutes)
    {
      calcCalendar.set(Calendar.MINUTE, 0);
      calcCalendar.set(Calendar.SECOND, 0);
      calcCalendar.set(Calendar.MILLISECOND, 0);
      granularity = kHoursGranularity;
    }
    return(hourSelected);
  }
  boolean calcMinutes(Calendar checkCalendar, Calendar calcCalendar, int offset,boolean rollover)
  {
    boolean minutesSelected = false, clearSeconds = false;
    if (minutesSet == true)
    {
      int startMinute = checkCalendar.get(Calendar.MINUTE)+offset;
      while (!minutesSelected)
      {
        for (int checkMinute = startMinute; checkMinute < 60; checkMinute++)
        {
          if (minute[checkMinute])
          {
            calcCalendar.set(Calendar.MINUTE, checkMinute);
            // If we're dealing with another day or it's today and we're not dealing with this minute, set the seconds, etc. to 0
            if (checkMinute > checkCalendar.get(Calendar.HOUR_OF_DAY))
              clearSeconds = true;

            minutesSelected = true;
            break;
          }
        }
        if (!rollover)
          break; // no matter what, don't go around again if we can't rollover
        if (!minutesSelected)
        {
          calcCalendar.add(Calendar.HOUR, 1);
          startMinute = 0;
          clearSeconds = true;
        }
      }
    }
    if (clearSeconds)
    {
      calcCalendar.set(Calendar.SECOND, 0);
      calcCalendar.set(Calendar.MILLISECOND, 0);
      granularity = kMinutesGranularity;
    }
    return(minutesSelected);
  }
*/
  /*
  public String toString()
  {
    StringBuffer returnStringBuf = new StringBuffer();
    if (!dayOfWeek[0])
    {
      returnStringBuf.append("every ");
      for (int curDayOfWeekNum = 1; curDayOfWeekNum <= Calendar.SATURDAY; curDayOfWeekNum++)
      {

        if (dayOfWeek[curDayOfWeekNum])
        {
          switch(curDayOfWeekNum)
          {
            case Calendar.SUNDAY:
              returnStringBuf.append("Sunday ");
              break;
            case Calendar.MONDAY:
              returnStringBuf.append("Monday ");
              break;
            case Calendar.TUESDAY:
              returnStringBuf.append("Tuesday ");
              break;
            case Calendar.WEDNESDAY:
              returnStringBuf.append("Wednesday ");
              break;
            case Calendar.THURSDAY:
              returnStringBuf.append("Thursday ");
              break;
            case Calendar.FRIDAY:
              returnStringBuf.append("Friday ");
              break;
            case Calendar.SATURDAY:
              returnStringBuf.append("Saturday ");
              break;
          }
        }
      }
    }
    if (!dayOfMonth[0])
    {
      returnStringBuf.append("on ");
      for (int curDayOfMonthNum = 1; curDayOfMonthNum < 31; curDayOfMonthNum++)
      {
        if (dayOfMonth[curDayOfMonthNum])
        {
        returnStringBuf.append(Integer.toString(curDayOfMonthNum));
        returnStringBuf.append(" ");
      }
      }
    }
    
    returnStringBuf.append("at ");
    if (hoursSet)
    {
    	for (int curHour = 0; curHour < 24; curHour ++)
    	{
    		boolean pm = false;
    		if(hour[curHour])
    		{
    			int addHour = curHour;
    			if (addHour >= 12)
    			{
    				pm = true;
    				if (addHour > 12)
    					addHour -= 12;
    			}
    			String addHourStr = Integer.toString(addHour);
    			int addMinute = 0;
    			for (int curMinute = 0; curMinute < 60; curMinute ++)
    			{
    				if (minute[curMinute])
    				{
    					addMinute = curMinute;
    					break;
    				}
    			}
    			String addMinuteStr = Integer.toString(addMinute);
    			if (addMinuteStr.length() < 2)
    				addMinuteStr = "0" + addMinuteStr;
    			returnStringBuf.append(addHourStr);
    			returnStringBuf.append(":");
    			returnStringBuf.append(addMinuteStr);
    			if (pm)
    				returnStringBuf.append(" PM");
    			else
    				returnStringBuf.append(" AM");
    		}
    	}
    }
    return(returnStringBuf.toString());
  }
  */
  public String toString()
  {
  	String returnString = ""; //$NON-NLS-1$
  	if (!dayOfWeek[0])
  	{
  		StringBuffer daysString = new StringBuffer();
  		boolean needsComma = false;
  		for (int curDayOfWeekNum = 1; curDayOfWeekNum <= Calendar.SATURDAY; curDayOfWeekNum++)
  		{

  			if (dayOfWeek[curDayOfWeekNum])
  			{
  				if (needsComma)
  					daysString.append(", "); //$NON-NLS-1$
  				switch(curDayOfWeekNum)
				{
				case Calendar.SUNDAY:
					daysString.append(_("Sunday")); //$NON-NLS-1$
					break;
				case Calendar.MONDAY:
					daysString.append(_("Monday")); //$NON-NLS-1$
					break;
				case Calendar.TUESDAY:
					daysString.append(_("Tuesday")); //$NON-NLS-1$
					break;
				case Calendar.WEDNESDAY:
					daysString.append(_("Wednesday")); //$NON-NLS-1$
					break;
				case Calendar.THURSDAY:
					daysString.append(_("Thursday")); //$NON-NLS-1$
					break;
				case Calendar.FRIDAY:
					daysString.append(_("Friday")); //$NON-NLS-1$
					break;
				case Calendar.SATURDAY:
					daysString.append(_("Saturday")); //$NON-NLS-1$
					break;
  				}
  				needsComma = true;
  			}
  		}
  		returnString = MessageFormat.format(_("every {0} at {1,time,short}"), new Object[]{daysString.toString(), new Date(getNextTime())}); //$NON-NLS-1$
  	}
  	/*
  	if (!dayOfMonth[0])
  	{
  		returnStringBuf.append("on ");
  		for (int curDayOfMonthNum = 1; curDayOfMonthNum < 31; curDayOfMonthNum++)
  		{
  			if (dayOfMonth[curDayOfMonthNum])
  			{
  				returnStringBuf.append(Integer.toString(curDayOfMonthNum));
  				returnStringBuf.append(" ");
  			}
  		}
  	}*/
  	
  	return(returnString);
  }
}