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
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import com.igeekinc.util.discburning.BurnDevice;
import com.igeekinc.util.discburning.BurnDeviceID;
import com.igeekinc.util.discburning.BurnDeviceStatusChanged;
import com.igeekinc.util.discburning.BurnDeviceStatusChangedListener;
import com.igeekinc.util.discburning.BurnState;
import com.igeekinc.util.discburning.MediaState;
import com.igeekinc.util.discburning.MediaStatus;
import com.igeekinc.util.discburning.MediaType;
import com.igeekinc.util.exceptions.DeadListenerError;
import com.igeekinc.util.logging.DebugLogMessage;

public class RemoteBurnDeviceImpl extends UnicastRemoteObject implements
        RemoteBurnDevice, BurnDeviceStatusChangedListener
{
    private static final long serialVersionUID = 6126976733458698461L;
    BurnDevice localDevice;
    RemoteDiscBurningEventDelivery eventDelivery;
    
    public RemoteBurnDeviceImpl(BurnDevice localDevice) throws RemoteException
    {
        super();
        this.localDevice = localDevice;
        localDevice.addStatusChangedListener(this);
    }

    public RemoteBurnDeviceImpl(BurnDevice localDevice, int port) throws RemoteException
    {
        super(port);
        this.localDevice = localDevice;
        localDevice.addStatusChangedListener(this);
    }

    public RemoteBurnDeviceImpl(BurnDevice localDevice, int port, RMIClientSocketFactory csf,
            RMIServerSocketFactory ssf) throws RemoteException
    {
        super(port, csf, ssf);
        this.localDevice = localDevice;
        localDevice.addStatusChangedListener(this);
    }

    public String getName() throws RemoteException
    {
        return localDevice.getName();
    }

    public BurnState getBurnState() throws RemoteException
    {
        return localDevice.getBurnState();
    }

    public void openTray() throws IOException
    {
        localDevice.openTray();
    }

    public void closeTray() throws IOException
    {
        localDevice.closeTray();
    }

    public void ejectMedia() throws IOException
    {
        localDevice.ejectMedia();
    }

    public long getAvailableSpace() throws RemoteException
    {
        return localDevice.getAvailableSpace();
    }

    public void setEventDelivery(RemoteDiscBurningEventDelivery eventDelivery)
    {
        this.eventDelivery = eventDelivery;
    }

    public void burnDeviceStatusChanged(BurnDeviceStatusChanged event)
    {
        if (eventDelivery != null)
        {
            try
            {
                eventDelivery.burnDeviceEvent(event);
            } catch (RemoteException e)
            {
                eventDelivery = null;
                Logger.getLogger(getClass()).debug(new DebugLogMessage("Caught exception delivering event, removing remote eventDelivery"), e);
                throw new DeadListenerError("Remote listener not responding", e);
            }
        }
    }

    public BurnDeviceID getID() throws RemoteException
    {
        return localDevice.getID();
    }

    public MediaState getMediaState() throws RemoteException
    {
        return localDevice.getMediaState();
    }

    public MediaStatus getMediaStatus() throws RemoteException
    {
        return localDevice.getMediaStatus();
    }

    public MediaType getMediaType() throws RemoteException
    {
        return localDevice.getMediaType();
    }

    public double[] getAvailableSpeeds() throws RemoteException
    {
        return localDevice.getAvailableSpeeds();
    }

    public boolean acquireExclusiveAccess() throws IOException
    {
        return localDevice.acquireExclusiveAccess();
    }
    
    public boolean isAccessExclusive() throws RemoteException
    {
        return localDevice.isAccessExclusive();
    }

    public void askForMediaReservation() throws IOException
    {
        localDevice.askForMediaReservation();
    }

    public boolean isMediaReserved() throws RemoteException
    {
        return localDevice.isMediaReserved();
    }

    public void releaseMediaReservation() throws IOException
    {
        localDevice.releaseMediaReservation();
    }
    
    public void releaseExclusiveAccess() throws IOException
    {
        localDevice.releaseExclusiveAccess();
    }

    public boolean canTrayOpen() throws RemoteException
    {
        return localDevice.canTrayOpen();
    }

    public boolean isTrayOpen() throws RemoteException
    {
        return localDevice.isTrayOpen();
    }

    public MediaType[] getSupportedMedia() throws RemoteException
    {
        return localDevice.getSupportedMedia();
    }
}
