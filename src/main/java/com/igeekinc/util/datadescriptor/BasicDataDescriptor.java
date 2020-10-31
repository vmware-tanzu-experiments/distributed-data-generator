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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.Future;

import com.igeekinc.util.DirectMemoryUtils;
import com.igeekinc.util.async.AsyncCompletion;

public class BasicDataDescriptor implements DataDescriptor
{
	private static final long serialVersionUID = 1100966811926847677L;
	/**
	 * ByteBuffers are a pain because of the internal position and limit.  In order to avoid headaches and thread disasters, ByteBuffer
	 * is used in a specific manner within a BasicDataDescriptor.
	 * 
	 * The byteBuffer ByteBuffer always has a position of 0.  The limit is the end of available data.  When we create based on an external
	 * ByteBuffer, we slice() the ByteBuffer immediately so that the position is set to 0.  Similarly, when creating a ByteBuffer from a
	 * segment of an array we need to slice() the ByteBuffer immediately afterwards or the position will be the offset into the byte array.
	 * 
	 * For any internal accesses to ByteBuffer, usually getByteBuffer() is the right way to access it.  This will duplicate() the ByteBuffer
	 * so there are no worries about changing the postion or limit.
	 * 
	 * 	Bytes buffers are not necessarily serializable - we'll handle our own serialization
	 */

    private transient ByteBuffer byteBuffer;	
    
    /**
     * Only for subclasses that know what they're doing
     */
    protected BasicDataDescriptor()
    {
    	
    }
    
    /**
     * Creates a new BasicDataDescriptor from the given ByteBuffer.  The ByteBuffer will be duplicated, so the
     * position, etc. will not be modified by operations on BasicDataDescriptor.  However, operations on the underlying buffer
     * will affect the related buffer.  The buffer's position must be 0 - if you want to use a subsection, slice the buffer
     * before calling
     * @param data
     */
    public BasicDataDescriptor(ByteBuffer data)
    {
    	if (data.position() != 0)
    		throw new IllegalStateException("data.position() != 0 ");
    	byteBuffer = data.slice();	// We will eventually remove the limit on position == 0, so slice() so that position will be == 0
    }
    
    public BasicDataDescriptor(byte [] data)
    {
        this(data, 0, data.length);
    }
    
    public BasicDataDescriptor(DataDescriptor source) 
    throws IOException
    {
        this(source.getInputStream(), (int)source.getLength());
    }
    
    public BasicDataDescriptor(DataDescriptor source, long offset, int length) 
    throws IOException
    {
        byteBuffer = ByteBuffer.allocate(length);
        source.getData(byteBuffer, offset, length, false);
        byteBuffer.position(0);
    }

    public BasicDataDescriptor(byte [] inData, int offset, int length)
    {
        init(inData, offset, length);
    }
    
    /**
     * Wraps the data in the data descriptor - changing the array MAY change the data in the descriptor
     * @param inData
     * @param offset
     * @param length
     */
    protected void init(byte [] inData, int offset, int length)
    {
    	byte [] wrapData;
    	if (offset != 0 || length != inData.length)
    	{
    		wrapData = new byte[length];
    		System.arraycopy(inData, offset, wrapData, 0, length);
    	}
    	else
    	{
    		wrapData = inData;
    	}
    	ByteBuffer positionedBuffer = ByteBuffer.wrap(wrapData, 0, length);
		byteBuffer = positionedBuffer.slice();	// Stupid ByteBuffer needs to be sliced so that position will be 0, not the offset into the buffer
    }

    public BasicDataDescriptor(FileInputStream inputStream, int length)
    throws IOException
    {
    	this(inputStream.getChannel(), length);
    }
    
    public BasicDataDescriptor(InputStream inputStream, int length)
    throws IOException
    {
    	int bytesRead;
    	byte [] dataBytes = new byte[length];
    	bytesRead = inputStream.read(dataBytes);
    	byteBuffer = ByteBuffer.wrap(dataBytes, 0, bytesRead);
    	if (bytesRead < 0)
    		throw new IOException("Unexpected end of stream");
    }

    public BasicDataDescriptor(FileChannel inputChannel, int length) throws IOException
    {
    	byteBuffer = ByteBuffer.allocate(length);
    	int bytesRead = inputChannel.read(byteBuffer);
    	if (bytesRead < 0)
            throw new IOException("Unexpected end of stream");
    	byteBuffer.position(0);
    }
    
    public BasicDataDescriptor(FileChannel inputChannel, long position, int length) throws IOException
    {
    	byteBuffer = ByteBuffer.allocate(length);
    	inputChannel.position(position);
    	int bytesRead = inputChannel.read(byteBuffer);
    	if (bytesRead < 0)
            throw new IOException("Unexpected end of stream");
    	byteBuffer.position(0);
    }
    
