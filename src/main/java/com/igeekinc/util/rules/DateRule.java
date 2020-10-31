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

import java.io.IOException;

import com.igeekinc.util.ClientFile;
import com.igeekinc.util.FileLike;

public abstract class DateRule implements Rule
{
	static final long serialVersionUID = -3241058030971537260L;
	private long startTime, endTime;
	private int dateField;
	
	public static final int kModifiedTime = 0;
	public static final int kCreatedTime = 1;
	
	//private String describeString;

	protected void init(long startTime, long endTime, int dateField)
	{
		this.startTime = startTime;
		this.endTime = endTime;
		this.dateField = dateField;
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.Rule#matchesRule(com.igeekinc.util.ClientFile)
	 */
	public RuleMatch matchesRule(FileLike checkFileLike)
	{
		ClientFile checkFile = (ClientFile)checkFileLike;
		long fileTime = -1;
		switch(dateField)
		{
			case kModifiedTime:
				fileTime = checkFile.lastModified();
				break;
			case kCreatedTime:
				try
				{
					fileTime = checkFile.getMetaData().getCreateTime().getTime();
				}
				catch (IOException e)
				{
					return(RuleMatch.kNoMatch);
				}
		}
		if (fileTime >= startTime && fileTime <= endTime)
			return(RuleMatch.kFileMatches);
		return(RuleMatch.kNoMatch);
	}

	/*
	public String toString()
	{
		return describeString;
	}
	*/
	public void init()
	{
		// nothing to do
	}
	
	protected int getDateField()
	{
		return dateField;
	}
	
	protected long getStartTime()
	{
		return startTime;
	}
	
	protected long getEndTime()
	{
		return endTime;
	}
}
