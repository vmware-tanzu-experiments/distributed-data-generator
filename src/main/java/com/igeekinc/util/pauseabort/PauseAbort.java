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
 
package com.igeekinc.util.pauseabort;

import org.apache.logging.log4j.Logger;

public class PauseAbort implements PauserControllerIF, PauserControlleeIF
{
    protected Logger logger;
    protected boolean pause=false, abort=false, suspend=false;
    protected AbortReason abortType;
    protected PauserControllerIF subPauser;
    
    public PauseAbort(Logger inLogger)
    {
        logger = inLogger;
    }
    @Override
    public synchronized void checkPauseAndAbort()
    throws AbortedException
    {
        if (abort)
        {
            logger.debug("Aborted");
            abortNotify();
            if (abortType == AbortReason.kSuspended)
                throw new SuspendedException();
            else
                throw new AbortedException();
        }
        while (pause)
        {
            logger.debug("Paused");
            pauseNotify();
            try
            {
                wait(10000);
            }
            catch (InterruptedException e)
            {

            }
            if (!pause)
            {
                logger.debug("Resumed...");
                resumeNotify();
            }
        }
        return;
    }
    
    @Override
    public synchronized void checkAbort() throws AbortedException
    {
        if (abort)
        {
            logger.debug("Aborted");
            abortNotify();
            throw new AbortedException();
        }
    }
    @Override
    public synchronized void pause()
    {
        pause = true;
        if (subPauser != null)
            subPauser.pause();
    }

    @Override
    public synchronized void resume()
    {
        pause=false;
        if (subPauser != null)
            subPauser.resume();
        notifyAll();
    }

    @Override
    public synchronized void abort(AbortReason inAbortReason)
    {
        abort=true;
        pause=false;
        abortType = inAbortReason;
        if (subPauser != null)
            subPauser.abort(inAbortReason);
        notifyAll();

    }

    public synchronized void suspend()
    {
    	abort(AbortReason.kSuspended);
    }
    
    public void pauseNotify()
    {

    }

    public void resumeNotify()
    {

    }

    public void abortNotify()
    {

    }
    /**
     * @return Returns the abortReason.
     */
    public AbortReason getAbortType()
    {
        return abortType;
    }
    
    public synchronized boolean isPaused()
    {
        return pause;
    }

    public synchronized boolean isAbort()
    {
        return abort;
    }
    
    public void setSubPauser(PauserControllerIF subPauser)
    {
        this.subPauser = subPauser;
    }
    
    public PauserControllerIF getSubPauser()
    {
        return subPauser;
    }
}