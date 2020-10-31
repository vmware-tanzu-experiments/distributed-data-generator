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

import java.io.IOException;
import java.io.InputStream;

public class ConstrainedInputStream extends InputStream
{
	InputStream baseStream;
	long offset, length, bytesRead;
	
	public ConstrainedInputStream(InputStream inBaseStream, long inOffset, long inLength)
	throws IOException
	{
		baseStream = inBaseStream;
		offset = inOffset;
		length = inLength;
		baseStream.skip(offset);
		bytesRead = 0;
	}
	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public int read() 
		throws IOException
	{
		if (bytesRead > length)
			return(-1);
		bytesRead++;
		return baseStream.read();
	}

	public int read(byte [] b)
		throws IOException
	{
		return(read(b, 0, b.length));
	}
	
	public int read(byte [] b, int offset, int readLength)
		throws IOException
	{
		if (bytesRead > length)
			return(-1);
			
		if (bytesRead + readLength > length)
		{
			readLength = (int)(length - bytesRead);
		}
		int returnBytesRead = baseStream.read(b, offset, readLength);
		bytesRead += returnBytesRead;
		return(returnBytesRead);
	}
	
	public void close()
	throws IOException
	{
		baseStream.close();
	}
}
