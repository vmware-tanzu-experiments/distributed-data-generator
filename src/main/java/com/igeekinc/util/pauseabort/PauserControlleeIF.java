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

public interface PauserControlleeIF
{
    /**
     * Checks to see if a pause or abort request has been received on this object.
     * If an abort request has been received, an AbortedException is thrown
     * If a pause request has been received the routine will block until the pause request has been
     * lifted.  Don't call this routine if you are holding locks on shared resources!
     * @throws AbortedException
     */
	public void checkPauseAndAbort() throws AbortedException;
	
	/**
	 * Checks to see if an abort request has been received.
	 * If an abort request has been received, an AbortedException is thrown
	 * This routine will return or throw an exception immediately
	 * @throws AbortedException
	 */
    public void checkAbort() throws AbortedException;
}