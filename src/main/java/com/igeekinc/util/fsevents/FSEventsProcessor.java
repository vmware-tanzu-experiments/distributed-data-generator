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
 
package com.igeekinc.util.fsevents;

import com.igeekinc.util.FilePath;
import com.igeekinc.util.Volume;
import com.igeekinc.util.rules.Rule;


public interface FSEventsProcessor
{
    /**
     * Adds an FSEventListener that will be called for file system events that are contained within
     * the listenVolume/listenPath.  If listenPath is null, all events for the volume will be sent.
     * The include/exclude rules will be applied as well.  Events will only be sent for files that 
     * match at least one of the include rules and that match none of the exclude rules.
     * @param listenVolume - a volume to listen for events on
     * @param listenPath - a path relative to the Volume to listen for events on/within (may be null)
     * @param includeRules - events will be sent only if the path matches at least one of the include rules.  If 
     * includeRules is null, all events will be sent
     * @param excludeRules - events will be sent only if the path does not match any of the exclude rules.  If
     * excludeRules is null, all events will be sent
     * @param listener
     */
    public void addFSEventListener(Volume listenVolume, FilePath listenPath, FSStreamOffset offset, Rule [] includeRules, Rule [] excludeRules,
            FSEventListener listener);
    
    public void removeFSEventListener(FSEventListener removeListener);

    /**
     * Starts the events processor.  Call this after all listeners have been setup.
     *
     */
    public void start();
    
    /**
     * Stops the events processor.  Stops listening for new events and returns when all
     * queue events have been processed.
     */
    public void stop();
    
    public FSStreamOffset getCurrentOffset();
}