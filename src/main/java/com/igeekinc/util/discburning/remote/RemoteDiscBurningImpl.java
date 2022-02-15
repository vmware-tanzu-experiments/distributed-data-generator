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
import com.igeekinc.util.discburning.BurnDeviceEvent;
import com.igeekinc.util.discburning.BurnDeviceEventListener;
import com.igeekinc.util.discburning.BurnDeviceID;
import com.igeekinc.util.discburning.BurnProgressIndicator;
import com.igeekinc.util.discburning.BurnSetupProperties;
import com.igeekinc.util.discburning.BurnVolume;
import com.igeekinc.util.discburning.DiscBurning;
import com.igeekinc.util.logging.ErrorLogMessage;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import java.io.IOException;
import java.rmi.RemoteException;
import org.apache.logging.log4j.LogManager;



public class RemoteDiscBurningImpl extends java.rmi.server.UnicastRemoteObject implements RemoteDiscBurning, BurnDeviceEventListener
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 5464487470720271026L;
    
    DiscBurning localDiscBurning;
    RemoteDiscBurningEventDelivery eventDelivery;
    public RemoteDiscBurningImpl(DiscBurning localDiscBurning)
    throws RemoteException
    {
        this.localDiscBurning = localDiscBurning;
        this.localDiscBurning.addBurnDeviceEventListener(this);
    }
    
    @Override
    public BurnVolume createRecordableVolume(String volumeName,
            PauserControlleeIF pauser) throws IOException, AbortedException, RemoteException
    {
        return localDiscBurning.createRecordableVolume(volumeName, pauser);
    }

    @Override
    public void discardRecordableVolume(BurnVolume volumeToDiscard)
    throws RemoteException, IOException
    {
        localDiscBurning.discardRecordableVolume(volumeToDiscard);
    }

    @Override
    public void burnVolume(BurnDevice burnDevice, BurnVolume volumeToBurn,
            BurnSetupProperties burnProperties, BurnProgressIndicator burnProgress,
            PauserControlleeIF pauser) throws IOException, AbortedException, RemoteException
    {
        localDiscBurning.burnVolume(burnDevice, volumeToBurn, burnProperties, burnProgress, pauser);
    }

    @Override
    public RemoteBurnDevice[] getBurningDevices() throws RemoteException
    {
        BurnDevice [] localDevices = localDiscBurning.getBurningDevices();
        RemoteBurnDevice [] remoteDevices = new RemoteBurnDevice[localDevices.length];
        for (int curLocalDeviceNum = 0; curLocalDeviceNum < localDevices.length; curLocalDeviceNum++)
        {
            remoteDevices[curLocalDeviceNum] = new RemoteBurnDeviceImpl(localDevices[curLocalDeviceNum]);
        }
        return remoteDevices;
    }

    @Override
    public void burnDeviceEvent(BurnDeviceEvent firedEvent)
    {
        if (eventDelivery != null)
            try
            {
                eventDelivery.burnDeviceEvent(firedEvent);
            } catch (RemoteException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught remote exception delivering BurnDeviceEvent"), e);
            }
    }

    @Override
    public void setBurnDeviceEventDelivery(RemoteDiscBurningEventDelivery deliveryObject) throws RemoteException
    {
        eventDelivery = deliveryObject;
    }

    @Override
    public RemoteBurnDevice getBurnDeviceForID(BurnDeviceID deviceID) throws RemoteException
    {
        BurnDevice deviceForID = localDiscBurning.getBurnDeviceForID(deviceID);
        RemoteBurnDevice returnDevice = null;
        if (deviceForID != null)
            returnDevice = new RemoteBurnDeviceImpl(deviceForID);
        return returnDevice;
    }

    @Override
    public void close() throws IOException, RemoteException
    {
        localDiscBurning.close();
    }
    
    
}
