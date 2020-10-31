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

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * FilePath is an alternative to a String for representing a path to a File.
 * FilePath encapsulates the routines necessary to parse and work with file paths.
 * FilePath does not directly connect to file object and does not have any connection to meta data, etc.
 * FilePaths are immutable.
 * The conversion from a String to a FilePath involves a certain amount of parsing.  Null components (i.e. "//")
 * will be collapsed.  Up tokens ("..") will be removed if normalization is on (default).
 * 
 * Component 0 of an absolute FilePath is the root name.  On Unix and Unix like systems (Linux, Mac OS X) this will be an empty string
 * On systems such as Windows and Mac OS Classic this will be the name of the volume (e.g. C:)
 *  * @author David Smith-Uchida
 *
 */
public abstract class FilePath implements Serializable
{
    static final long serialVersionUID = 651576691184363925L;
    protected String [] pathComponents;
	protected int offset, count;
	protected boolean isAbsolute, normalized;
	private static Constructor<?> stringPathConstructor, componentsPathConstructor;
	static
	{
	    SystemInfo curSystemInfo = SystemInfo.getSystemInfo();
	    Class<?> [] stringPathArgs = {String.class, Boolean.TYPE};
        Class<?> [] componentsArgs = {String [].class, Boolean.TYPE};
	    Class<? extends FilePath> filePathClass = curSystemInfo.getFilePathClass();      

	    try
	    {
	        stringPathConstructor = filePathClass.getConstructor(stringPathArgs);
	        componentsPathConstructor = filePathClass.getConstructor(componentsArgs);
        } catch (SecurityException e)
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    } catch (NoSuchMethodException e)
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }     //$NON-NLS-1$
	    
	    
	}
	public static FilePath getFilePath(String pathString)
	{
		return getFilePath(pathString, true);
	}
	
	public static FilePath getFilePath(String pathString, boolean normalize)
	{
		Object args [] = new Object[2];
		args[0] = pathString;
		args[1] = Boolean.valueOf(normalize);
		FilePath returnPath;
		try {
			returnPath = (FilePath)stringPathConstructor.newInstance(args);
		} catch (Exception e) {
			throw new InternalError("Unable to create FilePath object exception = "+e.toString()); //$NON-NLS-1$
		}
		return(returnPath);
	}
	
    public static FilePath getFilePath(String [] components, boolean isAbsolute)
    {
        Object args [] = new Object[2];
        args[0] = components;
        args[1] = Boolean.valueOf(isAbsolute);
        FilePath returnPath;
        try {
            returnPath = (FilePath)componentsPathConstructor.newInstance(args);
        } catch (Exception e) {
            throw new InternalError("Unable to create FilePath object"); //$NON-NLS-1$
        }
        return(returnPath);
    }
	public static FilePath getFilePath(FileLike file)
	{
		return getFilePath(file.getAbsolutePath(), true);
	}
	
	public static FilePath getFilePath(File file)
	{
		return getFilePath(file.getAbsolutePath(), true);
	}
	
	public static FilePath getFilePath(ClientFile file)
	{
		return getFilePath(file.getAbsolutePath(), true);
	}
	
	public boolean startsWith(FilePath checkStartPath)
	{
		if (isAbsolute != checkStartPath.isAbsolute)
			return false;
		if (checkStartPath.getNumComponents() >getNumComponents())
			return false;
		if (checkStartPath.getNumComponents() == 0)
			return true;	// Everything starts with nothing!
		for (int curComponentNum = 0; curComponentNum < checkStartPath.getNumComponents(); curComponentNum++)
		{
			if (!getComponent(curComponentNum).equals(checkStartPath.getComponent(curComponentNum)))
				return false;
		}
		return true;
	}
	
	public FilePath getPathRelativeTo(FilePath basePath)
	{
		int returnOffset;
		boolean absolute = false;
		if (startsWith(basePath))
		{
			returnOffset = basePath.getNumComponents();
		}
		else
		{
			returnOffset = 0;
			absolute = isAbsolute;
		}
		// We use getComponents() here rather than the components array because some
		// subclases (Windows) do some cheating with the components array
		FilePath returnPath = getNewFilePath(getComponents(), returnOffset, 
				count - returnOffset, absolute);
		return(returnPath);
	}
	
    /**
     * Works similarly to String.substring except instead of characters our units are
     * path components.  startPos indicates the component num to start at and endPos is 
     * last component num + 1.
     * @param startPos
     * @param endPos
     * @return
     */
    public FilePath subpath(int startPos, int endPos)
    {
        boolean newAbsolute = isAbsolute;
        if (startPos > 0)
            newAbsolute = false;
        return getNewFilePath(pathComponents, offset + startPos, endPos - startPos, newAbsolute);

    }
    
    protected boolean isSeparator(char checkChar, char [] separators)
    {
    	for (char curSeparatorChar:separators)
    		if (checkChar == curSeparatorChar)
    			return true;
    	return false;
    }
    
    protected int separatorIndexOf(String checkString, char [] separators, int startCharPos)
    {
    	for (int curCharNum = startCharPos; curCharNum < checkString.length(); curCharNum++)
    		if (isSeparator(checkString.charAt(curCharNum), separators))
    			return curCharNum;
    	
    	return -1;
    }
	protected void init(String pathString, char [] separators, boolean normalize, boolean inIsAbsolute)
	{
		//String [] newComponents = new String[256];
	    ArrayList<String>newComponents = new ArrayList<String>();
        count = 0;
		
        boolean containsUpTokens = false;
		isAbsolute = inIsAbsolute;
        int startCharPos = 0;
        // Handle anonymous root for Unix systems.  Assumes that who ever called us set isAbsolute correctly.
        if (pathString.length() > 0 && isSeparator(pathString.charAt(0), separators) && (isAbsolute || pathString.length() == 1))
        {
            newComponents.add("");
            startCharPos++;
        }
        int nextSeparatorIndex;
		while((nextSeparatorIndex = separatorIndexOf(pathString, separators, startCharPos)) > -1)
		{
		    String curComponent = pathString.substring(startCharPos, nextSeparatorIndex);
		    startCharPos = nextSeparatorIndex + 1;
		    if (curComponent.length() > 0)
		    {
		        if (curComponent.equals("..")) //$NON-NLS-1$
		        {
		            containsUpTokens = true;
		        }
		        /*
                if (count == newComponents.size())
                {
                    String [] newNewComponents = new String[newComponents.length * 2];
                    System.arraycopy(newComponents, 0, newNewComponents, 0, newComponents.length);
                    newComponents = newNewComponents;
                }
                
		        newComponents[count++] = curComponent;
		        */
		        newComponents.add(curComponent);
		    }
		}
        if (startCharPos < pathString.length())
        {
            String lastComponent = pathString.substring(startCharPos);
            //newComponents[count++] = lastComponent;
            newComponents.add(lastComponent);
        }
        count = newComponents.size();
        
		normalized = true;
        offset = 0;
        pathComponents = new String[newComponents.size()];
        pathComponents = newComponents.toArray(pathComponents);
		if (containsUpTokens)
		{
			if (normalize)	
            {
                pathComponents = normalizeArray(pathComponents, 0, count);
                count = pathComponents.length;
            }
			else
				normalized = false;
		}
		



		
	}
	
	protected void init(String [] inPathComponents, int inOffset, int inCount, boolean inIsAbsolute)
	{
		if (inOffset + inCount > inPathComponents.length)
			throw new IllegalArgumentException("count + offset > components length");
		if (inOffset <0)
			throw new IllegalArgumentException("offset < 0");
		if (inCount < 0)
			throw new IllegalArgumentException("count < 0");
		pathComponents = inPathComponents;
		offset = inOffset;
		count = inCount;
		isAbsolute = inIsAbsolute;
	}

	private ArrayList<String> normalize(String [] normalizeArray)
	{
		ArrayList<String> normalizedList = new ArrayList<String>();
		for(int curComponentNum = 0; curComponentNum <normalizeArray.length; curComponentNum ++)
		{
			String curComponent = normalizeArray[curComponentNum];
			if (curComponent.equals("..")) //$NON-NLS-1$
			{
				if (normalizedList.size() > 0 && curComponentNum > 0)
					normalizedList.remove(normalizedList.size() - 1);
				else
					if (!isAbsolute)
						throw new IllegalArgumentException("Normalization of path "+toString()+" failed - attempted to step out past first component"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				normalizedList.add(normalizeArray[curComponentNum]);
			}
		}
		return(normalizedList);
	}
	
	private ArrayList<String> normalize(ArrayList<String> normalizeList)
	{
		String [] components = new String[normalizeList.size()];
		components = normalizeList.toArray(components);
		return(normalize(components));
	}
	
	private String [] normalizeArray(String [] pathComponents)
	{
		ArrayList<String> normalizeList = normalize(pathComponents);
		String [] returnArray = new String[normalizeList.size()];
		returnArray = normalizeList.toArray(returnArray);
		return(returnArray);
	}
	
	private String [] normalizeArray(String [] pathComponents, int offset, int count)
	{
		String [] arrayToNormalize;
		if (count != pathComponents.length)
		{
			arrayToNormalize = new String[count];
			System.arraycopy(pathComponents, offset, arrayToNormalize, 0, count);
		}
		else
		{
			arrayToNormalize = pathComponents;
		}
		return(normalizeArray(arrayToNormalize));
	}
	
	public FilePath getParent()
	{
        if (isAbsolute && count ==1)
            return this;
        if (count == 0)
		{	
            throw new InternalError("Trying to get parent past beginning of relative path"); //$NON-NLS-1$
		}

		if (normalized)
		{
			return(getNewFilePath(pathComponents, offset, count - 1, isAbsolute));
		}
		else
		{
			String [] componentsToNormalize = new String[count];
			for (int srcNum = offset, destNum = 0; srcNum <offset + count; srcNum++, destNum++)
				componentsToNormalize[destNum] = pathComponents[srcNum];
			String [] normalizedComponents = normalizeArray(componentsToNormalize);
			if (normalizedComponents.length == 0)
			{
				if (isAbsolute)
					return this;
				else
					throw new InternalError("Trying to get parent past beginning of relative path"); //$NON-NLS-1$
			}
			return(getNewFilePath(normalizedComponents, 0, normalizedComponents.length - 1, isAbsolute));	
		}
	}
	
	public FilePath getChild(FilePath childPath)
	{
        if (childPath == null)
            return this;
        if (childPath.isAbsolute)
        {
            if (childPath.getNumComponents() == 1)  // Just the root
                return this;
            childPath = childPath.removeLeadingComponent();
        }
		String [] childComponents = new String[count+childPath.getNumComponents()];
		System.arraycopy(pathComponents, offset, childComponents, 0, count);
		System.arraycopy(childPath.pathComponents, childPath.offset, childComponents, count, childPath.count);
		return(getNewFilePath(childComponents, 0, childComponents.length, isAbsolute));
	}
	
	public FilePath getChild(String childName)
	{
	    if (isPath(childName))
	        throw new IllegalArgumentException(childName+" is a path, not a file name");
		String [] childComponents = new String[count+1];
		System.arraycopy(pathComponents, offset, childComponents, 0, count);
		childComponents[count] = childName;
		return(getNewFilePath(childComponents, 0, childComponents.length, isAbsolute));
	    
	}
	
	public abstract boolean isPath(String checkString);
	
	protected abstract FilePath getNewFilePath(String [] components, int offset, int count, boolean isAbsolute);
	
    public FilePath removeLeadingComponent()
    {
        return removeLeadingComponents(1);
    }
    
    public FilePath removeLeadingComponents(int numberToRemove)
    {
        if (numberToRemove > count)
            throw new IllegalArgumentException("Trying to remove "+numberToRemove+" leading components but total number of components is only "+count);
        return getNewFilePath(pathComponents, offset + numberToRemove, count-numberToRemove, false);
    }
    
    public FilePath removeTrailingComponent()
    {
        return removeTrailingComponents(1);
    }
    
    public FilePath removeTrailingComponents(int numberToRemove)
    {
        if (numberToRemove > count)
            throw new IllegalArgumentException("Trying to remove "+numberToRemove+" trailing components but total number of components is only "+count);
        if (numberToRemove < 0)
        	throw new IllegalArgumentException("Cannot remove a negative number of components");
        return getNewFilePath(pathComponents, offset, count-numberToRemove, isAbsolute);
    }
    
	public int getNumComponents()
	{
		return count;
	}
	
	public String getComponent(int index)
	{
		if (index >= count ||index < 0)
			throw new ArrayIndexOutOfBoundsException("Trying to get component "+index+" out of "+count+" components"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return(pathComponents[offset+index]);
	}
	
	public String getName()
	{
		int numComponents = getNumComponents();
		if (numComponents > 0)
			return(getComponent(numComponents-1));
		return "";
	}
	
	public String [] getComponents()
	{
		String [] returnArray = new String[count];
		System.arraycopy(pathComponents, offset, returnArray, 0, count);
		return(returnArray);
	}
	
	/*
	 * FilePaths are equal if all of their components match.  If the paths are not normalized (i.e. they contain "..")
	 * they must be exact matches.  Normalize paths before checking if you want to check the normalized path.
	 */
	public boolean equals(Object checkObject)
	{
		if (!(checkObject instanceof FilePath))
			return false;
		if (checkObject == this)
			return true;
		FilePath checkPath = (FilePath)checkObject;
		if (count == checkPath.count)
		{
			for (int curOffset = 0; curOffset <count; curOffset++)
			{
				if (!pathComponents[offset+curOffset].equals(checkPath.pathComponents[checkPath.offset+curOffset]))
					return false;
			}
			// All relevant components match
			return true;
		}
		return false;
	}
	
	public String getPath()
	{
		return(toString());
	}
	
    public FilePath getNormalizedPath()
    {
        if (normalized)
            return this;
        String [] normalizedComponents = normalizeArray(pathComponents, offset, count);
        return getNewFilePath(normalizedComponents, 0, normalizedComponents.length, isAbsolute);
    }
    
	public abstract String toString();
	protected StringBuffer toStringBuf(String separator)
	{
		StringBuffer returnBuf= new StringBuffer();
		if (count > 0)
		{	
			for (int curComponentNum = 0; curComponentNum < count - 1; curComponentNum++)
			{
				returnBuf.append(pathComponents[offset + curComponentNum]);
				returnBuf.append(separator);
			}
			returnBuf.append(pathComponents[(count-1)+offset]);
		}
		return(returnBuf);
	}
	
	public boolean isAbsolute()
	{
		return isAbsolute;
	}
    
    public boolean isNormalized()
    {
        return normalized;
    }

    public String getSuffix()
    {
        String name = getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0)
            return "";
        return(name.substring(dotIndex + 1));
    }
    
    /**
     * Convenience function to add a suffix to the last component of the path
     * 
     * @param addSuffix
     * @return
     */
    public FilePath addSuffix(String addSuffix)
    {
        String [] newComponents = new String[count];
        System.arraycopy(pathComponents, offset, newComponents, 0, count );
        newComponents[count - 1] = newComponents[count - 1]+addSuffix;
        return(getNewFilePath(newComponents, 0, count, isAbsolute));
    }
    
    /**
     * Adds an "extension" to the filepath.  Same as addSuffix except it automatically adds a "." between the extension
     * and the existing name
     * @param extension
     * @return
     */
    public FilePath addExtension(String extension)
    {
        return(addSuffix("."+extension));
    }
    public FilePath removeSuffix(int numChars)
    {
        String [] newComponents = new String[count];
        System.arraycopy(pathComponents, offset, newComponents, 0, count);
        
        String name = newComponents[count - 1];
        newComponents[count - 1] = name.substring(0, name.length() - numChars);

        return(getNewFilePath(newComponents, 0, count, isAbsolute));
    }
    
    public FilePath removeExtension()
    {
        String name = pathComponents[count - 1];
        int dotIndex = name.indexOf('.');
        if (dotIndex >= 0)
            return removeSuffix(name.length() - dotIndex);
        else
            return this;
    }
    
    /**
     * hashCode for a FilePath is calculated as the XOR of all the hashcodes
     * of its component strings
     */
    public int hashCode()
    {
        int returnCode = 0;
        for (int curComponentNum = offset; curComponentNum < count; curComponentNum++)
        {
            returnCode ^= pathComponents[curComponentNum].hashCode();
        }
        return returnCode;
    }

    public abstract FilePath makeAbsolute();

	public boolean isRoot()
	{
		if (isAbsolute && getNumComponents() <= 1)
			return true;
		return false;
	}
}
