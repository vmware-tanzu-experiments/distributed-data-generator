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

import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import com.igeekinc.util.discburning.BurnDeviceEvent;
import com.igeekinc.util.discburning.BurnDeviceEventListener;

public class RemoteDiscBurningEventDeliveryImpl extends UnicastRemoteObject
        implements RemoteDiscBurningEventDelivery
{
    BurnDeviceEventListener localProxy;
    
    /**
     * 
     */
    private static final long serialVersionUID = 4369795650328773718L;

    public RemoteDiscBurningEventDeliveryImpl(BurnDeviceEventListener localProxy) throws RemoteException
    {
        super();
        this.localProxy = localProxy;
    }

    public RemoteDiscBurningEventDeliveryImpl(BurnDeviceEventListener localProxy, int port) throws RemoteException
    {
        super(port);
        this.localProxy = localProxy;
    }

    public RemoteDiscBurningEventDeliveryImpl(BurnDeviceEventListener localProxy, int port,
            RMIClientSocketFactory csf, RMIServerSocketFactory ssf)
            throws RemoteException
    {
        super(port, csf, ssf);
        this.localProxy = localProxy;
    }

    public void burnDeviceEvent(BurnDeviceEvent firedEvent)
            throws RemoteException
    {
        localProxy.burnDeviceEvent(firedEvent);
    }
}
