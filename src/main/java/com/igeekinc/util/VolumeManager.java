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
 
package com.igeekinc.util;

import java.io.IOException;

public abstract class VolumeManager extends ChangeModel
{
	public VolumeManager(CheckCorrectDispatchThread checker)
	{
		super(checker);
	}
	public abstract Volume [] getVolumes();
	public abstract Volume getBootDrive();
	public abstract Volume [] getExternalDrives();
	public abstract Volume [] getRemovableDrives();
    public abstract Volume getVolumeForPath(FilePath path) throws IOException;
	
    public Volume getVolumeForPath(String path)
    throws IOException
    {
        FilePath findPath = FilePath.getFilePath(path);
        return getVolumeForPath(findPath);
    }
    
	public Volume getLargestExternal()
	{
		Volume [] externalVolumes = getExternalDrives();
		Volume bootVolume = getBootDrive();
		
		long lastBiggest = 0;
		Volume maxAvailable = null;
		for (int curVolumeNum = 0; curVolumeNum < externalVolumes.length; curVolumeNum++)
		{
			Volume curVolume = externalVolumes[curVolumeNum];
			if (curVolume.freeSpace() > lastBiggest && !curVolume.equals(bootVolume))
			{
				maxAvailable = curVolume;
				lastBiggest = curVolume.freeSpace();
			}
		}
		return(maxAvailable);
	}
	
	public static final int kMountedOK = 0;
	public static final int kMountTimedOut = 1;
	public static final int kPermissionDenied = 2;
	public static final int kMountFailed = 3;
	
	public abstract int mountVolume(Volume volumeToMount, long timeout, boolean userInteractionAllowed);
	public static final int kMountable = 0;
	public static final int kNeedsStoredPassword = 1;
	public static final int kNeedsAccessToStoredPassword = 2;
    public static final String kVolumeAddedPropertyName = "volumeAdded";
    public static final String kVolumeRemovedPropertyName = "volumeRemoved";
	public abstract int ensureMountablity(Volume volumeToPreflight);
	
}
