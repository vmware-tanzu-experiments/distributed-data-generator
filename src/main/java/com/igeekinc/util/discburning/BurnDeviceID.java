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

public class BurnDeviceID implements Serializable
{
    String burnDeviceIDStr;
    
    public BurnDeviceID(String burnDeviceIDStr)
    {
        this.burnDeviceIDStr = burnDeviceIDStr;
    }
    
    public String getBurnDeviceIDStr()
    {
        return burnDeviceIDStr;
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((burnDeviceIDStr == null) ? 0 : burnDeviceIDStr.hashCode());
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
        final BurnDeviceID other = (BurnDeviceID) obj;
        if (burnDeviceIDStr == null)
        {
            if (other.burnDeviceIDStr != null)
                return false;
        } else if (!burnDeviceIDStr.equals(other.burnDeviceIDStr))
            return false;
        return true;
    }
}