    public void writeData(FileOutputStream destinationStream) throws IOException
    {
    	destinationStream.getChannel().write(getByteBuffer());
    }
    /* (non-Javadoc)
     * @see com.igeekinc.indelible.indeliblefs.datamover.DataDescriptor#writeData(java.io.OutputStream)
     */
    public void writeData(OutputStream destinationStream)
    throws IOException
    {
    	ByteBuffer srcBuffer;
    	srcBuffer = getByteBuffer();
    	if (srcBuffer.hasArray())
    	{
    		destinationStream.write(srcBuffer.array());
    	}
    	else
    	{
    		byte [] writeBuf = new byte[srcBuffer.limit()];
    		srcBuffer.get(writeBuf);
    		destinationStream.write(writeBuf);
    	}
    }
    // Returns an input stream for a ByteBuffer.
    // The read() methods use the relative ByteBuffer get() methods.
    public static InputStream newInputStream(final ByteBuffer buf) 
    {
    	return new InputStream() {
    		public synchronized int read() throws IOException 
    		{
    			if (!buf.hasRemaining()) 
    			{
    				return -1;
    			}
    			return buf.get();
    		}

    		public synchronized int read(byte[] bytes, int off, int len) throws IOException 
    		{
    			// Read only what's left
    			len = Math.min(len, buf.remaining());
    			buf.get(bytes, off, len);
    			return len;
    		}
    	};
    }
    /* (non-Javadoc)
     * @see com.igeekinc.indelible.indeliblefs.datamover.DataDescriptor#getDataStream()
     */
    public InputStream getInputStream()
    {
    	ByteBuffer srcBuffer;
    	srcBuffer = getByteBuffer();
        return newInputStream(srcBuffer);
    }

    /* (non-Javadoc)
     * @see com.igeekinc.indelible.indeliblefs.datamover.DataDescriptor#getLength()
     */
    public long getLength()
    {
        return byteBuffer.limit();
    }

    @Override
	public int getData(ByteBuffer destination, long srcOffset, int length,
			boolean release) throws IOException
	{
    	ByteBuffer srcBuffer;
    	srcBuffer = getByteBuffer();
    	srcBuffer.position((int)srcOffset);
    	srcBuffer.limit((int)srcOffset + length);
    	destination.put(srcBuffer);
		return length;
	}

	@Override
	public Future<Integer> getDataAsync(ByteBuffer destination, long srcOffset,
			int length, boolean release) throws IOException
	{
		DescriptorFuture returnFuture = new DescriptorFuture();
		int bytesRead = getData(destination, srcOffset, length, release);
		returnFuture.setDone(bytesRead);
		return returnFuture;
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
		} catch (Throwable t)
		{
			handler.failed(t, attachment);
		}
	}

	public byte[] getData()
    {
		ByteBuffer srcBuffer;
    	srcBuffer = getByteBuffer();
    	if (srcBuffer.hasArray())
    	{
    		return srcBuffer.array();
    	}
    	else
    	{
    		byte [] returnData = new byte[srcBuffer.limit()];
    		srcBuffer.get(returnData);
    		return returnData;
    	}
    }
    
    public int getData(byte[] destination, int destOffset, long srcOffset,
            int length, boolean release)
    {
        int bytesToCopy = length;
		ByteBuffer srcBuffer;
    	srcBuffer = getByteBuffer();
        if (srcOffset + length > getLength())
            bytesToCopy = (int) (getLength() - srcOffset);
        srcBuffer.position((int)srcOffset);
        srcBuffer.get(destination, destOffset, bytesToCopy);
        return bytesToCopy;
    }

    
    public boolean isAccessible()
    {
        return true;    // Always accessible
    }

    public boolean isShareableWithLocalProcess()
    {
        return true;    // We can serialize and send this somewhere else
    }

    public boolean isShareableWithRemoteProcess()
    {
        return true;    // We can serialize and send this somewhere else
    }

    public boolean descriptorContainsData()
    {
        return true;
    }
    
    public void close()
    {
        if (byteBuffer.isDirect())
        {
        	ByteBuffer cleanBuffer = byteBuffer;
        	byteBuffer = null;
        	DirectMemoryUtils.clean(cleanBuffer);
        }
    }
    

    private void writeObject(ObjectOutputStream out) throws IOException
    {
    	byte [] dataBuf = getData();
    	out.writeInt(dataBuf.length);
    	out.write(dataBuf);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
    	in.defaultReadObject();
    	int size = in.readInt();
    	byte [] dataBuf = new byte[size];
    	in.readFully(dataBuf);
    	byteBuffer = ByteBuffer.wrap(dataBuf);
    }

	public ByteBuffer getByteBuffer()
	{
		return byteBuffer.duplicate();
	}
}
