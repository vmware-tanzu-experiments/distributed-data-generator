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

public class FileCopyProgressIndicator implements FileCopyProgressIndicatorIF
{
	long bytesCopied, bytesToCopy;
	
	public void setBytesToCopy(long bytesToCopy)
	{
		this.bytesToCopy = bytesToCopy;
	}
	
	public void setBytesCopied(long bytesCopied)
	{
		this.bytesCopied = bytesCopied;
	}
	
	public void updateProgress(long bytesCopied)
	{
		this.bytesCopied += bytesCopied;
	}
	
	public long getBytesCopied()
	{
		return bytesCopied;
	}
	
	public long getBytesToCopy()
	{
		return bytesToCopy;
	}

    public long totalUnits()
    {
        return bytesToCopy;
    }

    public long unitsCompleted()
    {
        return bytesCopied;
    }

    public void dataCopyFinished()
    {
        // Here to help out time estimation - no-op's until we add that functionality
        
    }

    public void startingDataCopy()
    {
     // Here to help out time estimation - no-op's until we add that functionality
        
    }
	
	
}
