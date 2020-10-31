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
 
package com.igeekinc.util.windows;

import com.igeekinc.util.FilePath;

public class WindowsFilePath extends FilePath 
{
	private static final long serialVersionUID = 2051855680582454475L;
	private char [] parsingSeparators={'\\', '/'};	// Accept \ and / as separators
	private String separator="\\";	// Only output \'s
	public WindowsFilePath(String path, boolean normalize)
	{
		boolean absolute=false;
		if (path.length() > 0)
		{
			int firstSeparatorPos = path.indexOf(parsingSeparators[0]);
			if (path.indexOf(parsingSeparators[1]) > firstSeparatorPos)
				firstSeparatorPos = path.indexOf(parsingSeparators[1]);
			if (firstSeparatorPos > 0) 
			{
				int colonPos = path.indexOf(':');
				if (colonPos > -1)
				{
					if (colonPos > 1 && colonPos < firstSeparatorPos)
					{
						throw new IllegalArgumentException("Only single letter drive names allowed (parsing path="+path+")");
					}
					absolute=true;
					if (path.length() < 3)
						path = path.concat(separator);  // Make sure we have at least one character
				}
			}
			else
			{
				if (firstSeparatorPos == 0)
					absolute = true;
			}
		}
		init(path, parsingSeparators, normalize, absolute);

	}

	public WindowsFilePath(String []components, boolean absolute)
	{
		this(components, 0, components.length, absolute);
	}
	
	public WindowsFilePath(String [] components, int offset, int length, boolean absolute)
	{
		if (offset == 0 && length > 0 && components[offset].indexOf(':') > 0)
		{
			int colonPos = components[offset].indexOf(':');
			if (colonPos == 1 && components[offset].length() == 2)
				absolute = true;
		}
		init(components, offset, length, absolute);
	}

	private WindowsFilePath()
	{
	}
	/**
	 * @return Returns the driveLetter.
	 */
	public String getDriveLetter() {
		if (isAbsolute)
			return pathComponents[0].substring(0, 1);	//Remove the :
		return "";
	}

    /* (non-Javadoc)
     * @see com.igeekinc.util.FilePath#isPath(java.lang.String)
     */
    public boolean isPath(String checkString) 
    {
    	for (char checkSeparator:parsingSeparators)
    		if (checkString.indexOf(checkSeparator) >= 0)
    			return true;
    	return false;
    }

	@Override
	protected FilePath getNewFilePath(String[] components, int offset,
			int count, boolean isAbsolute) 
	{
		WindowsFilePath returnPath = new WindowsFilePath(components, offset, count, false);
		if (isAbsolute)
			returnPath.isAbsolute = isAbsolute;
		return returnPath;
	}

	@Override
	public String toString() 
	{
		StringBuffer returnBuf = new StringBuffer();
		for (int curComponentNum = 0; curComponentNum < count; curComponentNum++)
		{
			returnBuf.append(pathComponents[offset + curComponentNum]);
			if (curComponentNum < count - 1 || (isAbsolute && curComponentNum == 0))
				returnBuf.append(separator);
		}
		return returnBuf.toString();
	}

	@Override
	public FilePath makeAbsolute()
	{
		if (isAbsolute)
			return this;
		WindowsFilePath returnPath = new WindowsFilePath();
		returnPath.init(pathComponents, offset, count, true);
		return returnPath;
	}
}
