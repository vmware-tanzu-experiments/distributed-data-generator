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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




public class DirectMemoryUtils {

	private static final Logger logger = LogManager.getLogger(DirectMemoryUtils.class);
	private static final String MAX_DIRECT_MEMORY_PARAM ="-XX:MaxDirectMemorySize=";
	private static final AtomicLong allocated = new AtomicLong(0);

	public static MappedByteBuffer mapReadOnlyBuffer(FileChannel channel, long offset, long length) throws IOException
	{
		if (logger.isDebugEnabled())
		{
			long maxDirectMemory = getDirectMemorySize();
			long allocatedCurrently = allocated.get();
			logger.debug("Memory Map Allocation: " +
					" Allocation = " + length +
					", Allocated = " + allocatedCurrently +
					", MaxDirectMemorySize = " + maxDirectMemory +
					", Remaining = " + Math.max(0,(maxDirectMemory - allocatedCurrently)));
		}
		boolean triedToGC = false;
		while (true)
		try {
			MappedByteBuffer returnBuffer = channel.map(FileChannel.MapMode.READ_ONLY,
					offset, length);
			allocated.addAndGet(length);
			return returnBuffer;
		} catch(OutOfMemoryError error) {
			if (!triedToGC)
			{
				// First, see if we can garbage collect our way out
				System.gc();
				triedToGC = true;
			}
			else
			{
				
				long maxDirectMemory = getDirectMemorySize();
				long allocatedCurrently = allocated.get();
				logger.error("Error mapping " + length + ", you likely want" +
						" to increase " + MAX_DIRECT_MEMORY_PARAM, error);
				throw error;
			}
		}
		
	}
	public static ByteBuffer allocateDirect(int size) 
	{
		/*
		if (logger.isDebugEnabled())
		{
			long maxDirectMemory = getDirectMemorySize();
			long allocatedCurrently = allocated.get();
			logger.debug("Direct Memory Allocation: " +
					" Allocation = " + size +
					", Allocated = " + allocatedCurrently +
					", MaxDirectMemorySize = " + maxDirectMemory +
					", Remaining = " + Math.max(0,(maxDirectMemory - allocatedCurrently)));
		}
		boolean triedToGC = false;
		while (true)
		{
			try {
				ByteBuffer result = ByteBuffer.allocateDirect(size);
				allocated.addAndGet(size);
				return result;
			} catch(OutOfMemoryError error) {
				if (!triedToGC)
				{
					// First, see if we can garbage collect our way out
					System.gc();
					triedToGC = true;
				}
				else
				{
					long maxDirectMemory = getDirectMemorySize();
					long allocatedCurrently = allocated.get();
					logger.error("Error allocating " + size + ", you likely want" +
							" to increase " + MAX_DIRECT_MEMORY_PARAM + " - trying to return a heap buffer", error);
					return ByteBuffer.allocate(size);
				}
			}
		}
		*/
		return ByteBuffer.allocate(size);
	}
	
	/**
	 * "Cleans" a direct byte buffer - this releases its direct memory
	 * @param buffer
	 * @throws Exception
	 */
	public static void clean(ByteBuffer buffer) 
	{
		if (!buffer.isDirect())
			throw new IllegalArgumentException("Buffer is not direct");
		try
		{
			Method cleanerMethod = buffer.getClass().getMethod("cleaner");
			cleanerMethod.setAccessible(true);
			Object cleaner = cleanerMethod.invoke(buffer);
			if (cleaner != null)
			{
				Method cleanMethod = cleaner.getClass().getMethod("clean");
				cleanMethod.setAccessible(true);
				cleanMethod.invoke(cleaner);
			}
			if (logger.isDebugEnabled())
			{
				allocated.getAndAdd(-buffer.capacity());
				long maxDirectMemory = getDirectMemorySize();
				logger.debug("Direct Memory Deallocation: " +
						", Allocated = " + allocated.get() +
						", MaxDirectMemorySize = " + maxDirectMemory +
						", Remaining = " + Math.max(0, (maxDirectMemory - allocated.get())));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			throw new InternalError("Could not clean buffer");	// Most likely this is a reflection error of some kind
		}

	}
	
	public static long getDirectMemorySize() 
	{
		return sun.misc.VM.maxDirectMemory();
	}
}
