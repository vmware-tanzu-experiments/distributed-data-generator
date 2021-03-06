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

public class NameEqualsRule extends NameRule
{
	static final long serialVersionUID = 8738733921589143941L;
	String name;
	static MessageFormat formatter = new MessageFormat(_("Files named \"{0}\""));  //$NON-NLS-1$
	public NameEqualsRule(String inName, boolean caseSensitive, boolean excludeSubDirectories)
	{
		super(inName, caseSensitive, excludeSubDirectories);
		name = inName;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	public String toString()
	{
		return(formatter.format(new Object[]{name}));
	}
	
	public RuleMatch matchesRule(String checkPath)
	{
	    RuleMatch fullNameMatch = super.matchesRule(checkPath);
		if (fullNameMatch != RuleMatch.kNoMatch)
			return fullNameMatch;
		int lastDotPos = checkPath.lastIndexOf("."); //$NON-NLS-1$
		if (lastDotPos > 0)
		{
			checkPath = checkPath.substring(0, lastDotPos);
			return(super.matchesRule(checkPath));
		}
		return(RuleMatch.kNoMatch);
	}
}
