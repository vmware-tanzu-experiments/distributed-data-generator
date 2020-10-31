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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import com.igeekinc.util.DataSource;
import com.igeekinc.util.async.AsyncCompletion;

public interface DataDescriptor extends Serializable, DataSource
{
    /**
     * Writes the data referenced by the descriptor into the destinationStream
     * @param destinationStream
     * @throws IOException
     */
    public void writeData(OutputStream destinationStream)
    	throws IOException;
    
    /**
     * Writes the data referenced by the descriptor into the destinationStream (specifying FileOutputStream
     * allows us to use FileChannel internally)
     * @param destinationStream
     * @throws IOException
     */
    public void writeData(FileOutputStream destinationStream)
    	throws IOException;
    
    /**
     * Returns an InputStream that will read all of the data referenced by the descriptor
     * @return
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;
    
    /**
     * Returns all of the data referenced by the descriptor
     * @return
     * @throws IOException
     */
    public byte [] getData() throws IOException;
    
    /**
     * Returns all of the data referenced by the descriptor
     * @return
     * @throws IOException
     */
    public ByteBuffer getByteBuffer() throws IOException;
    /**
     * Reads some of the data from the descriptor
     * @param destination - buffer to read data into
     * @param destOffset - offset in the destination buffer
     * @param srcOffset - offset in the source data
     * @param length - bytes to read
     * @param release - release the data descriptor after this read (cannot be used again after this is called)
     * @return number of bytes read
     * @throws IOException
     */
    public int getData(byte [] destination, int destOffset, long srcOffset, int length, boolean release) throws IOException;
    
    /**
     * Reads some of the data from the descriptor
     * @param destination - buffer to read data into - data is written at the current buffer offset
     * @param srcOffset - offset in the source data
     * @param length - bytes to read
     * @param release - release the data descriptor after this read (cannot be used again after this is called)
     * @return number of bytes read
     * @throws IOException
     */
    public int getData(ByteBuffer destination, long srcOffset, int length, boolean release) throws IOException;
    
    public Future<Integer>getDataAsync(ByteBuffer destination, long srcOffset, int length, boolean release) throws IOException;
    
    public <A> void getDataAsync(ByteBuffer destination, long srcOffset, int length, boolean release, A attachment, AsyncCompletion<Integer, ? super A>handler) throws IOException;
    /**
     * Length of the data
     * @return
     */
    public long getLength();
    /**
     * Returns true if this descriptor can be given to another process on the same machine and still
     * work (for example, a FileDataDescriptor which holds a path)
     * @return
     */
    public boolean isShareableWithLocalProcess();
    /**
     * Returns true if this descriptor can be given to a process on a different host and still work
     * (for example, a NetworkDataDescriptor)
     * @return
     */
    public boolean isShareableWithRemoteProcess();
    /**
     * Returns true if the data is currently accessible.  Returns false if it is not.  (For example, a data descriptor
     * that is only locally accessible that is sent to a remote machine)
     * @return
     */
    public boolean isAccessible();
    
    /**
     * Returns true if the data is contained within the descriptor (e.g. BasicDataDescriptor which contains the memory buffer)
     * @return
     */
    public boolean descriptorContainsData();
    
    /**
     * Tells the descriptor that we're not planning to use it anymore and that it can release file handles, etc.
     * @throws IOException 
     */
    public void close() throws IOException;
}
