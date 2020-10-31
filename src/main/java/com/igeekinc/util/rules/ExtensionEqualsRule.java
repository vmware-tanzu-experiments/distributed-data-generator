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

public class ExtensionEqualsRule extends NameRule
{
	static final long serialVersionUID = 4157923628127883533L;
	boolean is;
	String extensionString;
	static MessageFormat areFormatter = new MessageFormat(_("Files whose extensions are \"{0}\""));  //$NON-NLS-1$
	static MessageFormat areNotFormatter = new MessageFormat(_("Files whose extensions are not \"{0}\""));  //$NON-NLS-1$
	/**
	 * @return Returns the endString.
	 */
	public String getExtensionString()
	{
		return extensionString;
	}

	public ExtensionEqualsRule(String inExtensionString, boolean inIs, boolean caseSensitive, boolean excludeSubDirectories)
	{
		super("*."+inExtensionString, caseSensitive, excludeSubDirectories); //$NON-NLS-1$
		extensionString = inExtensionString;
		is = inIs;
	}
	
	public String toString()
	{
		if (is)
			return (areFormatter.format(new Object[]{extensionString}));
		else
			return (areNotFormatter.format(new Object[]{extensionString}));

	}

	  public RuleMatch matchesRule(FileLike checkFile)
	  {
	  	if (is)
	  	{
	  		return(super.matchesRule(checkFile.getName()));
	  	}
	  	else
	  	{
	  	  RuleMatch returnVal = super.matchesRule(checkFile.getName());
			if (returnVal == RuleMatch.kNoMatch)
			{
				returnVal = RuleMatch.kFileMatches;
			}
			else
			{	
				returnVal = RuleMatch.kNoMatch;		
			}
			return(returnVal);
	  	}
	  }
	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.NameRule#matchesRule(java.lang.String)
	 */
	public RuleMatch matchesRule(String checkPath)
	{
		if (is)
			return super.matchesRule(checkPath);
		else
		{
		    RuleMatch returnVal = super.matchesRule(checkPath);
			if (returnVal == RuleMatch.kNoMatch)
			{
				returnVal = RuleMatch.kFileMatches;
			}
			else
			{	
				returnVal = RuleMatch.kNoMatch;		
			}
			return(returnVal);
		}
	}

}
