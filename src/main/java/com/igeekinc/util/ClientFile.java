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

import com.igeekinc.util.exceptions.ForkNotFoundException;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;




/**
 * <p>Title: Indelible File System</p>
 * <p>Description: ClientFile represents a file that is in a client file system.  ClientFile
 * implements a more faithful representation of the client file then the standard java.io.File class.
 * ClientFile is an abstract class - concrete classes are implemented for each filesystem supported</p>
 */

public abstract class ClientFile extends File implements Serializable, FileLike
{


static final long serialVersionUID = 7319714950392853867L;
  //String base, backupPath;
  FilePath base, backupPath;
  Volume volume;
  
  private static final ObjectStreamField  [] serialPersistentFields = {
      new ObjectStreamField("base", Object.class),
      new ObjectStreamField("backupPath", Object.class),
      new ObjectStreamField("volume", Volume.class)
  };
  transient FilePath filePath;
  static String pathSeparator = SystemInfo.getSystemInfo().getSeparator();
  public ClientFile(String inBase, String fileName)
  {
    this(FilePath.getFilePath(inBase), fileName);
  }

  public ClientFile(FilePath inBase, String fileName)
  {
    super(fileName);
    base = inBase;
    FilePath volumeFindPath = getFilePath();

    try
    {

        volume = SystemInfo.getSystemInfo().getVolumeManager().getVolumeForPath(volumeFindPath);
    }
    catch (IOException e)
    {
        try
        {
            volume = SystemInfo.getSystemInfo().getVolumeManager().getVolumeForPath(volumeFindPath.getParent());
        }
        catch (IOException e1)
        {
          LogManager.getLogger(getClass()).error(new ErrorLogMessage("Could not get volume for {0} or its parent", new Serializable[]{
                    volumeFindPath.toString()
            }));
        }
    }
    setBackupPath();
  }
  
  public ClientFile(Volume inVolume, FilePath inBase, String fileName)
  {
    super(fileName);
    volume = inVolume;
    base = inBase;
    setBackupPath();
  }
  
  public ClientFile(Volume inVolume, String inBase, String fileName)
  {
      this(inVolume, FilePath.getFilePath(inBase), fileName);
  }

  public ClientFile(ClientFile parent, String fileName)
  {
    super(parent, fileName);
    base = parent.getBase();
    volume = parent.getVolume();
    setBackupPath();
  }
  public ClientFile(Volume inVolume, ClientFile parent, String fileName)
  {
    super(parent, fileName);
    volume = inVolume;
    base = parent.getBase();
    setBackupPath();

  }
  
  protected void setBackupPath()
  {
      FilePath fullPath = getFilePath();
      /*if (!fullPath.startsWith(base))
          throw new InternalError("This file ("+fullPath+") is not a descendant of "+base);*/
      if (base.getNumComponents() == 1)
          backupPath = fullPath; // If our root is just the path separator (i.e. "/"), leave it attached
      else
          backupPath = fullPath.removeLeadingComponents(base.getNumComponents());
  }
  @Override
  public abstract int getNumForks();
  @Override
  public abstract String [] getForkNames();
  @Override
  public abstract ClientFileMetaData getMetaData() throws IOException;
  public abstract void setMetaData(ClientFileMetaData newMetaData) throws IOException;
	
  /**
   * getForkInputStream returns an input stream for the appropriate fork.  This is used
   * by ForkInputStream which tries to give creation semantics similar to FileInputStream
   * @param streamName
   * @return
   * @throws ForkNotFoundException
   */
  @Override
  public abstract InputStream getForkInputStream(String streamName) throws ForkNotFoundException;
  @Override
  public abstract OutputStream getForkOutputStream(String streamName) throws ForkNotFoundException;
  
  @Override
  public abstract InputStream getForkInputStream(String streamName, boolean noCache) throws ForkNotFoundException;
  @Override
  public abstract OutputStream getForkOutputStream(String streamName, boolean noCache) throws ForkNotFoundException;
  
  public abstract FileChannel getForkChannel(String forkName, boolean writeable) throws ForkNotFoundException;
  public abstract FileChannel getForkChannel(String forkName, boolean noCache, boolean writeable) throws ForkNotFoundException;
  public Volume getVolume()
  {
      if (volume == null)
      {
        try
        {
            volume = SystemInfo.getSystemInfo().getVolumeManager().getVolumeForPath(getFilePath());
        } catch (IOException e)
        {
          LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
        }
      }
    return volume;
  }
  public FilePath getBase()
  {
    return base;
  }

  public void setBase(FilePath newBase)
  {
    base=newBase;
    setBackupPath();
  }

