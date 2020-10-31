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
 
package com.igeekinc.util.linux;

import com.igeekinc.util.FilePath;
import com.igeekinc.util.macos.macosx.MacOSXFilePath;
import com.igeekinc.util.windows.WindowsFilePath;

public class LinuxFilePath extends FilePath
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3960915930103434028L;
	private char [] separatorChar ={ '/'};
	private String separator ="/";
	
	public LinuxFilePath(String path, boolean normalize)
	{
		boolean absolute=false;
		if (path.startsWith(separator))
			absolute = true;
		init(path, separatorChar, normalize, absolute);
	}

	public LinuxFilePath(String [] components, boolean isAbsolute)
	{
		init(components, 0, components.length, isAbsolute);
	}
	
	private LinuxFilePath(String [] components, int offset, int count, boolean isAbsolute)
	{
		init(components, offset, count, isAbsolute);
	}
	
	private LinuxFilePath()
	{
		// Internal use only
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.FilePath#getNewFilePath(java.lang.String[], int, int, boolean)
	 */
	protected FilePath getNewFilePath(
		String[] components,
		int offset,
		int count,
		boolean isAbsolute)
	{
		return new LinuxFilePath(components, offset, count, isAbsolute);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    public String toString()
    {
        StringBuffer pathString = toStringBuf(separator);
        if (isAbsolute && super.count == 1)
            pathString = new StringBuffer(separator);
        return(pathString.toString());
    }

	@Override
	public boolean isPath(String checkString) 
	{
		return (checkString.indexOf(separator) >= 0);
	}

	
	@Override
	public FilePath makeAbsolute()
	{
		if (isAbsolute)
			return this;
		LinuxFilePath returnPath = new LinuxFilePath();
		String [] absolutePathComponents = new String[count + 1];
		absolutePathComponents[0] = "";
		System.arraycopy(pathComponents, offset, absolutePathComponents, 1, count);
		returnPath.init(absolutePathComponents, 0, absolutePathComponents.length, true);
		return returnPath;
	}
}
