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

public abstract class SizeRule implements Rule
{
	static final long serialVersionUID = 6075251204893564128L;
	long size, kbSize;
	
	public SizeRule(long inKBSize)
	{
		kbSize = inKBSize;
		size = kbSize * 1024;;
	}

	/**
	 * @return Returns the size.
	 */
	public long getSize()
	{
		return size;
	}

	public void init()
	{
		// nothing to do
	}
}
