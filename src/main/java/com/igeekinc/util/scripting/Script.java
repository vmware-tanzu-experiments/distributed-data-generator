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
 
package com.igeekinc.util.scripting;

import java.io.Serializable;
import java.util.Hashtable;

import com.igeekinc.util.ClientFile;

public class Script implements Serializable
{
    private static final long serialVersionUID = 2274604003736006362L;
    protected String name;
    protected ClientFile scriptFile;
    protected long maxExecutionTime;
    protected boolean abortOnError;
    protected Hashtable<String, Serializable> properties;
    public static final String kArgumentsPropertyName = "arguments";
    public Script(String inName, ClientFile inScriptFile)
    {
        name = inName;
        scriptFile = inScriptFile;
        maxExecutionTime = 0;
        properties = new Hashtable<String, Serializable>();
    }

    public String getName()
    {
        return name;
    }
    
    public ClientFile getScriptFile()
    {
        return scriptFile;
    }
    
    public void setMaxExecutionTime(long newMax)
    {
        maxExecutionTime = newMax;
    }
    
    public long getMaxExecutionTime()
    {
        return maxExecutionTime;
    }

    /**
     * Indicates that execution of scripts should continue in the
     * event that this script fails (return code != 0)
     * @return
     */
    public boolean isAbortOnError()
    {
        return abortOnError;
    }

    public void setAbortOnError(boolean continueOnError)
    {
        this.abortOnError = continueOnError;
    }
    
    public Serializable setProperty(String propertyName, Serializable value)
    {
        return (Serializable)properties.put(propertyName, value);
    }
    
    public Serializable getProperty(String propertyName)
    {
        return (Serializable)properties.get(propertyName);
    }
    
    public String [] getArguments()
    {
    	String [] scriptArguments = (String [])getProperty(Script.kArgumentsPropertyName);
    	return scriptArguments;
    }

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (abortOnError ? 1231 : 1237);
		result = prime * result + (int) (maxExecutionTime ^ (maxExecutionTime >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((scriptFile == null) ? 0 : scriptFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Script other = (Script) obj;
		if (abortOnError != other.abortOnError)
			return false;
		if (maxExecutionTime != other.maxExecutionTime)
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null)
		{
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (scriptFile == null)
		{
			if (other.scriptFile != null)
				return false;
		} else if (!scriptFile.equals(other.scriptFile))
			return false;
		return true;
	}
}
