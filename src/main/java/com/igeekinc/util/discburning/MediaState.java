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
 
package com.igeekinc.util.discburning;

import java.io.Serializable;

public class MediaState implements Serializable
{
    private static final long serialVersionUID = -7643066141876736681L;
    public final static int kEmptyStateInt = 0;
    public final static int kTransitioningStateInt = 1;
    public final static int kPresentStateInt = 2;
    public final static int kDeviceInUseInt = 3;
    
    public final static MediaState kEmptyState = new MediaState(kEmptyStateInt);
    public final static MediaState kTransitioningState = new MediaState(kTransitioningStateInt);
    public final static MediaState kPresentState = new MediaState(kPresentStateInt);
    public final static MediaState kDeviceInUseState = new MediaState(kDeviceInUseInt);
    private int state;
    
    private MediaState(int inState)
    {
        state = inState;
    }
    
    public int getState()
    {
        return state;
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + state;
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MediaState other = (MediaState) obj;
        if (state != other.state)
            return false;
        return true;
    }
    
    public String toString()
    {
        switch (state)
        {
        case kEmptyStateInt:
            return ("Empty");
        case kTransitioningStateInt:
            return ("Transitioning");
        case kPresentStateInt:
            return ("Present");
        case kDeviceInUseInt:
        	return "In use";
        }
        return ("Unknown state "+state);
    }
}
