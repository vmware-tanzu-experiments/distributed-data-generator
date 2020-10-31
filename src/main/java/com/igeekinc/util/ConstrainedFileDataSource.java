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
 
package com.igeekinc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ConstrainedFileDataSource implements DataSource
{
	File sourceFile;
	long startOffset, length;
	
	public ConstrainedFileDataSource(File inSourceFile, long inStartOffset, long inLength)
	{
		sourceFile = inSourceFile;
		startOffset = inStartOffset;
		length = inLength;
	}
	/* (non-Javadoc)
	 * @see com.igeekinc.util.DataSource#getInputStream()
	 */
	public InputStream getInputStream()
	throws java.io.IOException
	{
		FileInputStream fis = new FileInputStream(sourceFile);
		ConstrainedInputStream returnStream = new ConstrainedInputStream(fis,
			startOffset, length);
		return returnStream;
	}

}
