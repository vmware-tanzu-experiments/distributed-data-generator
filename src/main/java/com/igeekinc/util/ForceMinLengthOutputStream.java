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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ForceMinLengthOutputStream extends FilterOutputStream
{
	long bytesWritten = 0, minLength;
	public ForceMinLengthOutputStream(OutputStream inner, long inMinLength)
	{
		super(inner);
		minLength = inMinLength;
	}
	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException
	{
		if (bytesWritten % minLength != 0)
		{
			int numExtraBytes = (int)(minLength - (bytesWritten%minLength));
			for (int curSpareByteNum = 0; 
			curSpareByteNum < numExtraBytes; curSpareByteNum++)
			{
				super.write(0);
			}
		}
		super.close();
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException
	{
		// TODO Auto-generated method stub
		super.flush();
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) throws IOException
	{
		// TODO Auto-generated method stub
		out.write(b, off, len);
		bytesWritten += len;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException
	{
		// TODO Auto-generated method stub
		out.write(b);
		bytesWritten += b.length;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int b) throws IOException
	{
		// TODO Auto-generated method stub
		out.write(b);
		bytesWritten++;
	}

}
