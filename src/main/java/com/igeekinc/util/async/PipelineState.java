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

import java.util.Date;

/*
 * A PipelineState is allocated for each operation in the Pipeline.  Extend it to
 * add application specific information
 */
public class PipelineState<E extends Enum<E>>
{
	private long enteredTime, lastUpdateTime;
	private long pipelineSequence;
	
	private E currentState;

	private Object lastResult;
	private Throwable exception;
	
	public E getCurrentState()
	{
		return currentState;
	}

	public void setCurrentState(E currentState)
	{
		this.currentState = currentState;
	}

	public Object getLastResult()
	{
		return lastResult;
	}

	public void setLastResult(Object lastResult)
	{
		this.lastResult = lastResult;
	}

	public Throwable getException()
	{
		return exception;
	}

	public void setException(Throwable exception)
	{
		this.exception = exception;
	}

	public long getEnteredTime()
	{
		return enteredTime;
	}

	public void setEnteredTime(long enteredTime)
	{
		this.enteredTime = enteredTime;
	}

	public long getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime)
	{
		this.lastUpdateTime = lastUpdateTime;
	}

	public long getPipelineSequence()
	{
		return pipelineSequence;
	}

	public void setPipelineSequence(long pipelineSequence)
	{
		this.pipelineSequence = pipelineSequence;
	}
	
	public String toString()
	{
		Date enteredDate = new Date(enteredTime);
		Date lastUpdated = new Date(lastUpdateTime);
		
		return "Sequence "+pipelineSequence+" entered at "+enteredDate+" last updated at "+lastUpdated;
	}
}
