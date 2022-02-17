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
 
package com.igeekinc.util.async;

import com.igeekinc.util.logging.ErrorLogMessage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;



/**
 * A ComboFuture combines a Future object with a CompletionHandler and a caller for CompletionHandlers
 * @author David L. Smith-Uchida
 *
 */
public class ComboFutureBase<V> implements Future<V>, AsyncCompletion<V, Object>
{
	private boolean done;
	private Throwable error;
	private AsyncCompletion<V, Object>completionHandler;
	private Object attachment;
	protected V value;
	
	private static long defaultTimeout = 120000;		// Time out in 2 minutes max
	public ComboFutureBase()
	{
		
	}
	
	@SuppressWarnings("unchecked")
	public <A>ComboFutureBase(AsyncCompletion<V, ? super A>completionHandler, A attachment)
	{
		this.completionHandler = (AsyncCompletion<V, Object>) completionHandler;
		this.attachment = attachment;
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
	public V get() throws InterruptedException, ExecutionException
	{
		try
		{
			return get(defaultTimeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e)
		{
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
			throw new InterruptedException("Timed out");
		}
	}

	@Override
	public synchronized V get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		long timeoutMS = TimeUnit.MILLISECONDS.convert(timeout, unit);
    	long timeStartedMS = System.currentTimeMillis();
    	long elapsed = System.currentTimeMillis() - timeStartedMS;
    	while (!isDone() && (timeoutMS == 0 || timeoutMS > elapsed))
    	{
    		this.wait(timeoutMS);
    		elapsed = System.currentTimeMillis() - timeStartedMS;
    	}
    	if (timeoutMS > 0 && timeoutMS <= elapsed)
    	{
    		LogManager.getLogger(getClass()).error("ComboFutureBase timed out");
    		throw new TimeoutException("Timed out after "+elapsed+" ms");
    	}
    	if (error != null)
    		throw new ExecutionException(error);
		return value;
	}
	
	@Override
	public synchronized void completed(V result, Object attachment)
	{
		value = result;
		// attachment is ignored - the attachment to be returned to our original caller is in this.attachment
		setDone();
	}
	
	protected synchronized void setDone()
	{
		done = true;
		notifyAll();
		if (completionHandler != null)
		{
			try
			{
				if (error == null)
					completionHandler.completed(value, attachment);
				else
					completionHandler.failed(error, attachment);
			}
			catch (Throwable t)
			{
				LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), t);
			}
		}
	}
	
	@Override
	public synchronized void failed(Throwable exc, Object attachment)
	{
		error = exc;
		setDone();	// bail
	}
}
