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

import com.igeekinc.util.logging.DebugLogMessage;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


class CompletionBlock implements AsyncCompletion<Object, Void>
{
	private RemoteAsyncManager parent;
	private RemoteAsyncCommandBlock command;
	private RemoteAsyncCompletionStatus completionStatus;
	private boolean completed = false;
	public CompletionBlock(RemoteAsyncManager parent, RemoteAsyncCommandBlock command)
	{
		this.parent = parent;
		this.command = command;
	}

	public RemoteAsyncCompletionStatus getCompletionStatus()
	{
		return completionStatus;
	}

	public void setCompletionStatus(RemoteAsyncCompletionStatus completionStatus)
	{
		this.completionStatus = completionStatus;
	}

	public RemoteAsyncCommandBlock getCommand()
	{
		return command;
	}
	
	public long getCompletionID()
	{
		return command.getCompletionID();
	}

	@Override
	public void completed(Object result, Void attachment)
	{
		parent.completed(result, this);
		completed = true;	// Don't mark completed until after the parent processes us!
	}

	@Override
	public void failed(Throwable exc, Void attachment)
	{
		parent.failed(exc, this);
		completed = true;	// Don't mark completed until after the parent processes us!
	}

	public boolean isCompleted()
	{
		return completed;
	}
	
	
}

public class RemoteAsyncManager implements AsyncCompletion<Object, CompletionBlock>
{
	private ArrayList<CompletionBlock>completionQueue = new ArrayList<CompletionBlock>();
	private long lastCommandExecuted = -1, lastCommandCompleted = -1;
	private Logger logger = LogManager.getLogger(getClass());
	public RemoteAsyncManager()
	{
		
	}
	
	/**
	 * executeAsync is designed to support asynchronous execution via RMI.  Since RMI is inherently synchronous, executeAsync
	 * acts as a synchronous interface to asychronous commands.  In order to support streaming, multiple commands can be passed at
	 * once to be started on the server side.  Multiple completions can be returned as well (these completions may not include any
	 * of the commands that have been passed in the current invocation).
	 * 
	 * Completions are returned in-order, even if the completion happened out of order.  This restriction may be relaxed in the future.
	 * 
	 * The client-side should maintain a queue of commands and completion handlers which is added to by "async" calls on the client side.
	 * The client-side execution thread should run in a loop, grabbing any commands and batching them together into a single executeAsync
	 * call to the remote.  The remote will start those commands and then return any completions that have occured.
	 * 
	 * On the client side, if there are no 
	 * @param commandsToExecute
	 * @param timeToWaitForNewCompletionsInMS - 0 indicates an immediate return
	 * @return
	 */
	public RemoteAsyncCompletionStatus [] executeAsync(RemoteAsyncCommandBlock [] commandsToExecute, long timeToWaitForNewCompletionsInMS)
	{
		for (RemoteAsyncCommandBlock curCommand:commandsToExecute)
		{
			CompletionBlock curCompletionBlock = new CompletionBlock(this, curCommand);
			if (logger.isDebugEnabled())
			{
				logger.debug(new DebugLogMessage("Received async command {0}", curCommand.getCompletionID()));
			}
			synchronized(completionQueue)
			{
				completionQueue.add(curCompletionBlock);
			}
			try
			{
				lastCommandExecuted = curCommand.getCompletionID();
				curCommand.executeAsync(curCompletionBlock);
			}
			catch (Throwable t)
			{
				curCompletionBlock.setCompletionStatus(new RemoteAsyncCompletionStatus(curCompletionBlock.getCompletionID(), t, null));
				curCompletionBlock.failed(t, null);
			}
		}
		ArrayList<RemoteAsyncCompletionStatus>returnList = new ArrayList<RemoteAsyncCompletionStatus>();
		synchronized(completionQueue)
		{
			long timeoutEnd = System.currentTimeMillis() + timeToWaitForNewCompletionsInMS;
			while((completionQueue.size() == 0 || !completionQueue.get(0).isCompleted()) && System.currentTimeMillis() < timeoutEnd)
			{
				try
				{
					completionQueue.wait(timeToWaitForNewCompletionsInMS);
				} catch (InterruptedException e)
				{
				}
			}
			
			while(completionQueue.size() > 0 && completionQueue.get(0).isCompleted())
			{
				RemoteAsyncCompletionStatus curCompleted = completionQueue.get(0).getCompletionStatus();
				completionQueue.remove(0);
				returnList.add(curCompleted);
				if (logger.isDebugEnabled())
				{
					logger.debug(new DebugLogMessage("Returning completion for async command {0}", curCompleted.getCompletionID()));
				}
			}
		}
		RemoteAsyncCompletionStatus [] returnArray = new RemoteAsyncCompletionStatus[returnList.size()];
		returnArray = returnList.toArray(returnArray);
		return returnArray;
	}

	@Override
	public void completed(Object result, CompletionBlock completionBlock)
	{
		RemoteAsyncCompletionStatus completionStatus = new RemoteAsyncCompletionStatus(completionBlock.getCompletionID(), null, result);
		handleCompletion(completionBlock, completionStatus);
	}

	@Override
	public void failed(Throwable exc, CompletionBlock completionBlock)
	{
		RemoteAsyncCompletionStatus completionStatus = new RemoteAsyncCompletionStatus(completionBlock.getCompletionID(), exc, null);
		handleCompletion(completionBlock, completionStatus);
	}

	private void handleCompletion(CompletionBlock completionBlock,
			RemoteAsyncCompletionStatus completionStatus)
	{
		synchronized(completionQueue)
		{
			completionBlock.setCompletionStatus(completionStatus);
			lastCommandCompleted = completionBlock.getCompletionID();
			if (logger.isDebugEnabled())
			{
				logger.debug(new DebugLogMessage("Completed async command {0}", completionBlock.getCompletionID()));
			}
			completionQueue.notifyAll();
		}
	}
	
	public int getNumWaiting()
	{
		synchronized(completionQueue)
		{
			return completionQueue.size();
		}
	}
	
	public void waitAllCompleted()
	{
		synchronized(completionQueue)
		{
			while(completionQueue.size() > 0)
			{
				try
				{
					completionQueue.wait();
				} catch (InterruptedException e)
				{
					LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
				}
			}
		}
	}
}
