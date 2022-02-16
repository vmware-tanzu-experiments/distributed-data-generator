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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



class WaitingCommand
{
	private long completionID;
	private RemoteAsyncCommandBlock commandBlock;
	private ComboFutureBase completion;
	private Object attachment;
	
	public WaitingCommand(RemoteAsyncCommandBlock commandBlock, ComboFutureBase completion, Object attachment)
	{
		this.completionID = commandBlock.getCompletionID();
		this.commandBlock = commandBlock;
		this.completion = completion;
		this.attachment = attachment;
	}

	public long getCompletionID()
	{
		return completionID;
	}

	public RemoteAsyncCommandBlock getCommandBlock()
	{
		return commandBlock;
	}

	public ComboFutureBase getCompletion()
	{
		return completion;
	}
	
	public Object getAttachment()
	{
		return attachment;
	}
}
/**
 * Client side support for a RemoteAsyncManager
 * @author David L. Smith-Uchida
 *
 */
public abstract class ClientRemoteAsyncManager implements Runnable
{
	private long completionID = 0;
	//private ArrayList<WaitingCommand>commandQueue = new ArrayList<WaitingCommand>();
	private LinkedBlockingQueue<WaitingCommand>commandQueue = new LinkedBlockingQueue<WaitingCommand>(16);
	private HashMap<Long, WaitingCommand>waiting = new HashMap<Long, WaitingCommand>();
	private Thread asyncThread;
	private Logger logger = LogManager.getLogger(getClass());
	public void queueCommand(RemoteAsyncCommandBlock commandBlockToQueue, ComboFutureBase completion, Object attachment)
	{
		long curCompletionID;
		synchronized(this)
		{
			curCompletionID = completionID++;
		}
		commandBlockToQueue.setCompletionID(curCompletionID);
		checkThread();
		WaitingCommand curCommand = new WaitingCommand(commandBlockToQueue, completion, attachment);
		if (logger.isDebugEnabled())
		{
			logger.debug(new DebugLogMessage("Queuing command {0}", curCommand.getCompletionID()));
		}
		commandQueue.add(curCommand);
	}
	
	public void waitForQueueDrain()
	{
		while(commandQueue.peek() != null)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
			}
		}
	}
	synchronized void checkThread()
	{
		if (asyncThread == null)
		{
			asyncThread = new Thread(this, "Async Proxy Thread");
			asyncThread.start();
		}
	}
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				WaitingCommand [] commandsToExecute = null;
				boolean noWork = true;
				while (noWork)
				{
					int numWaiting;
					synchronized(waiting)
					{
						numWaiting = waiting.size();
					}
					ArrayList<WaitingCommand>executeNow = new ArrayList<WaitingCommand>();
					if (numWaiting == 0 && commandQueue.size() == 0)
					{
						try
						{
							WaitingCommand first = commandQueue.poll(10, TimeUnit.SECONDS);
							if (first != null)
							{
								executeNow.add(first);
								noWork = false;
							}
						} catch (InterruptedException e)
						{
							LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
						}
					}
					else
					{
						noWork = false;
						commandQueue.drainTo(executeNow);
					}
					// It's OK to come out of here with commandsToExecute.length == 0.  That can happen if we
					// have commands waiting for completion so we need to go poll for them
					commandsToExecute = new WaitingCommand[executeNow.size()];
					commandsToExecute = executeNow.toArray(commandsToExecute);
				}
				RemoteAsyncCommandBlock [] remoteCommands = new RemoteAsyncCommandBlock[commandsToExecute.length];
				synchronized(waiting)
				{
					for (int curCommandNum = 0; curCommandNum < commandsToExecute.length; curCommandNum++)
					{
						WaitingCommand curCommand = commandsToExecute[curCommandNum];
						remoteCommands[curCommandNum] = curCommand.getCommandBlock();
						waiting.put(curCommand.getCompletionID(), curCommand);
						if (logger.isDebugEnabled())
						{
							logger.debug(new DebugLogMessage("Sending command {0}", curCommand.getCompletionID()));
						}
					}
				}
				try
				{
					long timeout = 100;		// Time to wait if there's nothing being sent
					if (remoteCommands.length > 0)
						timeout = 0;
					RemoteAsyncCompletionStatus [] completed = executeAsync(remoteCommands, timeout);
					for (RemoteAsyncCompletionStatus curCompleted:completed)
					{
						if (curCompleted != null)
						{
							long completionID = curCompleted.getCompletionID();
							if (logger.isDebugEnabled())
							{
								logger.debug(new DebugLogMessage("Got completion for command {0}", completionID));
							}
							WaitingCommand curCompletionBlock;
							synchronized(waiting)
							{
								curCompletionBlock = waiting.remove(completionID);
							}
							if (curCompletionBlock != null)
							{
								ComboFutureBase completionFuture = curCompletionBlock.getCompletion();

								if (curCompleted.getException() != null)
								{
									completionFuture.failed(curCompleted.getException(), curCompletionBlock.getAttachment());
								}
								else
								{
									completionFuture.completed(curCompleted.getReturnValue(), curCompletionBlock.getAttachment());
								}

							}
							else
							{
								LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got completion ID = {0} but could not find matching command"));
							}
						}
						else
						{
							LogManager.getLogger(getClass()).error(new ErrorLogMessage("Got back a null completion status"));
						}
					}
				} catch (RemoteException e)
				{
					LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
				}
			}
		}
		catch (Throwable t)
		{
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Unexpected exception"), t);
		}
	}
	
	protected abstract RemoteAsyncCompletionStatus [] executeAsync(RemoteAsyncCommandBlock [] commandsToExecute, long timeToWaitForNewCompletionsInMS) throws RemoteException;
}
