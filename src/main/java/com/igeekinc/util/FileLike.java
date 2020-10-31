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
import java.io.InputStream;
import java.io.OutputStream;

import com.igeekinc.util.exceptions.ForkNotFoundException;

/**
 * Interface for things that are similar to, but not quite a java.io.File
 * Used so that we can have RestoreFileDescriptors and ClientFiles be used
 * in FSTraverser even though they are very different beasts
 */
public interface FileLike
{
	public FileLike getChild(String childName) throws IOException;
	public FileLike getChild(FilePath childPath) throws IOException;
	public String [] list() throws IOException;
	public String [] list(FileLikeFilenameFilter filter) throws IOException;
	public String getAbsolutePath();
	public String getName();
	public long lastModified();
	public boolean exists();
	public boolean isDirectory();
	public boolean isFile();
	public long length();
	public long totalLength();
	public boolean isMountPoint();
	public FilePath getFilePath();
	public FilePath getBackupPartialPath();
	public ClientFileMetaData getMetaData() throws IOException;

	public abstract int getNumForks() throws IOException;
	public abstract String [] getForkNames() throws IOException;

	  
	public abstract InputStream getForkInputStream(String streamName) throws ForkNotFoundException, IOException;
	public abstract OutputStream getForkOutputStream(String streamName) throws ForkNotFoundException, IOException;

	public abstract InputStream getForkInputStream(String streamName, boolean noCache) throws ForkNotFoundException, IOException;
	public abstract OutputStream getForkOutputStream(String streamName, boolean noCache) throws ForkNotFoundException, IOException;
}