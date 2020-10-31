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

import com.igeekinc.util.CheckCorrectDispatchThread;
import com.igeekinc.util.EventDeliverySupport;
import com.igeekinc.util.EventHandler;

public abstract class BurnDevice
{
    public static final double kCD1xSpeed = 176.4; // KB/s
    public static final double kDVD1xSpeed = 1385.0; // KB/s
    
    private String name;
    private BurnDeviceID id;
    protected EventDeliverySupport deliverySupport;
    private final class StatusChangedAdapter implements EventHandler
    {
        BurnDeviceStatusChangedListener listener;
        public StatusChangedAdapter(BurnDeviceStatusChangedListener listener)
        {
            this.listener = listener;
        }
        public void handleEvent(java.util.EventObject eventToHandle) 
        {
            listener.burnDeviceStatusChanged((BurnDeviceStatusChanged)eventToHandle);
        }
        public int hashCode()
        {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((listener == null) ? 0 : listener.hashCode());
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
            final StatusChangedAdapter other = (StatusChangedAdapter) obj;
            if (listener == null)
            {
                if (other.listener != null)
                    return false;
            } else if (!listener.equals(other.listener))
                return false;
            return true;
        }
        
        
    }
    protected BurnDevice(String inName, BurnDeviceID inID, CheckCorrectDispatchThread checker)
    {
        name = inName;
        id = inID;
        deliverySupport = new EventDeliverySupport(checker);
    }
    
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the current status of the device - idle, empty, burning, verifying, etc.
     * @return
     */
    public abstract BurnState getBurnState();
    /**
     * Returns the current state of the media - no media, present, transitioning (mounting)
     * @return
     */
    public abstract MediaState getMediaState();
    /**
     * Return the kind of media loaded - CD, DVD, etc.
     * @return
     */
    public abstract MediaType getMediaType();
    /**
     * Return the status of the media (erasable, appendable, not writable, etc) as an array of media statuses
     * @return
     */
    public abstract MediaStatus getMediaStatus();
    
    public abstract boolean canTrayOpen();  // Whether or not the device can "open" (usually means has a tray)
    public abstract boolean isTrayOpen();
    public abstract void openTray() throws IOException;
    public abstract void closeTray() throws IOException;
    public abstract void ejectMedia() throws IOException;

    public abstract long getAvailableSpace();
    
    public void addStatusChangedListener(BurnDeviceStatusChangedListener listener)
    {
        deliverySupport.addEventHandler(BurnDeviceStatusChanged.class, new StatusChangedAdapter(listener));
    }
    
    public void removeStatusChangedListener(BurnDeviceStatusChangedListener removeListener)
    {
        deliverySupport.removeEventHandler(BurnDeviceStatusChanged.class, new StatusChangedAdapter(removeListener));
    }
    /**
     * Returns a BurnDeviceID that uniquely identifies this BurnDevice and can be passed to DiscBurning to
     * retrieve this BurnDevice again
     * @return
     */
    public BurnDeviceID getID()
    {
        return id;
        
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
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
        final BurnDevice other = (BurnDevice) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    /**
     * Gets the available speed multiple as closest integers
     * A cd or dvd must be loaded or int[0] will be returned
     */
    public int [] getAvailableSpeedMultiples()
    {
        double [] availableSpeeds = getAvailableSpeeds();
        int [] returnMultiples = new int[availableSpeeds.length];
        if (!getMediaState().equals(MediaState.kPresentState) ||
                getMediaType() == null || getMediaType().equals(MediaType.kMediaTypeUnknown) ||
                getMediaType().equals(MediaType.kMediaTypeNone))
            return new int[0];
        double baseSpeed;
        if (getMediaType().isCDMedia())
            baseSpeed = kCD1xSpeed;
        else
            baseSpeed = kDVD1xSpeed;
        for (int curSpeedNum = 0; curSpeedNum < availableSpeeds.length; curSpeedNum++)
        {
            int curMultiple = (int)((availableSpeeds[curSpeedNum]/baseSpeed)+.5);
            returnMultiples[curSpeedNum] = curMultiple;
        }
        return returnMultiples;
    }
    /**
     * Returns speeds in KB/s
     * @return
     */
    public abstract double [] getAvailableSpeeds();
    
    /**
     * Requests a reservation for the media in the drive.
     * A MediaReserved event will be generated if the media is succcessfully reserveed
     * @throws IOException 
     */
    public abstract void askForMediaReservation() throws IOException;
    public abstract boolean isMediaReserved();
    public abstract void releaseMediaReservation() throws IOException;
    /**
     * Acquires exclusive access to the device for burning
     * @return
     */
    public abstract boolean acquireExclusiveAccess() throws IOException;
    public abstract boolean isAccessExclusive();
    public abstract void releaseExclusiveAccess() throws IOException;
    public abstract MediaType [] getSupportedMedia();
}
