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

import com.igeekinc.util.datadescriptor.DataDescriptor;
import com.igeekinc.util.datadescriptor.FileDataDescriptor;
import com.igeekinc.util.exceptions.ForkNotFoundException;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import org.apache.logging.log4j.LogManager;



/**
 * A FilePackage encapsulates all of the info about a file except for its path/name.
 * It contains data descriptors for all of the forks and extended attributes along
 * with the metadata.
 * @author dave
 *
 */
public class FilePackage implements Serializable
{
	private static final long serialVersionUID = -8729030340477059962L;
	private FilePath filePath;
	private HashMap<String, ? extends DataDescriptor> forkData;
	private HashMap<String, ? extends DataDescriptor> extendedAttributeData;
	private ClientFileMetaData metaData;

	private static Constructor<?> partsConstructor, fileConstructor;
	static
	{
	    SystemInfo curSystemInfo = SystemInfo.getSystemInfo();
	    Class<?> [] partsArg = {FilePath.class, HashMap.class, HashMap.class, ClientFileMetaData.class};
        Class<?> [] fileArgs = {FilePath.class, ClientFile.class};
	    Class<? extends FilePackage> filePackageClass = curSystemInfo.getFilePackageClass();

	    try
	    {
	    	partsConstructor = filePackageClass.getConstructor(partsArg);
	    	fileConstructor = filePackageClass.getConstructor(fileArgs);
        } catch (SecurityException e)
	    {
        	LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
	    } catch (NoSuchMethodException e)
	    {
	    	LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
	    }     //$NON-NLS-1$
	}
	
	public static FilePackage getFilePackage(FilePath filePath, HashMap<String, ? extends DataDescriptor> forkData,
			HashMap<String, ? extends DataDescriptor> extendedAttributeData,
			ClientFileMetaData metaData)
	{
		Object [] args = new Object[4];
		args[0] = filePath;
		args[1] = forkData;
		args[2] = extendedAttributeData;
		args[3] = metaData;
		try
		{
			return (FilePackage) partsConstructor.newInstance(args);
		} catch (IllegalArgumentException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (InstantiationException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (IllegalAccessException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (InvocationTargetException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		}
		throw new InternalError("Could not allocate FilePackage for "+filePath);
	}
	
	public static FilePackage getFilePackage(FilePath filePath, ClientFile sourceFile) throws ForkNotFoundException, IOException
	{
		Object [] args = new Object[2];
		args[0] = filePath;
		args[1] = sourceFile;
		try
		{
			return (FilePackage) fileConstructor.newInstance(args);
		} catch (IllegalArgumentException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (InstantiationException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (IllegalAccessException e)
		{
			LogManager.getLogger(FilePackage.class).error(new ErrorLogMessage("Caught exception"), e);
		} catch (InvocationTargetException e)
		{
			if (e.getCause() instanceof IOException)
				throw (IOException)e.getCause();
		}
		throw new InternalError("Could not allocate FilePackage for "+filePath);
	}
	
	@SuppressWarnings("unchecked")
	protected FilePackage(FilePath filePath, HashMap<String, ? extends DataDescriptor> forkData,
			HashMap<String, ? extends DataDescriptor> extendedAttributeData,
			ClientFileMetaData metaData)
	{
		init(filePath, forkData, extendedAttributeData, metaData);
	}
	
	protected FilePackage()
	{
		
	}

	protected void init(FilePath filePath,
			HashMap<String, ? extends DataDescriptor> forkData,
			HashMap<String, ? extends DataDescriptor> extendedAttributeData,
			ClientFileMetaData metaData)
	{
		if (!filePath.isAbsolute)
			throw new IllegalArgumentException("filePath must be absolute");
		this.filePath = filePath;
		this.forkData = (HashMap<String, ? extends DataDescriptor>) forkData.clone();
		this.extendedAttributeData = (HashMap<String, ? extends DataDescriptor>) extendedAttributeData.clone();
		this.metaData = metaData.clone();
	}

	protected FilePackage(FilePath filePath, ClientFile sourceFile) throws ForkNotFoundException, IOException
	{
		this.filePath = filePath;
		metaData = sourceFile.getMetaData().clone();
		HashMap<String, DataDescriptor>concrete = new HashMap<String, DataDescriptor>();	// Stupid generics
		if (sourceFile.getMetaData().isRegularFile())
		{
			// Don't try to get the data for anything other than a regular file
			String [] forkNames = sourceFile.getForkNames();
			for (String curForkName:forkNames)
			{

				long forkLength = metaData.getForkLength(curForkName);
				FileDataDescriptor curDescriptor = new FileDataDescriptor(sourceFile, curForkName, 0, forkLength);
				concrete.put(curForkName, curDescriptor);
			}
		}
		forkData = (HashMap<String, ? extends DataDescriptor>) concrete;
		extendedAttributeData = new HashMap<String, DataDescriptor>();
	}
	public String [] listForks()
	{
		Set<String>forkNamesSet = forkData.keySet();
		String [] forkNames = new String[forkNamesSet.size()];
		forkNames = forkNamesSet.toArray(forkNames);
		return forkNames;
	}

	public String [] listExtendedAttributes()
	{
		Set<String>extendedAttributeNamesSet = extendedAttributeData.keySet();
		String [] extendedAttributeNames = new String[extendedAttributeNamesSet.size()];
		extendedAttributeNames = extendedAttributeNamesSet.toArray(extendedAttributeNames);
		return extendedAttributeNames;
	}

	public int getNumForks()
	{
		return forkData.size();
	}

	public int getNumExtendedAttributes()
	{
		return extendedAttributeData.size();
	}

	public DataDescriptor getDataDescriptorForFork(String forkName)
	{
		return forkData.get(forkName);
	}

	public DataDescriptor getDataDescriptorForExtendedAttribute(String forkName)
	{
		return extendedAttributeData.get(forkName);
	}

	public ClientFileMetaData getMetaData()
	{
		return metaData;
	}
	
	public FilePath getFilePath()
	{
		return filePath;
	}
	
 @Override
	public String toString()
	{
		return filePath.toString();
	}
}
