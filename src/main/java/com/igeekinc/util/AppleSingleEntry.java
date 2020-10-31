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

public class AppleSingleEntry implements Comparable<AppleSingleEntry>
{
	public static final int kDataForkID = 1;
	public static final int kResourceForkID = 2;
	public static final int kRealName = 3;
	public static final int kComment = 4;
	public static final int kBWIcon = 5;
	public static final int kColorIcon = 6;
	public static final int kDatesInfo = 8;
	public static final int kFinderInfo = 9;
	public static final int kMacFileInfo = 10;
	public static final int kProDOSFileInfo = 11;
	public static final int kMSDOSFileInfo = 12;
	public static final int kShortName = 13;
	public static final int kAFPFileInfo = 14;
	public static final int kDirectoryID = 15;
	
	int entryID;
	long offset;
	long length;
	boolean isCompleted = false;
	DataSource dataSource;
	
	public AppleSingleEntry(int inEntryID, long inLength, DataSource inDataSource)
	{
		entryID = inEntryID;
		length = inLength;
		dataSource = inDataSource;
	}
	
	public void setOffset(long inOffset)
	{
		offset = inOffset;
		isCompleted = true;
	}
	
	public void setDataSource(DataSource inDataSource)
	{
		dataSource = inDataSource;
	}
	/**
	 * 
	 * @return
	 */
	public int getEntryID()
	{
		return entryID;
	}

	/**
	 * @return
	 */
	public long getLength()
	{
		return length;
	}

	/**
	 * @return
	 */
	public long getOffset()
	{
		return offset;
	}
	
	public DataSource getDataSource()
	{
		return dataSource;
	}
	
	public int compareTo(AppleSingleEntry compareEntry)
	{
		long diff = offset - compareEntry.offset;
		if (diff > 0)
			return 1;
		if (diff < 0)
			return -1;
		return 0;
	}
}
