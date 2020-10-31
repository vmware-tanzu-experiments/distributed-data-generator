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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A Pipeline is used to manage a set of asynchronous method calls in a coherent and standardized manner.
 * This is for a series of operations that need to be executed in a specified order.  Define each of the
 * operations as a method in a subclass of the Pipeline class.  When you make async calls, use the subclass (this)
 * as the AsyncCompletion manager.  A PipelineState object should be passed as the attachment.  The Pipeline dispatch()
 * method will use the PipelineState object to determine what the next method to be called is.
 * @author David L. Smith-Uchida
 *
 */
public abstract class Pipeline<E extends Enum<E>, V> implements AsyncCompletion<V, PipelineState<E>>
{
	private ArrayList<PipelineState<E>> pipeline = new ArrayList<PipelineState<E>>();
	private long sequence = 0;

	public synchronized void enter(PipelineState<E> stateToEnter)
	{
		stateToEnter.setPipelineSequence(sequence);
		sequence++;
		stateToEnter.setEnteredTime(System.currentTimeMillis());
		pipeline.add(stateToEnter);
	}
	
	public synchronized void exit(PipelineState<E> stateToExit)
	{
		pipeline.remove(stateToExit);
		notifyAll();
	}
	
	@Override
	public void completed(V result, PipelineState<E> attachment)
	{
		attachment.setLastResult(result);
		dispatchSuccess(attachment);
	}

	@Override
	public void failed(Throwable exc, PipelineState<E> attachment)
	{
		attachment.setException(exc);
		dispatchFailed(attachment);
	}

	public abstract void dispatchSuccess(PipelineState<E> attachment);
	
	public abstract void dispatchFailed(PipelineState<E> attachment);
	
	public synchronized void waitForPipelineCompletion(long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		long timeToWaitMS = timeUnit.toMillis(timeout);
		long startTime = System.currentTimeMillis();
		while (pipeline.size() > 0 && (timeToWaitMS == 0 || System.currentTimeMillis() - startTime < timeToWaitMS))
		{
			wait(timeToWaitMS);
		}
	}
	
	public synchronized String toString()
	{
		StringBuffer buildBuffer = new StringBuffer("Pipeline entries = ");
		buildBuffer.append(Integer.toString(pipeline.size()));
		buildBuffer.append(":\n");
		for (PipelineState<E>curPipelineState:pipeline)
		{
			buildBuffer.append(curPipelineState.toString());
			buildBuffer.append("\n");
		}
		return buildBuffer.toString();
	}
}
