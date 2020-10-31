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
import java.io.File;
import java.text.MessageFormat;

import com.igeekinc.util.FileLike;

public class SubDirectoryOfRule extends NameRule
{
	public static final long serialVersionUID=-8737728500441711260L;
	File directory;
	boolean is;
	MessageFormat areFormatter = new MessageFormat(_("Files that are contained by the folder {0}")); //$NON-NLS-1$
	MessageFormat areNotFormatter = new MessageFormat(_("Files that are not contained by the folder {0}")); //$NON-NLS-1$
	
	public SubDirectoryOfRule(File inDirectory, boolean is)
	{
		super(inDirectory.getAbsolutePath()+File.separator+"**", true, true); //$NON-NLS-1$
		directory = inDirectory;
		this.is = is;
	}
	/**
	 * @return Returns the directory.
	 */
	public File getDirectory()
	{
		return directory;
	}

	public String toString()
	{
		if (is)
			return (areFormatter.format(new Object[]{directory.getAbsolutePath()}));
		else
			return (areNotFormatter.format(new Object[]{directory.getAbsolutePath()}));

	}
	public RuleMatch matchesRule(FileLike checkFile)
	{
		return(matchesRule(checkFile.getAbsolutePath()));
	}
	
	public RuleMatch matchesRule(String checkPath)
	{
	    RuleMatch returnVal = super.matchesRule(checkPath);
		
		if (is)
		{	
            if (returnVal == RuleMatch.kFileMatches)
                return RuleMatch.kSubdirsMatch;
			return returnVal;
		}
		else
		{
			if (returnVal == RuleMatch.kFileMatches)
				return RuleMatch.kNoMatch;
			else
				return RuleMatch.kFileMatches;
		}
	}
}

