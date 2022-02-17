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

import com.igeekinc.util.exceptions.FeatureNotSupportedException;
import com.igeekinc.util.exceptions.GroupNotFoundException;
import com.igeekinc.util.exceptions.UserNotFoundException;
import com.igeekinc.util.fileinfo.FileInfoDBManager;
import com.igeekinc.util.fsevents.FSEventsProcessor;
import com.igeekinc.util.fsevents.FileStateChangedEventListener;
import com.igeekinc.util.fsevents.FileStateChangedSupport;
import com.igeekinc.util.logging.ErrorLogMessage;
import com.igeekinc.util.msgpack.ClientFileMetaDataMsgPack;
import com.igeekinc.util.scripting.ScriptExecutor;
import com.igeekinc.util.xmlserial.XMLObjectParseHandler;
import com.igeekinc.util.xmlserial.XMLObjectSerializeHandler;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public abstract class SystemInfo// extends ChangeModel
{
  static SystemInfo mySystemInfo;
  CheckCorrectDispatchThread eventDispatcher = null;
  protected static FileInfoDBManager fileInfoDBManager;
  protected FileStateChangedSupport fileStateChangedSupport;
  

  @SuppressWarnings("unchecked")
static public synchronized SystemInfo getSystemInfo()
  {
    String  osName = System.getProperty("os.name"); //$NON-NLS-1$
    String className = null;
    Logger logger = LogManager.getLogger(SystemInfo.class);
    
    if (mySystemInfo == null)
    {
      if (osName.equals("Windows 2000") || osName.equals("Windows XP") || osName.equals("Windows Vista") || osName.equals("Windows 7")) //$NON-NLS-1$
      {
        className = "com.igeekinc.util.windows.WindowsSystemInfo"; //$NON-NLS-1$
      }

      if (osName.equals("Mac OS X") || osName.equals("Darwin")) //$NON-NLS-1$
      {
      	className = "com.igeekinc.util.macos.macosx.MacOSXSystemInfo"; //$NON-NLS-1$
      }
      
      if (osName.equals("Linux")) //$NON-NLS-1$
      {
    	  className = "com.igeekinc.util.linux.LinuxSystemInfo";	//$NON-NLS-1$
      }
      try
	  {
      	Class<? extends SystemInfo> systemInfoClass = (Class<? extends SystemInfo>) Class.forName(className);
      	
      	Class<?> [] constructorArgClasses = {};
      	Constructor systemInfoConstructor = systemInfoClass.getConstructor(constructorArgClasses);
      	Object [] constructorArgs = {};
      	mySystemInfo = (SystemInfo)systemInfoConstructor.newInstance(constructorArgs);
      }
      catch (Exception e)
	  {
      	e.printStackTrace();
      	logger.error("Caught exception creating SystemInfo", e); //$NON-NLS-1$
      	throw new InternalError("Caught exception creating SystemInfo"); //$NON-NLS-1$
      }
    }
    if (mySystemInfo == null)
      throw new InternalError("System type "+osName+" is unknown"); //$NON-NLS-1$ //$NON-NLS-2$
    return(mySystemInfo);
  }

  public abstract  EthernetID getEthernetID();
  public abstract String getSystemName();
  
  public abstract User getAdminUser();
  public abstract User getUser() throws UserNotFoundException;
  public abstract User getExecutingUser() throws UserNotFoundException;
  
  public abstract User getUserInfoForName(String userName) throws UserNotFoundException;
  public abstract User getUserInfoForUID(int uid) throws UserNotFoundException;
  
  public abstract Group getGroupInfoForName(String groupName) throws GroupNotFoundException;
  public abstract Group getGroupInfoForGID(int gid) throws GroupNotFoundException;
  
  public File getGlobalPreferencesDirectory()
  {
      if (System.getProperty("com.igeekinc.util.globalPreferencesDir") != null)
      {
          return (new File(System.getProperty("com.igeekinc.util.globalPreferencesDir")));
      }
      else
          return null;
  }
  public File getUserPreferencesDirectory()
  {
      if (System.getProperty("com.igeekinc.util.userPreferencesDir") != null)
      {
          return (new File(System.getProperty("com.igeekinc.util.userPreferencesDir")));
      }
      else
          return null;      
  }
  
  public abstract File getPreferencesDirectoryForUser(User user);
  
  public abstract File getUserLogDirectory();
  public abstract File getTemporaryDirectory();
  public abstract File getLogDirectory();
  public abstract File getCacheDirectory();
  /**
   * Directory where we can put info about running applications (e.g. /var/run)
   * @return
   */
  public abstract File getRunDirectory();
  public abstract FileCopy getFileCopy();

  public abstract VolumeManager getVolumeManager();
  public abstract SecurityManager getSecurityManager();
  public abstract PowerManager getPowerManager() throws FeatureNotSupportedException;
  
  public abstract boolean openUrl(URL url);
  public abstract void openInTextEditor(File file);
  public abstract void displayPDFFile(File file);
  
  public ClientFile getClientFileForPath(String pathStr) 
  throws IOException
  {
      if (pathStr == null)
          return null;
      FilePath path = FilePath.getFilePath(pathStr);
      return getClientFileForPath(path);
  }

  public ClientFile getClientFileForPath(FilePath path) 
  throws IOException
  {
        Volume fileVolume = getVolumeManager().getVolumeForPath(path);
        if (fileVolume == null)
            throw new IOException("Could not find volume for path "+path);
        return(fileVolume.getClientFile("/", path.toString()));
  }
  
  public ClientFile getClientFileForFile(File newFile) throws IOException
  {
      return getClientFileForPath(FilePath.getFilePath(newFile));
  }
  
  public abstract ClientFile getUsersDirectory() throws IOException;
  /**
   * getSeparatorChar is here so that we can override it for Mac OS X where
   * file returns ":" but uses "/" everywhere
   */
  public char getSeparatorChar()
  {
    return (File.separatorChar);
  }
  public String getSeparator()
  {
    return (File.separator);
  }
  public User getSocketAuthenticatedUser()
  throws UserNotFoundException
  {
	  /*
	  try
	  {
		  PeerCredentials peerCredentials = AFUNIXSocket.getThreadPeerCredentials();
		  if (peerCredentials != null)
		  {
			  return getUserInfoForUID(peerCredentials.getUID());
		  }
		  String userName = cryptix.sasl.rmi.SaslSocket.getSASLUsername();
		  if (userName.equals("indelibleRMI"))
			  userName = getAdminUser().getUserName();
		  return(getUserInfoForName(userName));
	  }
	  catch (java.rmi.server.ServerNotActiveException e)
	  {
		  String userName = System.getProperty("user.name"); //$NON-NLS-1$
		  return(getUserInfoForName(userName));
	  }
	  catch(Exception e)
	  {
		  Logger.getLogger(getClass()).error(new ErrorLogMessage("Caught exception retrieving SASL authenticated user"), e);
		  return null;
	  }
	  */
	  return null;	// TODO - fix this
  }
  
  public abstract ScriptExecutor getScriptExecutor();
  
  protected int osMajorVersion, osMinorVersion, osPointVersion;
  

  /*
  public PauseAbort getSleepPauser()
  {
  	return sleepPauser;
  }

  public abstract PowerManager getPowerManager();
    */
  public abstract Class<? extends FilePath> getFilePathClass();
  
  public abstract Class<? extends FilePackage> getFilePackageClass();
  
  public abstract OSType getOSType();
  
  public int getOsMajorVersion() 
  {
      return osMajorVersion;
  }
  public int getOsMinorVersion() 
  {
      return osMinorVersion;
  }
  public int getOsPointVersion() 
  {
      return osPointVersion;
  }
  
  public abstract FSEventsProcessor getFSEventsProcessor() throws FeatureNotSupportedException;
  public synchronized void setDispatcher(CheckCorrectDispatchThread inDispatcher)
  {
      eventDispatcher = inDispatcher;
      getVolumeManager().setDispatcher(eventDispatcher);
      if (fileStateChangedSupport != null)
    	  fileStateChangedSupport.setDispatcher(eventDispatcher);
  }
  
  public abstract Date getSystemBootTime();
  
  public long getSystemUptime()
  {
      Date bootTime = getSystemBootTime();
      long uptime = System.currentTimeMillis() - bootTime.getTime();
      return uptime;
  }
  
  public abstract User [] getActiveGUIUsers();
  
  public abstract UserIterator getUsers();
  public abstract GroupIterator getGroups();
  
  public FileInfoDBManager getFileInfoDBManager()
  {
      if (fileInfoDBManager == null)
          fileInfoDBManager = createFileInfoDBManager();
      return fileInfoDBManager;
  }
  
  protected abstract FileInfoDBManager createFileInfoDBManager();
   
  static public boolean is64BitVM()
  {
      boolean is64Bit = false;
      String bits = System.getProperty("sun.arch.data.model", "?");
      if (bits.equals("64"))
      {
          is64Bit = true;
      }
      if (bits.equals("?"))
      {
          // probably sun.arch.data.model isn't available
          // maybe not a Sun JVM?
          // try with the vm.name property
          is64Bit = System.getProperty("java.vm.name").toLowerCase().indexOf("64") >= 0;
      }
      return is64Bit;
  }
  
  public static boolean is32BitVM()
  {
      return !is64BitVM();
  }
  
  public abstract int getNativeIntSize();
  public abstract int getNativeLongSize();
  
  public ProcessorType getProcessorType()
  {
      String osArch = System.getProperty("os.arch");
      if (osArch.equals("i386") || osArch.equals("universal") || osArch.equals("x86_64"))
          return ProcessorType.kIntel;
      if (osArch.equals("powerpc") || osArch.equals("ppc"))
          return ProcessorType.kPowerPC;
      return null;
  }
  
  public InterfaceAddressInfo [] getActiveAddresses()
  {
	  ArrayList<InterfaceAddressInfo>returnInfoList = new ArrayList<InterfaceAddressInfo>();
	  try
	  {
		  Enumeration<NetworkInterface>interfaces = NetworkInterface.getNetworkInterfaces();
		  while (interfaces.hasMoreElements())
		  {
			  NetworkInterface curInterface = interfaces.nextElement();
			  if (isUp(curInterface))
			  {
				  ArrayList<InterfaceAddressInfo> infoForInterface = getInfoForInterface(curInterface);
				  returnInfoList.addAll(infoForInterface);
			  }
		  }
	  } catch (SocketException e)
	  {
		  // TODO Auto-generated catch block
		  LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  }
	  InterfaceAddressInfo [] returnInfo = returnInfoList.toArray(new InterfaceAddressInfo[returnInfoList.size()]);
	  return returnInfo;
  }
  
  private boolean getInterfaceAddressesMethodChecked = false;
  private Method getInterfaceAddressesMethod;
  private Class<?> interfaceAddressClass;
  private Method getAddressMethod, getNetworkPrefixLengthMethod;
  public ArrayList<InterfaceAddressInfo> getInfoForInterface(NetworkInterface curInterface)
  {
	  try
	  {
		  if (!getInterfaceAddressesMethodChecked)
		  {
			  getInterfaceAddressesMethodChecked = true;
			  getInterfaceAddressesMethod = NetworkInterface.class.getMethod("getInterfaceAddresses");
			  interfaceAddressClass = Class.forName("java.net.InterfaceAddress");
			  getAddressMethod = interfaceAddressClass.getMethod("getAddress");
			  getNetworkPrefixLengthMethod = interfaceAddressClass.getMethod("getNetworkPrefixLength");
		  }
	  } catch (NoSuchMethodException e)
	  {
		  // TODO Auto-generated catch block
		  LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  } catch (SecurityException e)
	  {
		  // TODO Auto-generated catch block
		  LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  } catch (ClassNotFoundException e)
	  {
		  // TODO Auto-generated catch block
		  LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  }
	  
	  ArrayList<InterfaceAddressInfo>returnInfoList = new ArrayList<InterfaceAddressInfo>();
	  if (getInterfaceAddressesMethod != null)
	  {
		  try
		{
			List interfaceAddressesList = (List) getInterfaceAddressesMethod.invoke(curInterface);
			  for (Object curInterfaceAddress:interfaceAddressesList)
			  {
				  InetAddress interfaceAddress = (InetAddress) getAddressMethod.invoke(curInterfaceAddress);
				  short networkPrefixLength = (Short) getNetworkPrefixLengthMethod.invoke(curInterfaceAddress);
				  InterfaceAddressInfo curInfo = new InterfaceAddressInfo(interfaceAddress, networkPrefixLength);
				  returnInfoList.add(curInfo);
			  }
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
		}
	  }
	  else
	  {
		  
	  }
	  return returnInfoList;
  }
  
  private Method	isUpMethod;
  private boolean	isUpMethodChecked = false;
  private boolean isUp(NetworkInterface checkInterface)
  {
	  try
	  {
		  if (!isUpMethodChecked)
		  {
			  isUpMethodChecked = true;		// Whether it succeeds or not we checked it so it's not going to change later
			  isUpMethod = NetworkInterface.class.getMethod("isUp");
		  }
		  if (isUpMethod != null)
			  return (Boolean)isUpMethod.invoke(checkInterface);
	  } catch (NoSuchMethodException e)
	  {
	  } catch (SecurityException e)
	  {
	  } catch (IllegalAccessException e)
	  {
        LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  } catch (IllegalArgumentException e)
	  {
        LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  } catch (InvocationTargetException e)
	  {
        LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
	  }
	  return true;	// 1.5 or lower - all interfaces are always up
  }
  
  
  public abstract void restartSystem();
  
  /**
   * Returns the amount of time the system has been idle (no user activity) in milliseconds
   * @return
   */
  public abstract long getSystemIdleTime();
  
  public abstract Class<?> getMetaDataClass();
  public abstract Class<? extends XMLObjectSerializeHandler<? extends ClientFileMetaData>> getMetaDataSerializerClass();
  public abstract Class<? extends XMLObjectParseHandler<? extends ClientFileMetaData>> getMetaDataParserClass();
  
  public synchronized void addFileStateChangedListener(FilePath listenPath, FileStateChangedEventListener listener)
  {
	  if (fileStateChangedSupport == null)
	  {
		  fileStateChangedSupport = new FileStateChangedSupport(eventDispatcher);
	  }
	  fileStateChangedSupport.addFileStateChangeListener(listenPath, listener);
  }
  
  public synchronized void removeFileStateChangedListener(FilePath listenPath, FileStateChangedEventListener listener)
  {
	  if (fileStateChangedSupport != null)
	  {
		  fileStateChangedSupport.removeFileStateChangeListener(listenPath, listener);
	  }
  }

  public abstract Class<? extends ClientFileMetaDataMsgPack> getMetaDataMsgPackSerializer();
  
  /**
   * Returns the "compare all" bitmask.  Override in OS specific SystemInfo to exclude bits that are not settable (e.g. Unix
   * change time)
   * @return
   */
  public ClientFileMetaDataCompareAttr getCompareAllAttrsBitMask()
  {
	  return ClientFileMetaDataCompareAttr.kAllAttrs;
  }
}