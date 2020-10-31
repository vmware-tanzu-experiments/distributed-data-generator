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


/**
 * AbortedError allows us to pass an AbortedException out through a call 
 * chain that has not declared itself to throw AbortedException
 *
 */
public class AbortedError extends Error
{
    /**
     * 
     */
    private static final long serialVersionUID = 2254023682643710971L;
    private AbortedException abortedException;
    public AbortedError(AbortedException abortedException)
    {
        this.abortedException = abortedException;
    }
    public AbortedException getAbortedException()
    {
        return abortedException;
    }
}
