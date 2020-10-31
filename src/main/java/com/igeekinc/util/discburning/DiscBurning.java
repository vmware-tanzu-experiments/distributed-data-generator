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

import java.io.IOException;

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

public interface DiscBurning 
{
	/**
	 * Creates a new volume to record to.  When the volume is ready to be burnt, pass it to burnVolume
	 * @param pauser - allows for the volume creation process to be paused or aborted
	 * @return
	 * @throws IOException
	 */
	public abstract BurnVolume createRecordableVolume(String volumeName, PauserControlleeIF pauser)
		throws IOException, AbortedException;
	/**
	 * Discards the information about a volume.
	 * @param volumeToDiscard
	 * @throws IOException 
	 */
	public abstract void discardRecordableVolume(BurnVolume volumeToDiscard) throws IOException;
	/**
	 * Burns a volume to CD or DVD
	 * @param volumeToBurn
	 * @param burnProgress
	 * @param pauser
	 * @throws IOException
	 * @throws AbortedException
	 */
	public abstract void burnVolume(BurnDevice burnDevice, BurnVolume volumeToBurn, BurnSetupProperties burnProperties, BurnProgressIndicator burnProgress, PauserControlleeIF pauser)
		throws BurnFailedException, AbortedException;
	
	/**
	 * Returns all of the available burning devices in the system
	 * @return
	 */
	public abstract BurnDevice [] getBurningDevices();
	
    public abstract void addBurnDeviceEventListener(BurnDeviceEventListener newListener);
    public abstract void removeBurnDeviceEventListener(BurnDeviceEventListener removeListener);
    
    /**
     * Returns the BurnDevice for the ID.  Returns null if the device is not found
     * @return
     */
    public abstract BurnDevice getBurnDeviceForID(BurnDeviceID deviceID);

    public abstract void close() throws IOException;
}
