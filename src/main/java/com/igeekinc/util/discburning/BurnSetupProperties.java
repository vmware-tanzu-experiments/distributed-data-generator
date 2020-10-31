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

public class BurnSetupProperties implements Serializable, Cloneable
{
    private static final long serialVersionUID = 5554722884129815579L;
    private BurnDeviceID burnDeviceID;
    private boolean leaveDiscAppendable;
    private boolean ejectAfterBurn;
    private boolean verifyBurn;
    private double burnSpeedMax; /* KB/s */
    private boolean testBurn;    // Just do a test burn if possible
    
    public BurnSetupProperties()
    {
        
    }

    public BurnDeviceID getBurnDeviceID()
    {
        return burnDeviceID;
    }

    public void setBurnDeviceID(BurnDeviceID burnDeviceID)
    {
        this.burnDeviceID = burnDeviceID;
    }

    public double getBurnSpeedMax()
    {
        return burnSpeedMax;
    }

    public void setBurnSpeedMax(double burnSpeedMax)
    {
        this.burnSpeedMax = burnSpeedMax;
    }

    public boolean isEjectAfterBurn()
    {
        return ejectAfterBurn;
    }

    public void setEjectAfterBurn(boolean ejectAfterBurn)
    {
        this.ejectAfterBurn = ejectAfterBurn;
    }

    public boolean isLeaveDiscAppendable()
    {
        return leaveDiscAppendable;
    }

    public void setLeaveDiscAppendable(boolean leaveDiscAppendable)
    {
        this.leaveDiscAppendable = leaveDiscAppendable;
    }

    public boolean isVerifyBurn()
    {
        return verifyBurn;
    }

    public void setVerifyBurn(boolean verifyBurn)
    {
        this.verifyBurn = verifyBurn;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public boolean isTestBurn()
    {
        return testBurn;
    }

    public void setTestBurn(boolean testBurn)
    {
        this.testBurn = testBurn;
    }
    
    
}
