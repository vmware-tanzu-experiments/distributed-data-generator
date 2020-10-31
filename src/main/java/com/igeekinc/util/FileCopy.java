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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import com.igeekinc.util.datadescriptor.DataDescriptor;
import com.igeekinc.util.formats.splitfile.SplitFileDescriptor;
import com.igeekinc.util.pauseabort.AbortedError;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

public abstract class FileCopy
{
	public static final int kDefaultCopySize = 1024 * 1024;
    protected byte[] copyBuffer;
    protected int preferredCopySize;
    
	public FileCopy()
	{
	    copyBuffer = new byte[kDefaultCopySize];
	    preferredCopySize = kDefaultCopySize;
	}
	
	public FileCopy(int preferredCopySize)
	{
	    copyBuffer = new byte[preferredCopySize];
	    this.preferredCopySize = preferredCopySize;
	}
	
	public abstract void copyFile(
		ClientFile source,
		boolean decompressSource,
		ClientFile destination,
		boolean compressDestination,
		boolean copyMetaData,
		PauserControlleeIF pauser,
		FileCopyProgressIndicatorIF progress)
		throws IOException, AbortedException;

    public abstract void copyFile(
            ClientFile source,
            boolean decompressSource,
            SplitFileDescriptor destination,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    
    public abstract void copyFile(
            SplitFileDescriptor source,
            boolean decompressSource,
            ClientFile destination,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    
    public abstract void copyFile(FilePackage filePackage,
    		boolean decompressSource,
            ClientFile destination,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    
    public abstract void copyFile(FilePackage filePackage,
    		boolean decompressSource,
    		SplitFileDescriptor destination,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    
	public abstract void encryptFile(
		ClientFile source,
		ClientFile destination,
		PublicKey publicKey,
		Key fileKey,
		boolean compressDestination,
		boolean copyMetaData,
		PauserControlleeIF pauser,
		FileCopyProgressIndicatorIF progress)
		throws IOException, AbortedException;
    public abstract void encryptFile(
            ClientFile source,
            SplitFileDescriptor destination,
            PublicKey publicKey,
            Key fileKey,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    public abstract void encryptFile(String fileName, HashMap<String, ? extends DataDescriptor>forkData,
            HashMap<String, ? extends DataDescriptor>extendedAttributeData,
            ClientFileMetaData sourceMetaData,
            ClientFile destination,
            PublicKey publicKey,
            Key fileKey,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    public abstract void encryptFile(String fileName, FilePackage filePackage,
            ClientFile destination,
            PublicKey publicKey,
            Key fileKey,
            boolean compressDestination,
            boolean copyMetaData,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
	public abstract void decryptFile(
		ClientFile source,
		boolean decompressSource,
		PrivateKey unlockKey,
		ClientFile destination,
		boolean copyMetaDate,
		PauserControlleeIF pauser,
		FileCopyProgressIndicatorIF progress)
		throws IOException, AbortedException;
	public abstract void decryptFile(
			FilePackage source,
			boolean decompressSource,
			PrivateKey unlockKey,
			ClientFile destination,
			boolean copyMetaDate,
			PauserControlleeIF pauser,
			FileCopyProgressIndicatorIF progress)
			throws IOException, AbortedException;
    public abstract void decryptFile(
            SplitFileDescriptor source,
            boolean decompressSource,
            PrivateKey unlockKey,
            ClientFile destination,
            boolean copyMetaDate,
            PauserControlleeIF pauser,
            FileCopyProgressIndicatorIF progress)
            throws IOException, AbortedException;
    static final long kNIOCopySize = kDefaultCopySize;
    
    public long copyFork(InputStream sourceStream, OutputStream destStream, PauserControlleeIF pauser, FileCopyProgressIndicatorIF progress) throws IOException, AbortedException
    {
        try
        {
            if (sourceStream.available() > kNIOCopySize && sourceStream instanceof FileInputStream && destStream instanceof FileOutputStream)
            {
                FileChannel sourceChannel = ((FileInputStream)sourceStream).getChannel();
                FileChannel destChannel = ((FileOutputStream)destStream).getChannel();
                long bytesTransferred = 0, totalBytesTransferred = 0, position = 0;
                while ((bytesTransferred = sourceChannel.transferTo(position, preferredCopySize, destChannel)) == preferredCopySize)
                {
                    position += bytesTransferred;
                    totalBytesTransferred += bytesTransferred;
                    if (progress != null)
                        progress.updateProgress(bytesTransferred);
                    if (pauser != null)
                        pauser.checkPauseAndAbort();
                }
                totalBytesTransferred += bytesTransferred;
                return totalBytesTransferred;
            }
            else
            {
                return copyForkViaStream(sourceStream, destStream, pauser, progress);
            }
        }
        catch(AbortedError e)
        {
            throw e.getAbortedException();
        }
    }
    
	public long copyForkViaStream(
		InputStream sourceStream,
		OutputStream destStream,
		PauserControlleeIF pauser,
		FileCopyProgressIndicatorIF progress)
		throws IOException, AbortedException
	{

	    int bytesRead = 0;
	    long totalBytesCopied = 0;
	    try
	    {
	        while ((bytesRead = sourceStream.read(copyBuffer)) > 0)
	        {
	            if (pauser != null)
	                pauser.checkPauseAndAbort();
	            destStream.write(copyBuffer, 0, bytesRead);
	            if (progress != null)
	                progress.updateProgress(bytesRead);
	            totalBytesCopied += bytesRead;
	        }
	    }
	    catch (AbortedError e)
	    {
	        throw e.getAbortedException();
	    }
	    return(totalBytesCopied);
	}
	
	public long copyDataDescriptorToForkViaStream(
	           DataDescriptor sourceDataDescriptor,
	           OutputStream destStream,
	           PauserControlleeIF pauser,
	           FileCopyProgressIndicatorIF progress)
	           throws IOException, AbortedException
	{
	    return copyForkViaStream(sourceDataDescriptor.getInputStream(), destStream, pauser, progress);  // TODO - implement this in a better way
	}
}
