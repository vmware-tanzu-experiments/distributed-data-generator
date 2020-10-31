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
 
package com.igeekinc.util.datadescriptor;

import java.io.IOException;
import java.io.InputStream;

public class DataDescriptorInputStream extends InputStream
{
    DataDescriptor dataSource;
    long offset;
    long lastMark;
    public DataDescriptorInputStream(DataDescriptor dataSource)
    {
        this.dataSource = dataSource;
        offset = 0L;
        lastMark = 0L;
    }
    
    @Override
    public int available() throws IOException
    {
        return 0;
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        lastMark = offset;
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (offset < dataSource.getLength())
        {
        	boolean release = false;
        	if (offset + len >= dataSource.getLength())
        		release = true;
            int bytesRead = dataSource.getData(b, off, offset, len, release);
            if (bytesRead > 0)
                offset += bytesRead;
            return bytesRead;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized void reset() throws IOException
    {
        offset = lastMark;
    }

    @Override
    public long skip(long n) throws IOException
    {
        if (n <= 0)
            return 0;
        long bytesToSkip = n;
        if (bytesToSkip + offset > dataSource.getLength())
            bytesToSkip = dataSource.getLength() - offset;
        offset += bytesToSkip;
        return bytesToSkip;
    }

    @Override
    public int read() throws IOException
    {
        if (offset < dataSource.getLength())
        {
            byte [] readByte = new byte[1];
        	boolean release = false;
        	if (offset + 1 >= dataSource.getLength())
        		release = true;
            dataSource.getData(readByte, 0, offset, 1, release);
            offset++;
            return (((int)readByte[0]) & 0xff);
        }
        else
        {
            return -1;      // No more data
        }
    }

    @Override
    public void close() throws IOException
    {
        dataSource.close();
    }
    
    
}
