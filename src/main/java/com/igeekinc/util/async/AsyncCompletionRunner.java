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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class CompletionRunnable implements Runnable
{
	private AsyncCompletion<Object, Object>completionHandler;
	private Object result;
	private Object attachment;
	private Throwable exception;
	
	/**
	 * Runs the completion handler.  If exception is null, completed is called, if exception is not null, failed is called
	 * @param completion
	 * @param result
	 * @param attachment
	 * @param exception
	 */
	public CompletionRunnable(AsyncCompletion<Object, Object>completion, Object result,
			Object attachment, Throwable exception)
	{
		this.result = result;
		this.attachment = attachment;
		this.exception = exception;
	}
	@Override
	public void run()
	{
		if (exception == null)
		{
			completionHandler.completed(result, attachment);
		}
		else
		{
			completionHandler.failed(exception, attachment);
		}
	}
	
}
public class AsyncCompletionRunner
{
	private BlockingQueue<Runnable> workQueue;
	private ThreadPoolExecutor pool;
	
	public AsyncCompletionRunner()
	{
		workQueue = new ArrayBlockingQueue<Runnable>(32);
		pool = new ThreadPoolExecutor(2, 8, 60, TimeUnit.SECONDS, workQueue);
	}
	
	public void completeAsyncHandler(AsyncCompletion<Object, Object>completion, Object result, Object attachment, Throwable exception)
	{
		CompletionRunnable runnable = new CompletionRunnable(completion, result, attachment, exception);
		pool.execute(runnable);
	}
}
