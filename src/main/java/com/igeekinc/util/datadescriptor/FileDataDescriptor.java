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

import com.igeekinc.util.ClientFile;
import com.igeekinc.util.ForkChannel;
import com.igeekinc.util.async.AsyncCompletion;
import com.igeekinc.util.exceptions.ForkNotFoundException;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;



public class FileDataDescriptor implements DataDescriptor
{
    private static final long serialVersionUID = -2371149894573772992L;
    ClientFile sourceFile;
    String forkName;
    long offset, descriptorLength;
    transient ForkChannel fileChannel;
    
    public FileDataDescriptor(ClientFile sourceFile) throws ForkNotFoundException, IOException
    {
        this(sourceFile, "data", 0L, sourceFile.length());
    }
    
    public FileDataDescriptor(ClientFile sourceFile, long offset, long length) throws ForkNotFoundException, IOException
    {
        this(sourceFile, "data", offset, length);
    }
    
    public FileDataDescriptor(ClientFile sourceFile, String forkName, long offset, long length) throws ForkNotFoundException, IOException
    {
        this.sourceFile = sourceFile;
        this.offset = offset;
        this.descriptorLength = length;
        this.forkName = forkName;
        if (!sourceFile.exists())
            throw new IOException(sourceFile+" does not exist!");
        if (sourceFile.isDirectory())
            throw new IOException(sourceFile+" cannot be a directory!");
        long forkLength = sourceFile.getMetaData().getForkLength(forkName);
        if (offset > forkLength)
            throw new IOException(offset+" is beyond end of file "+forkLength+" for "+sourceFile);
        if (length + offset > forkLength)
            throw new IOException(length+" + "+offset+" is beyond end of file "+forkLength+" for "+sourceFile);
    }

    @Override
    public byte[] getData() throws IOException
    {
		if (descriptorLength > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Descriptor too large");
        byte [] returnData = new byte[(int)descriptorLength];
        getData(returnData, 0, 0L, (int)descriptorLength, false);
        return returnData;
    }

    @Override
    public ByteBuffer getByteBuffer() throws IOException
    {
		if (descriptorLength > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Descriptor too large");
		ByteBuffer returnBuffer = ByteBuffer.allocate((int)descriptorLength);
		getData(returnBuffer, 0, (int)descriptorLength, false);
		returnBuffer.position(0);
		return returnBuffer;
    }
    
    @Override
    public synchronized int getData(byte[] destination, int destOffset, long srcOffset,
            int length, boolean release) throws IOException, ForkNotFoundException
    {
    	ByteBuffer readBuffer = ByteBuffer.wrap(destination, destOffset, length);
    	return getData(readBuffer, srcOffset, length, release);
    }
    
    @Override
	public int getData(ByteBuffer destination, long srcOffset, int length,
			boolean release) throws IOException
	{
        if (fileChannel == null)
            fileChannel = new ForkChannel(sourceFile, forkName, true, false);
        long fileOffset = offset + srcOffset;
        fileChannel.position(fileOffset);
        return fileChannel.read(destination);
	}

	@Override
	public synchronized Future<Integer> getDataAsync(ByteBuffer destination, long srcOffset,
			int length, boolean release) throws IOException
	{
		int bytesRead = 0;
		Throwable caughtException = null;
		try
		{
			bytesRead = getData(destination, srcOffset, length, release);
		}
		catch (Throwable t)
		{
			caughtException = t;
		}
		return new DummyFuture(bytesRead, caughtException);
	}

	@Override
	public <A> void getDataAsync(ByteBuffer destination, long srcOffset,
			int length, boolean release, A attachment,
			AsyncCompletion<Integer, ? super A> handler) throws IOException
	{
		try
		{
			int bytesRead = getData(destination, srcOffset, length, release);
			handler.completed(bytesRead, attachment);
		}
		catch (Throwable t)
		{
			handler.failed(t, attachment);
		}
	}

 @Override
	public InputStream getInputStream() throws IOException
    {
        return new DataDescriptorInputStream(this);
    }

    @Override
    public long getLength()
    {
        return descriptorLength;
    }

    @Override
    public void writeData(OutputStream destinationStream) throws IOException
    {
    	throw new UnsupportedOperationException();
    }
    
    @Override
    public void writeData(FileOutputStream destinationStream) throws IOException
    {
    	throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isAccessible()
    {
        try
        {
        	if (fileChannel == null)
        		fileChannel = new ForkChannel(sourceFile, forkName, true, false);
            if (fileChannel != null)
                return true;
        } catch (IOException e)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
        }
        return false;
    }

    @Override
    public boolean isShareableWithLocalProcess()
    {
        return true;    // We can serialize and send this somewhere else
    }

    @Override
    public boolean isShareableWithRemoteProcess()
    {
        return false;    // This can't be used on a remote host
    }

    @Override
    public boolean descriptorContainsData()
    {
        return false;
    }
    
    @Override
    public void close() throws IOException
    {
        if (fileChannel != null)
        {
            fileChannel.close();
            fileChannel = null;
        }
    }
}
