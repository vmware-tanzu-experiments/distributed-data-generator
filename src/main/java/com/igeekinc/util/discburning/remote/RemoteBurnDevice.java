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
 
package com.igeekinc.util.discburning.remote;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import com.igeekinc.util.discburning.BurnDeviceID;
import com.igeekinc.util.discburning.BurnState;
import com.igeekinc.util.discburning.MediaState;
import com.igeekinc.util.discburning.MediaStatus;
import com.igeekinc.util.discburning.MediaType;

public interface RemoteBurnDevice extends Remote
{
    public String getName() throws RemoteException;
    
    public abstract BurnState getBurnState() throws RemoteException;
    public abstract MediaState getMediaState() throws RemoteException;
    public abstract MediaStatus getMediaStatus() throws RemoteException;
    public abstract MediaType getMediaType() throws RemoteException;
    
    public abstract void openTray() throws RemoteException, IOException;
    public abstract void closeTray() throws RemoteException, IOException;
    public abstract void ejectMedia() throws RemoteException, IOException;
    
    public abstract long getAvailableSpace() throws RemoteException;
    public abstract void setEventDelivery(RemoteDiscBurningEventDelivery eventDelivery) throws RemoteException;
    public BurnDeviceID getID() throws RemoteException;
    public double [] getAvailableSpeeds() throws RemoteException;
    public boolean acquireExclusiveAccess() throws RemoteException, IOException;
    public boolean isAccessExclusive() throws RemoteException;
    public void releaseExclusiveAccess() throws RemoteException, IOException;
    public void askForMediaReservation() throws RemoteException, IOException;
    public boolean isMediaReserved() throws RemoteException;
    public void releaseMediaReservation() throws RemoteException, IOException;
    public boolean canTrayOpen() throws RemoteException;
    public boolean isTrayOpen() throws RemoteException;
    public MediaType[] getSupportedMedia() throws RemoteException;
}
