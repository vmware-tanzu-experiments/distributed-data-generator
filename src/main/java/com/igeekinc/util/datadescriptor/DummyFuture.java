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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * We need to figure out how to support pre 1.7 to take advantage of asynchronous I/O at the file level or
 * write our own stuff.  For the moment, just fake the async
 *
 */
public class DummyFuture implements Future<Integer>
{
	private int bytesRead;
	private Throwable exception;
	public DummyFuture(int bytesRead, Throwable exception)
	{
		this.bytesRead = bytesRead;
		this.exception = exception;
	}
	
	@Override
	public boolean cancel(boolean paramBoolean)
	{
		return false;
	}

	@Override
	public boolean isCancelled()
	{
		return false;
	}

	@Override
	public boolean isDone()
	{
		return true;
	}

	@Override
	public Integer get() throws InterruptedException, ExecutionException
	{
		if (exception != null)
			throw new ExecutionException(exception);
		return bytesRead;
	}

	@Override
	public Integer get(long paramLong, TimeUnit paramTimeUnit)
			throws InterruptedException, ExecutionException, TimeoutException
	{
		if (exception != null)
			throw new ExecutionException(exception);
		return bytesRead;
	}
	
}