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
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import org.apache.logging.log4j.LogManager;



public class RemoteBurnDeviceImpl extends java.rmi.server.UnicastRemoteObject implements
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

    @Override
    public String getName() throws RemoteException
    {
        return localDevice.getName();
    }

    @Override
    public BurnState getBurnState() throws RemoteException
    {
        return localDevice.getBurnState();
    }

    @Override
    public void openTray() throws IOException
    {
        localDevice.openTray();
    }

    @Override
    public void closeTray() throws IOException
    {
        localDevice.closeTray();
    }

    @Override
    public void ejectMedia() throws IOException
    {
        localDevice.ejectMedia();
    }

    @Override
    public long getAvailableSpace() throws RemoteException
    {
        return localDevice.getAvailableSpace();
    }

    @Override
    public void setEventDelivery(RemoteDiscBurningEventDelivery eventDelivery)
    {
        this.eventDelivery = eventDelivery;
    }

    @Override
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
                LogManager.getLogger(getClass()).debug(new DebugLogMessage("Caught exception delivering event, removing remote eventDelivery"), e);
                throw new DeadListenerError("Remote listener not responding", e);
            }
        }
    }

    @Override
    public BurnDeviceID getID() throws RemoteException
    {
        return localDevice.getID();
    }

    @Override
    public MediaState getMediaState() throws RemoteException
    {
        return localDevice.getMediaState();
    }

    @Override
    public MediaStatus getMediaStatus() throws RemoteException
    {
        return localDevice.getMediaStatus();
    }

    @Override
    public MediaType getMediaType() throws RemoteException
    {
        return localDevice.getMediaType();
    }

    @Override
    public double[] getAvailableSpeeds() throws RemoteException
    {
        return localDevice.getAvailableSpeeds();
    }

    @Override
    public boolean acquireExclusiveAccess() throws IOException
    {
        return localDevice.acquireExclusiveAccess();
    }
    
    @Override
    public boolean isAccessExclusive() throws RemoteException
    {
        return localDevice.isAccessExclusive();
    }

    @Override
    public void askForMediaReservation() throws IOException
    {
        localDevice.askForMediaReservation();
    }

    @Override
    public boolean isMediaReserved() throws RemoteException
    {
        return localDevice.isMediaReserved();
    }

    @Override
    public void releaseMediaReservation() throws IOException
    {
        localDevice.releaseMediaReservation();
    }
    
    @Override
    public void releaseExclusiveAccess() throws IOException
    {
        localDevice.releaseExclusiveAccess();
    }

    @Override
    public boolean canTrayOpen() throws RemoteException
    {
        return localDevice.canTrayOpen();
    }

    @Override
    public boolean isTrayOpen() throws RemoteException
    {
        return localDevice.isTrayOpen();
    }

    @Override
    public MediaType[] getSupportedMedia() throws RemoteException
    {
        return localDevice.getSupportedMedia();
    }
}
