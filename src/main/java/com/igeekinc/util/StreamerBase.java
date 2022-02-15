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

import com.igeekinc.util.logging.ErrorLogMessage;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauseAbort;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.Logger;



public abstract class StreamerBase<ExecutionInfoType, ExecutionCompletionType> implements Runnable
{
    protected LinkedBlockingQueue<ExecutionInfoType> executionQueue;
    protected PauseAbort pauser;
    protected int maxQueueLength;
    protected int numThreads;
    protected Logger logger;
    protected Thread [] workerThreads;
    protected boolean keepRunning = true;
    /**
     * 
     * @param maxQueueLength - maximum number of items to put on queue - MAXINT for none
     * @param numThreads - number of worker threads
     * @param logger
     * @param pauser
     */
    public StreamerBase(int maxQueueLength, int numThreads, Logger logger, PauseAbort pauser)
    {
        if (numThreads < 1)
            throw new IllegalArgumentException("Must have at least 1 thread");
        if (maxQueueLength < 1)
            throw new IllegalArgumentException("1 is min length for queue");
        if (logger == null)
            throw new IllegalArgumentException("logger cannot be null");
        if (pauser == null)
            throw new IllegalArgumentException("pauser cannot be null");
        executionQueue = new LinkedBlockingQueue<ExecutionInfoType>(maxQueueLength);
        this.maxQueueLength = maxQueueLength;
        this.numThreads = numThreads;
        this.logger = logger;
        this.pauser = pauser;
        
        workerThreads = new Thread[numThreads];
        
    }
    
    protected boolean queueAction(ExecutionInfoType executionInfo, boolean waitForQueue) throws AbortedException, InterruptedException
    {
        pauser.checkPauseAndAbort();
        synchronized(this)
        {
            if (waitForQueue)
            {
                executionQueue.put(executionInfo);
                return true;
            }
            else
            {
                return executionQueue.offer(executionInfo);
            }
        }
    }

    @Override
    public void run()
    {
        ExecutionInfoType executionInfo;
        while (keepRunning)
        {
            try
            {
                synchronized(this)
                {
                    executionInfo = executionQueue.take();
                }
                ExecutionCompletionType completionInfo = doWork(executionInfo);
                sendFinished(completionInfo);
            } catch (InterruptedException e)
            {
                logger.error(new ErrorLogMessage("Caught exception"), e);
            }
        }
    }
    
    protected abstract ExecutionCompletionType doWork(ExecutionInfoType executionInfo);
    protected abstract void sendFinished(ExecutionCompletionType completionInfo);
}
