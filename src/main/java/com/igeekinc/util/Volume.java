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
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class Volume implements Serializable
{
	static final long serialVersionUID =3694219538609533291L;
	protected static Logger logger = LogManager.getLogger(Volume.class);

	/**
	 * Gets a file with a path relative to the volume root
	 */
	public abstract ClientFile getRelativeClientFile(String relativePathName)
	throws IOException;
    
    public abstract ClientFile getRelativeClientFile(FilePath partialPath)
    throws IOException;
	
	public abstract ClientFile getClientFile(ClientFile parent, String fileName)
	throws IOException;
	
    public ClientFile getClientFile(ClientFile parent, FilePath relativePath)
    throws IOException
    {
        return(getClientFile(parent, relativePath.getPath()));
    }
	public abstract ClientFile getClientFile(String basePath, String relativePath)
	throws IOException;
    
    public ClientFile getClientFile(FilePath basePath, FilePath relativePath)
    throws IOException
    {
        return(getClientFile(basePath.getPath(), relativePath.getPath()));
    }
	public abstract String getVolumeName();
	public abstract String getFsType();
	public abstract String getDeviceName();
	public abstract ClientFile getRoot();
	
	public abstract boolean isExternal();
	public abstract boolean isRemovable();
	public abstract boolean isOnline();
	public abstract boolean isBootVolume();
	public abstract boolean isReadOnly();
    
	public abstract boolean isBootable() throws IOException;
	public abstract VolumeBootInfo getBootInfo() throws IOException;
	public abstract void enablePermissions() throws IOException;
	public abstract void makeBootable(VolumeBootInfo newBootInfo) throws IOException;
	
	public abstract ClientFile getTrashDirectory(User user);
	
	public boolean isInVolume(ClientFile checkFile)
	{
		Volume checkVolume = checkFile.getVolume();
        if (checkVolume != null && checkVolume.equals(this))
			return(true);
		else
			return(false);
	}
	
 @Override
	public String toString()
	{
		return(getVolumeName());
	}
	
 @Override
	public boolean equals(Object checkObject)
	{
		if (checkObject.getClass().isAssignableFrom(Volume.class))
		{
			Volume checkVolume = (Volume)checkObject;
			if (checkVolume.getVolumeName().equals(getVolumeName()))
				return true;
		}
		return false;
	}
	
	public abstract long totalSpace();
	public abstract long freeSpace();
	public abstract long filesInUse();
	public abstract void inhibitDismount() throws IOException;
	public abstract void allowDismount() throws IOException;

    public abstract boolean isRemote();
}