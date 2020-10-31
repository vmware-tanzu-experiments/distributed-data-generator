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
 
package com.igeekinc.util.macos.macosx;

import java.util.Date;
import com.igeekinc.util.BitTwiddle;
import com.igeekinc.util.SystemInfo;
import com.igeekinc.util.unix.UnixDate;

public class MacOSXDate extends UnixDate
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7758011642512698213L;

	public static int kMacOSXDateSize = (SystemInfo.is64BitVM() ? 16:8);
	
	public MacOSXDate()
	{
		super();
	}

  public MacOSXDate(int secs, int nsecs)
  {
    super(secs, nsecs);
  }
  
  public MacOSXDate(long secs, long nsecs)
  {
    super(secs, nsecs);
  }
  
  public MacOSXDate(Date inDate)
  {
    super(inDate);
  }

	public MacOSXDate(long inTime)
	{
		super(inTime);
	}
	
	static long secsFromBuffer(byte [] rawData, int offset)
	{
		if (!SystemInfo.is64BitVM())
		{
			return BitTwiddle.nativeByteArrayToInt(rawData, offset);
		}
		else
		{
			return BitTwiddle.nativeByteArrayToLong(rawData, offset);
		}
	}
	
	public MacOSXDate(byte [] rawData, int offset)
	{
		super(secsFromBuffer(rawData, offset),
				secsFromBuffer(rawData, offset+ (!SystemInfo.is64BitVM() ?4:8)));;
	}

	public void toByteArray(byte [] outputArray, int offset)
	{
		if (!SystemInfo.is64BitVM())
		{
			int secs, nSecs;
			long ourTime = getTime();
			secs = (int)(ourTime/1000L);
			nSecs = (int)(ourTime%1000L)*1000000;
			BitTwiddle.intToNativeByteArray(secs, outputArray, offset);
			BitTwiddle.intToNativeByteArray(nSecs, outputArray, offset+4);
		}
		else
		{
			long secs, nSecs;
			long ourTime = getTime();
			secs = ourTime/1000L;
			nSecs = (ourTime%1000L)*1000000;
			BitTwiddle.longToNativeByteArray(secs, outputArray, offset);
			BitTwiddle.longToNativeByteArray(nSecs, outputArray, offset+8);
		}
	}
}