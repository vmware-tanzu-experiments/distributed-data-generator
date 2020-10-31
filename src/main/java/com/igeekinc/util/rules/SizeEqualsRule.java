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

import com.igeekinc.util.FileLike;

public class SizeEqualsRule extends SizeRule
{
	static final long serialVersionUID = -8437092590553244922L;
	static MessageFormat formatter = new MessageFormat(_("Files whose size is {0} KB")); //$NON-NLS-1$
	/**
	 * @param inSize
	 */
	public SizeEqualsRule(long inSize)
	{
		super(inSize);
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.Rule#matchesRule(com.igeekinc.util.ClientFile)
	 */
	public RuleMatch matchesRule(FileLike checkFile)
	{
		long fileLength = checkFile.length()/1024;
		long checkSize = size/1024;
		if (checkFile.isFile() && fileLength == checkSize)
			return RuleMatch.kFileMatches;
		return RuleMatch.kNoMatch;
	}
	
	public String toString()
	{
		return (formatter.format(new Object[]{new Long(kbSize)}));
	}
	
}
