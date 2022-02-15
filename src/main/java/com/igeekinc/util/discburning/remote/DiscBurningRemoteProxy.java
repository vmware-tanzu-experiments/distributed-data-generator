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

import com.igeekinc.util.CheckCorrectDispatchThread;
import com.igeekinc.util.EventDeliverySupport;
import com.igeekinc.util.EventHandler;
import com.igeekinc.util.discburning.BurnDevice;
import com.igeekinc.util.discburning.BurnDeviceAppeared;
import com.igeekinc.util.discburning.BurnDeviceDisappeared;
import com.igeekinc.util.discburning.BurnDeviceEvent;
import com.igeekinc.util.discburning.BurnDeviceEventListener;
import com.igeekinc.util.discburning.BurnDeviceID;
import com.igeekinc.util.discburning.BurnFailedException;
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


/**
 * DiscBurningRemoteProxy wrappers a remote DiscBurning and also provides a local site for events
 * to be re-dispatched from
 */
public class DiscBurningRemoteProxy implements DiscBurning, BurnDeviceEventListener
{
    RemoteDiscBurning remoteDiscBurning;
    RemoteDiscBurningEventDeliveryImpl eventDeliveryImpl;
    EventDeliverySupport deliverySupport;
    CheckCorrectDispatchThread checker;
    
    private final class BurnDeviceEventListenerAdapter implements EventHandler
    {
        BurnDeviceEventListener listener;
        public BurnDeviceEventListenerAdapter(BurnDeviceEventListener listener)
        {
            this.listener = listener;
        }
        @Override
        public void handleEvent(java.util.EventObject eventToHandle) 
        {
            listener.burnDeviceEvent((BurnDeviceEvent)eventToHandle);
        }
        
        @Override
        public boolean equals(Object checkObject)
        {
            if (!(checkObject instanceof BurnDeviceEventListenerAdapter))
                return false;
            return(((BurnDeviceEventListenerAdapter)checkObject).listener.equals(listener));
        }
    }
    
    public DiscBurningRemoteProxy(RemoteDiscBurning remoteDiscBurning, CheckCorrectDispatchThread checker)
    throws RemoteException
    {
        this.remoteDiscBurning = remoteDiscBurning;
        this.checker = checker;
        deliverySupport = new EventDeliverySupport(checker);
        eventDeliveryImpl = new RemoteDiscBurningEventDeliveryImpl(this);
        remoteDiscBurning.setBurnDeviceEventDelivery(eventDeliveryImpl);
    }
    @Override
    public BurnVolume createRecordableVolume(String volumeName,
            PauserControlleeIF pauser) throws IOException, AbortedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void discardRecordableVolume(BurnVolume volumeToDiscard)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void burnVolume(BurnDevice burnDevice, BurnVolume volumeToBurn,
            BurnSetupProperties burnProperties, BurnProgressIndicator burnProgress,
            PauserControlleeIF pauser) throws BurnFailedException, AbortedException
    {
        // TODO Auto-generated method stub
    }

    @Override
    public BurnDevice[] getBurningDevices()
    {
        try
        {
            RemoteBurnDevice [] remoteDevices = remoteDiscBurning.getBurningDevices();
            BurnDeviceRemoteProxy [] localProxies = new BurnDeviceRemoteProxy[remoteDevices.length];
            for (int curDeviceNum = 0; curDeviceNum < remoteDevices.length; curDeviceNum++)
            {
                localProxies[curDeviceNum] = new BurnDeviceRemoteProxy(remoteDevices[curDeviceNum], checker);
            }
            return localProxies;
        } catch (RemoteException e)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught a remote exception getting burn devices"), e);
            return new BurnDevice[0];
        }
    }

    @Override
    public void addBurnDeviceEventListener(BurnDeviceEventListener newListener)
    {
        deliverySupport.addEventHandler(BurnDeviceEvent.class, new BurnDeviceEventListenerAdapter(newListener));
        deliverySupport.addEventHandler(BurnDeviceAppeared.class, new BurnDeviceEventListenerAdapter(newListener));
        deliverySupport.addEventHandler(BurnDeviceDisappeared.class, new BurnDeviceEventListenerAdapter(newListener));
    }

    @Override
    public void removeBurnDeviceEventListener(
            BurnDeviceEventListener removeListener)
    {
        deliverySupport.removeEventHandler(BurnDeviceEvent.class, new BurnDeviceEventListenerAdapter(removeListener));
    }
    
    /**
     * Called by RemoteDiscBurningEventDeliveryImpl to deliver an event from the remote DiscBurning
     * @param eventToDeliver
     */
    @Override
    public void burnDeviceEvent(BurnDeviceEvent eventToDeliver)
    {
        deliverySupport.sendEvent(eventToDeliver);
    }
    @Override
    public BurnDevice getBurnDeviceForID(BurnDeviceID deviceID)
    {
        try
        {
            RemoteBurnDevice remoteBurnDeviceForID = remoteDiscBurning.getBurnDeviceForID(deviceID);
            return new BurnDeviceRemoteProxy(remoteBurnDeviceForID, checker);
        } catch (RemoteException e)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
            return null;
        }
    }
    @Override
    public void close() throws IOException
    {
        try
        {
            remoteDiscBurning.close();
        } catch (RemoteException e)
        {
            LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
        }
    }
}
