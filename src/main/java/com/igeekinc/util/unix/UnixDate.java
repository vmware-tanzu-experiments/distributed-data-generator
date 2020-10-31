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
 
package com.igeekinc.util.unix;

import java.util.Date;

public class UnixDate extends Date
{

  /**
	 * 
	 */
	private static final long serialVersionUID = -4725739017656718373L;
public UnixDate()
  {
    super();
  }
  public UnixDate(Date inDate)
  {
    super(inDate.getTime());
  }

  public UnixDate(long inTime)
  {
    super(inTime);
  }

  public UnixDate(int secs, int nsecs)
  {
    long javaTime = getJavaTime(secs, nsecs);
    super.setTime(javaTime);
  }

  public UnixDate(long secs, long nsecs)
  {
    long javaTime = getJavaTime(secs, nsecs);
    super.setTime(javaTime);
  }
  
  public int getSecs()
  {
    return((int)(getTime()/1000));
  }

  public long getSecsLong()
  {
      return(getTime()/1000L);
  }
  public int getNSecs()
  {
    long returnTime = getTime();
    returnTime %= 1000;   // Strip off everything over millis
    returnTime = returnTime * 1000000; // Multiple millis by 1000000 (million) to get nanoseconds (billionth)
    return((int)returnTime);
  }


  public int getUSecs()
  {
	long returnTime = getTime();
	returnTime %= 1000;   // Strip off everything over millis
	returnTime = returnTime * 1000; // Multiple millis by 1000 (thousand) to get microseconds (millionth)
	return((int)returnTime);
  }

  public static long getJavaTime(int secs, int nsecs)
  {
    long javaSecs = secs, javaNSecs = nsecs, javaTime;
    javaSecs &= 0xffffffff;
    javaNSecs &=  0xffffffff;
    javaTime = javaSecs * 1000;  // Java time is in milliseconds
    javaTime += javaNSecs/1000000; // Divide nanoseconds (billionths) by 1000000 (million) to get milliseconds (thousandths)
    return javaTime;
  }

  public static long getJavaTime(long secs, long nsecs)
  {
    long javaSecs = secs, javaNSecs = nsecs, javaTime;
    javaTime = javaSecs * 1000;  // Java time is in milliseconds
    javaTime += javaNSecs/1000000; // Divide nanoseconds (billionths) by 1000000 (million) to get milliseconds (thousandths)
    return javaTime;
  }
}

