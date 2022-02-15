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

import com.igeekinc.util.logging.ErrorLogMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;



public class DescriptorFuture implements Future<Integer>
{
	private boolean done = false, cancelled = false;
	private int bytesRead;
	private Throwable error;
	
	public DescriptorFuture()
	{
		
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
		return cancelled;
	}

	@Override
	public boolean isDone()
	{
		return done;
	}

	@Override
	public Integer get() throws InterruptedException, ExecutionException
	{
		try
		{
			return get(0, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e)
		{
			// Should never get here
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
			return 0;
		}
	}

	@Override
	public synchronized Integer get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		long timeoutMS = unit.convert(timeout, TimeUnit.MILLISECONDS);
    	long timeStartedMS = System.currentTimeMillis();
    	while (!done && (timeoutMS == 0 || timeoutMS > (timeStartedMS - System.currentTimeMillis())))
    	{
    		this.wait(timeoutMS);
    	}
    	if (error != null)
    		throw new ExecutionException(error);
		return bytesRead;
	}

	public synchronized void setDone(int bytesRead)
	{
		done = true;
		this.bytesRead = bytesRead;
		notifyAll();
	}
	
	public synchronized void setError(Throwable error)
	{
		done = true;
		this.bytesRead = 0;
		this.error = error;
		notifyAll();
	}
}
