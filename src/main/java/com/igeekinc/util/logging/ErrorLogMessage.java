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
 
package com.igeekinc.util.logging;

import java.io.Serializable;

public class ErrorLogMessage extends LocalizableLogMessage
{
	private static final long serialVersionUID = -712897052207779862L;

	public ErrorLogMessage(String packageName, String key, Serializable [] inArgs)
	{
		super(packageName, key, inArgs);
	}

	public ErrorLogMessage(String formatString, Serializable... inArgs)
	{
		super(formatString, inArgs);
	}
	
	public ErrorLogMessage(String formatString)
	{
		super(formatString);
	}
	
	public ErrorLogMessage(String bundleName, String key)
	{
		super(bundleName, key, null);
	}
}
