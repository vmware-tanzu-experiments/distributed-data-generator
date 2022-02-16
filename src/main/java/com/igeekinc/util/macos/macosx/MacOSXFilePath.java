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
 
package com.igeekinc.util.macos.macosx;

import com.igeekinc.util.ClientFile;
import com.igeekinc.util.ClientFileMetaData;
import com.igeekinc.util.FilePath;
import com.igeekinc.util.SystemInfo;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;



public class MacOSXFilePath extends FilePath
{
	private static final long serialVersionUID = -1835941789300233309L;

	private char [] separatorChar = {'/'};
    private String separator=new String(separatorChar);
	public MacOSXFilePath(String path, boolean normalize)
	{
		boolean absolute=false;
		if (path.startsWith(separator))
			absolute = true;
		init(path, separatorChar, normalize, absolute);
	}

    public MacOSXFilePath(String [] components, boolean isAbsolute)
    {
        init(components, 0, components.length, isAbsolute);
    }
    
	private MacOSXFilePath(String [] components, int offset, int count, boolean isAbsolute)
	{
		init(components, offset, count, isAbsolute);
	}
	
	private MacOSXFilePath()
	{
		// internal use only
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.FilePath#getNewFilePath(java.lang.String[], int, int, boolean)
	 */
 @Override
	protected FilePath getNewFilePath(
		String[] components,
		int offset,
		int count,
		boolean isAbsolute)
	{
		return new MacOSXFilePath(components, offset, count, isAbsolute);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
 @Override
	public String toString()
	{
		StringBuffer pathString = toStringBuf(separator);
        if (isAbsolute && super.count == 1)
            pathString = new StringBuffer(separator);
        return(pathString.toString());
	}

    /* (non-Javadoc)
     * @see com.igeekinc.util.FilePath#isPath(java.lang.String)
     */
    @Override
    public boolean isPath(String checkString) 
    {
        return (checkString.indexOf(separator) >= 0);
    }
    
    public FilePath normalizeSymlinks()
    {
        FilePath returnPath = FilePath.getFilePath("/");
        for (int curComponentNum = 1; curComponentNum < getNumComponents(); curComponentNum++)
        {
            FilePath checkPath = subpath(0, curComponentNum + 1);
            try
            {
                ClientFile checkFile = SystemInfo.getSystemInfo().getClientFileForPath(checkPath);
                ClientFileMetaData md = checkFile.getMetaData();
                if (md.isSymlink())
                {
                    String symPathStr = md.getSymlinkTarget();
                    FilePath symPath = FilePath.getFilePath(symPathStr);
                    if (symPath.isAbsolute())
                        returnPath = symPath;
                    else
                        returnPath = returnPath.getChild(symPath);
                }
                else
                {
                    returnPath = returnPath.getChild(checkPath.getName());
                }
            } catch (IOException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
                return this;
            }
            
            
        }
        return returnPath;
    }
    
	@Override
	public FilePath makeAbsolute()
	{
		if (isAbsolute)
			return this;
		MacOSXFilePath returnPath = new MacOSXFilePath();
		String [] absolutePathComponents = new String[count + 1];
		absolutePathComponents[0] = "";
		System.arraycopy(pathComponents, offset, absolutePathComponents, 1, count);
		returnPath.init(absolutePathComponents, 0, absolutePathComponents.length, true);
		return returnPath;
	}
}
