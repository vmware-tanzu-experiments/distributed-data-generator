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

import static com.igeekinc.util.rules.Internationalization._;
import java.text.MessageFormat;

public class NameContainsRule extends NameRule
{
	static final long serialVersionUID = -6207664795110002560L;
	String containsString;
	static MessageFormat formatter = new MessageFormat(_("Files whose names contain \"{0}\""));  //$NON-NLS-1$
	/**
	 * @return Returns the endString.
	 */
	public String getContainsString()
	{
		return containsString;
	}

	public NameContainsRule(String inContainsString, boolean caseSensitive, boolean excludeSubDirectories)
	{
		super("*"+inContainsString+"*", caseSensitive, excludeSubDirectories); //$NON-NLS-1$ //$NON-NLS-2$
		containsString = inContainsString;
	}
	
	public String toString()
	{
		return (formatter.format(new Object[]{containsString}));
	}
}