  public void setBase(ClientFile newBase)
  {
    setBase(newBase.getFilePath());
  }
  /**
   * getBackupPartialPath returns a path that is suitable for concatenation to another path.
   * This skips the base directory
   */
  @Override
  public FilePath getBackupPartialPath()
  {
    return(backupPath);
  }
  public ClientFile[] listClientFiles()
  {
  	return listClientFiles(null);

  }
  
  public abstract ClientFile[] listClientFiles(FilenameFilter filter);
  
  protected <T extends ClientFile>T [] listClientFilesInt(FilenameFilter filter, Class<T>classType)
  {
    String[] fileNames;
    if (filter != null)
    		fileNames = list(filter);
    else
    		fileNames = list();
    
    ArrayList<T> returnFileVec = new ArrayList<T>();
    if (fileNames == null) 
        return null;
    int numFiles = fileNames.length;

    for (int curFileNum = 0; curFileNum < numFiles; curFileNum++)
    {
      ClientFile curFile;
      try
      {
        curFile = volume.getClientFile(this, fileNames[curFileNum]);
      }
      catch (IOException e)
      {
        continue;
      }
      if (curFile != null)  // On unix systems, we may have children that are on another volume.
        // In that event, volume will return null when we try to retrieve them
        returnFileVec.add((T)curFile);
    }
    T[] returnFiles = (T[])Array.newInstance(classType, returnFileVec.size());
    	
    returnFileVec.toArray(returnFiles);
    return returnFiles;
  }
  
  @Override
  public ClientFile getChild(String childName)
  throws IOException
  {
      if (!exists())
          throw new IOException(getAbsolutePath() + " does not exist or is not accessible");
  	if (!isDirectory())
  		throw new IOException(getAbsolutePath() + " is not a directory - cannot resolve child "+childName);
  	return volume.getClientFile(this, childName);
  }
  
  @Override
  public ClientFile getChild(FilePath childPath)
  throws IOException
  {
    if (!isDirectory())
        throw new IOException(getAbsolutePath() + " is not a directory - cannot resolve child "+childPath);
    return volume.getClientFile(this, childPath.getPath());
  }
  
  /**
   * Returns the parent of this file.  If the file is the root, returns null
   * @return
   * @throws IOException
   */
  public ClientFile getParentClientFile()
  throws IOException
  {
      String parentFile = getParent();
      if (parentFile == null)
          return null;
      ClientFile returnFile = SystemInfo.getSystemInfo().getClientFileForPath(parentFile);
      if (returnFile.getVolume().equals(getVolume()))
          returnFile.setBase(base);
      else
          returnFile.setBase(returnFile.getVolume().getRoot());
      return returnFile;
  }
  
  @Override
  public String toString()
  {
    return(getAbsolutePath());
  }

  @Override
  public boolean isMountPoint()
  {
    return false; // override for Unix & Mac OS X
  }
  
  public String getVolumeRelativePath()
  {
  	String volumePath = volume.getRoot().getAbsolutePath();
  	String returnString = getAbsolutePath();
  	if (!returnString.startsWith(volumePath))
  		throw new InternalError("volume path out of sync with file path");
  	returnString = returnString.substring(volumePath.length());
  	while (returnString.startsWith(separator))
  		returnString = returnString.substring(1);
  	return(returnString);
  	
  }
  
  @Override
  public long totalLength()
  {
  	return length();
  }
  
  @Override
  public FilePath getFilePath()
  {
      if (filePath == null)
          filePath = FilePath.getFilePath(getAbsolutePath());
      return filePath;
  }
  
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
  {
      // prepare to read the alternate persistent fields
      ObjectInputStream.GetField fields = null;

      fields = ois.readFields();
      
      volume = (Volume)fields.get("volume", null);
      Object baseObj, backupPathObj;
      baseObj = fields.get("base", null);
      backupPathObj = fields.get("backupPath", null);
      // Prior to Indelible 2, we stored base and backupPath as Strings not FilePath
      if (baseObj != null)
      {
          if (baseObj instanceof String)
              base = FilePath.getFilePath((String)baseObj);
          if (baseObj instanceof FilePath)
              base = (FilePath)baseObj;
      }
      if (backupPathObj != null)
      {
          if (backupPathObj instanceof String)
              backupPath = FilePath.getFilePath((String)backupPathObj);
          if (backupPathObj instanceof FilePath)
              backupPath = (FilePath)backupPathObj;
      }
  }
  
  private void writeObject(ObjectOutputStream s) throws IOException 
  {

      // set the values of the Serializable fields
      ObjectOutputStream.PutField fields = s.putFields();
      fields.put("volume", volume);
      fields.put("base", base);
      fields.put("backupPath", backupPath);

      // save them
      s.writeFields();        
  }

  public ClientFileIterator iterator()
  {
      return new DefaultClientFileIterator(this);
  }
  
  @Override
	public boolean renameTo(File dest)
	{
	  	filePath = null;
		return super.renameTo(dest);
	}
}
