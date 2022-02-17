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

import com.igeekinc.util.async.AsyncCompletion;
import com.igeekinc.util.async.ComboFutureBase;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;



class CompositeFuture extends ComboFutureBase<Integer>
{
	private boolean done;
	private boolean [] finished;
	private int numFinished;
	private int total;
	
	public CompositeFuture(int numComposites)
	{
		init(numComposites);
	}

	private void init(int numComposites)
	{
		finished = new boolean[numComposites];
		numFinished = 0;
		done = false;
	}
	
	@SuppressWarnings("unchecked")
	public <A> CompositeFuture(int numComposites, AsyncCompletion<Integer, ? super A>completionHandler, A attachment)
	{
		super(completionHandler, attachment);
		init(numComposites);
	}
	
	@Override
	public boolean cancel(boolean paramBoolean)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCancelled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public synchronized void completed(Integer result, Object attachment)
	{
		Integer finishedComposite = (Integer)attachment;
		if (!finished[finishedComposite])
		{
			finished[finishedComposite] = true;
			numFinished++;
			total += result;
			if (numFinished >= finished.length)
				setDone();	// We're done!
		}
	}
	
}
/**
 * A data descriptor made up of a list of data descriptors
 *
 */
public class CompositeDataDescriptor implements DataDescriptor
{
	private static final long	serialVersionUID	= -6730807092293159912L;
	private DataDescriptor [] dataDescriptors;
	private long length;
	private boolean accessible;
	private boolean localShareable, remoteShareable;
	private boolean descriptorContainsData;
	private TreeMap<Long, DataDescriptor>offsetMap;
	
	public CompositeDataDescriptor(DataDescriptor [] dataDescriptors)
	{
		this.dataDescriptors = new DataDescriptor[dataDescriptors.length];
		length = 0;
		accessible = true;
		localShareable = true;
		remoteShareable = true;
		descriptorContainsData = true;
		
		offsetMap = new TreeMap<Long, DataDescriptor>();
		for (int curDataDescriptorNum = 0; curDataDescriptorNum < dataDescriptors.length; curDataDescriptorNum++)
		{
			DataDescriptor curDataDescriptor = dataDescriptors[curDataDescriptorNum];
			this.dataDescriptors[curDataDescriptorNum] = dataDescriptors[curDataDescriptorNum];
			offsetMap.put(length, curDataDescriptor);
			length += curDataDescriptor.getLength();
			if (!curDataDescriptor.isAccessible())
				accessible = false;	// Only one bad apple
			if (!curDataDescriptor.isShareableWithLocalProcess())
				localShareable = false;
			if (!curDataDescriptor.isShareableWithRemoteProcess())
				remoteShareable = false;
			if (!curDataDescriptor.descriptorContainsData())
				descriptorContainsData = false;
		}
	}
	@Override
	public void writeData(OutputStream destinationStream) throws IOException
	{
		for (DataDescriptor curDescriptor:dataDescriptors)
		{
			curDescriptor.writeData(destinationStream);
		}
	}

	@Override
	public void writeData(FileOutputStream destinationStream) throws IOException
	{
		for (DataDescriptor curDescriptor:dataDescriptors)
		{
			curDescriptor.writeData(destinationStream);
		}
	}
	
	@Override
	public InputStream getInputStream() throws IOException
	{
		return new DataDescriptorInputStream(this);
	}

	@Override
	public byte[] getData() throws IOException
	{
		if (length > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Descriptor too large");
		// Probably not a good idea to call this
		byte [] returnData = new byte[(int)length];
		getData(returnData, 0, 0L, (int)length, false);
		return returnData;
	}

	@Override
	public ByteBuffer getByteBuffer() throws IOException
	{
		if (length > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Descriptor too large");
		ByteBuffer returnBuffer = ByteBuffer.allocate((int)length);
		getData(returnBuffer, 0, (int)length, false);
		returnBuffer.position(0);
		return returnBuffer;
	}
	
	@Override
	public int getData(byte[] destination, int destOffset, long srcOffset,
			int length, boolean release) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.wrap(destination);
		buffer.position(destOffset);
		return getData(buffer, srcOffset, length, release);
	}

	@Override
	public int getData(ByteBuffer destination, long srcOffset, int length,
			boolean release) throws IOException
	{
		int bytesRead = 0, bytesToRead = length;
		long curOffset = srcOffset;
		while (bytesToRead > 0)
		{
			Map.Entry<Long, DataDescriptor> retrieveEntry = offsetMap.floorEntry(curOffset);
			if (retrieveEntry == null)
				break;
			long retrieveStartOffset = retrieveEntry.getKey();
			long curRetrieveOffset = curOffset - retrieveStartOffset;	// Where we will start from in the current data descriptor
			DataDescriptor curDataDescriptor = retrieveEntry.getValue();
			long curLength = curDataDescriptor.getLength() - curRetrieveOffset;
			if (curLength > bytesToRead)
				curLength = bytesToRead;
			int curBytesRead = curDataDescriptor.getData(destination, curRetrieveOffset, (int)curLength, release);
			bytesToRead -= curBytesRead;
			curOffset += curBytesRead;
			bytesRead += curBytesRead;
		}
		return bytesRead;
	}
	
	
	@Override
	public Future<Integer> getDataAsync(ByteBuffer destination, long srcOffset,
			int length, boolean release) throws IOException
	{
		CompositeFuture returnFuture = new CompositeFuture(dataDescriptors.length);
		int bytesToRead = length;
		long curOffset = srcOffset;
		int curFutureNum = 0;
		while (bytesToRead > 0)
		{
			Map.Entry<Long, DataDescriptor> retrieveEntry = offsetMap.floorEntry(curOffset);
			if (retrieveEntry == null)
				break;
			long retrieveStartOffset = retrieveEntry.getKey();
			long curRetrieveOffset = curOffset - retrieveStartOffset;	// Where we will start from in the current data descriptor
			DataDescriptor curDataDescriptor = retrieveEntry.getValue();
			long curLength = curDataDescriptor.getLength() - curRetrieveOffset;
			if (curLength > bytesToRead)
				curLength = bytesToRead;
			curDataDescriptor.getDataAsync(destination, curRetrieveOffset, (int)curLength, release, (Integer)curFutureNum, returnFuture);
			curFutureNum ++;
			bytesToRead -= curLength;
			curOffset += curLength;
		}
		return returnFuture;
	}
	
	@Override
	public <A> void getDataAsync(ByteBuffer destination, long srcOffset,
			int length, boolean release, A attachment,
			AsyncCompletion<Integer, ? super A> handler) throws IOException
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public long getLength()
	{
		return length;
	}

	@Override
	public boolean isShareableWithLocalProcess()
	{
		return localShareable;
	}

	@Override
	public boolean isShareableWithRemoteProcess()
	{
		return remoteShareable;
	}

	@Override
	public boolean isAccessible()
	{
		return accessible;
	}

	@Override
	public boolean descriptorContainsData()
	{
		return descriptorContainsData;
	}

	@Override
	public void close() throws IOException
	{
		for (int curDataDescriptorNum = 0; curDataDescriptorNum < dataDescriptors.length; curDataDescriptorNum++)
		{
			DataDescriptor curDataDescriptor = dataDescriptors[curDataDescriptorNum];
			curDataDescriptor.close();
		}
	}
}
