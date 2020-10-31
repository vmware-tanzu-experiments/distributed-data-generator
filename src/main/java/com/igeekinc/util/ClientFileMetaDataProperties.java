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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class ClientFileMetaDataProperties
{
    protected HashMap<String, Object>map;
    private static Class<? extends ClientFileMetaDataProperties> propertiesClass;
    private static Constructor<? extends ClientFileMetaDataProperties> propertiesConstructor;
    
    @SuppressWarnings("unchecked")
	public static ClientFileMetaDataProperties getPropertiesForMap(Map<String, Object>map)
    {
    	if (map == null)
    		throw new IllegalArgumentException("Map cannot be null");
    	try
    	{
    		if (propertiesClass == null)
    		{

    			switch(SystemInfo.getSystemInfo().getOSType())
    			{
    			case kMacOSX:
    				propertiesClass = (Class<? extends ClientFileMetaDataProperties>) Class.forName("com.igeekinc.util.macos.macosx.MacOSXFileMetaDataProperties");
    				propertiesConstructor = propertiesClass.getConstructor(new Class<?>[]{Map.class});
    				break;
    			case kLinux:
    				propertiesClass = (Class<? extends ClientFileMetaDataProperties>) Class.forName("com.igeekinc.util.linux.LinuxFileMetaDataProperties");
    				propertiesConstructor = propertiesClass.getConstructor(new Class<?>[]{Map.class});
    				break;
    			case kWindows:
    				propertiesClass = (Class<? extends ClientFileMetaDataProperties>) Class.forName("com.igeekinc.util.windows.WindowsFileMetaDataProperties");
    				propertiesConstructor = propertiesClass.getConstructor(new Class<?>[]{Map.class});
    				break;
				case kAIX:
					throw new InternalError("AIX not supported");
				case kBSD:
					throw new InternalError("AIX not supported");
				case kSolaris:
					throw new InternalError("AIX not supported");
				default:
					break;
    				
    			}

    		}
    		return propertiesConstructor.newInstance(new Object[]{map});
    	} catch (Exception e)
    	{
    		throw new InternalError("Got unexpected exception "+e.toString());
    	}
    }
    
    public ClientFileMetaDataProperties(Map<String, Object>map)
    {
    	if (map == null)
    		throw new IllegalArgumentException("Map cannot be null");
    	this.map = new HashMap<String, Object>();
    	for (Map.Entry<String, Object>curEntry:map.entrySet())
    	{
    		this.map.put(curEntry.getKey(), curEntry.getValue());
    	}
    }
    
    public ClientFileMetaDataProperties(HashMap<String, Object>map)
    {
    	if (map == null)
    		throw new IllegalArgumentException("Map cannot be null");
    	this.map = map;
    }
    
    public ClientFileMetaDataProperties(Properties properties)
    {
    	map = new HashMap<String, Object>();
        if (properties != null)
        {
            for (Object curKey:properties.keySet())
            {
                String curKeyStr = (String)curKey;
                put(curKeyStr, properties.get(curKeyStr));
            }
        }
    }
    
    public ClientFileMetaDataProperties(ClientFileMetaData md)
    {
    	map = new HashMap<String, Object>();
        initFromMetaData(md);
    }

    public static final String kLengthPropertyName = "length";
    public static final String kModifyTimePropertyName = "modifyTime";
    public static final String kOwnerPropertyName = "owner";
    public static final String kIsOwnerReadOnly = "ownerReadOnly";
    
    protected void initFromMetaData(ClientFileMetaData md)
    {
        put (kLengthPropertyName, new Long(md.getFileLength()));
        put(kModifyTimePropertyName, md.getModifyTime());
        put(kOwnerPropertyName, md.getOwner());
        put(kIsOwnerReadOnly, new Boolean(md.isOwnerReadOnly()));
    }
    
    public abstract ClientFileMetaData getMetaData();
    
    public Object put(String key, Object value)
    {
    	return map.put(key, value);
    }
    
    public Object get(String key)
    {
    	return map.get(key);
    }
    
    @SuppressWarnings("unchecked")
	public Map<String, Object> getMap()
    {
    	return (HashMap<String, Object>) map.clone();
    }
}
