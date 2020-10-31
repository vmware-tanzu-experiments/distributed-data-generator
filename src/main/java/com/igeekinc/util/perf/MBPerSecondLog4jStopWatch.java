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
 
package com.igeekinc.util.perf;

import java.text.MessageFormat;

import org.perf4j.log4j.Log4JStopWatch;

public class MBPerSecondLog4jStopWatch extends Log4JStopWatch
{
	private static final double	kOneMegabyte	= 1024.0*1024.0;
	private static final long	serialVersionUID	= -4281765986363796440L;
	long bytesProcessed = 0;
	
	public MBPerSecondLog4jStopWatch()
	{
		super();
	}
	
	public MBPerSecondLog4jStopWatch(String tag)
	{
		super(tag);
	}
	/**
	 * Increases the number of bytes processed by bytesProcessed
	 * @param bytesProcessed
	 */
	public void bytesProcessed(long bytesProcessed)
	{
		this.bytesProcessed += bytesProcessed;
	}

	public double getBytesPerSecond()
	{
		double elapsedSeconds = ((double)getElapsedTime())/1000.0;
		return (double)bytesProcessed/elapsedSeconds;
	}
	
	public double getMegaBytesPerSecond()
	{
		double elapsedSeconds = ((double)getElapsedTime())/1000.0;
		double mbs = (double)bytesProcessed/kOneMegabyte;
		return mbs/elapsedSeconds;
	}
	
	public String getBytesPerSecondString()
	{
		//TODO - this is probably slow, figure out if we need to speed it up
		return MessageFormat.format("b={0,number,#},mbs={1,number,#.##}", bytesProcessed, getMegaBytesPerSecond());
	}
	@Override
	public String stop()
	{
		setMessage(getBytesPerSecondString());
		return super.stop();
	}
}
