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

public class BurnState implements Serializable
{
    public static final long serialVersionUID = -5093539739524878041L;
    public final static int kEmptyStateInt = 0;
    public final static int kBlankMediaMountedInt = 1;
    public final static int kBurntMediaMountedInt = 2;
    public final static int kBurningInt = 3;
    public final static int kVerifyingInt = 4;
    public final static int kBurnFailedInt = 5;
    public final static int kPreparingInt = 6;
    public final static int kErasingInt = 7;
    public final static int kFinishingInt = 8;
    public final static int kAbortingInt = 9;
    public final static int kOpeningTrackInt = 10;
    public final static int kClosingTrackInt = 11;
    public final static int kOpeningSessionInt = 12;
    public final static int kClosingSessionInt = 13;
    
    public final static BurnState kEmptyState = new BurnState(kEmptyStateInt);
    public final static BurnState kBlankMediaMounted = new BurnState(kBlankMediaMountedInt);
    public final static BurnState kBurntMediaMounted = new BurnState(kBurntMediaMountedInt);
    public final static BurnState kBurning = new BurnState(kBurningInt);
    public final static BurnState kVerifying = new BurnState(kVerifyingInt);
    public final static BurnState kBurnFailed = new BurnState(kBurnFailedInt);
    public final static BurnState kPreparing = new BurnState(kPreparingInt);
    public final static BurnState kErasing = new BurnState(kErasingInt);
    public final static BurnState kFinishing = new BurnState(kFinishingInt);
    public final static BurnState kAborting = new BurnState(kAbortingInt);
    public final static BurnState kOpeningTrack = new BurnState(kOpeningTrackInt);
    public final static BurnState kClosingTrack = new BurnState(kClosingTrackInt);
    public final static BurnState kOpeningSession = new BurnState(kOpeningSessionInt);
    public final static BurnState kClosingSession = new BurnState(kClosingSessionInt);
    private int state;
    
    private BurnState(int inState)
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
        final BurnState other = (BurnState) obj;
        if (state != other.state)
            return false;
        return true;
    }
}
