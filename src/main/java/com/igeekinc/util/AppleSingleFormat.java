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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.igeekinc.util.exceptions.ForkNotFoundException;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;

public class AppleSingleFormat implements DataStreamer
{
	public static final int kAppleSingleMagicNumber = 0x00051600;
    public static final int kAppleDoubleMagicNumber = 0x00051607;
    static public final int kMagicNumberOffset = 0;
	static public final int kVersionNumberOffset = kMagicNumberOffset + 4;
	static public final int kFillerOffset = kVersionNumberOffset + 4;
	static public final int kNumberOfEntriesOffset = kFillerOffset + 16;
	static public final int kEntriesOffset = kNumberOfEntriesOffset + 2;
	static public final int kHeaderSize = kEntriesOffset;
	
	static public final int kEntrySize = 12; // 3 4 byte ints
	ArrayList<AppleSingleEntry> entries = new ArrayList<AppleSingleEntry>();
	File storageFile=null;
	int offset;
	InputStream stream=null;
	
	public AppleSingleFormat(File inStorageFile)
	throws IOException
	{
		storageFile = inStorageFile;
		init();
	}
	
	public AppleSingleFormat(InputStream inStream)
	throws IOException
	{
		stream = inStream;
		offset = readEntries(stream);
	}
	
	public AppleSingleFormat()
	{
		// We can only use writeEntries(OutputStream ...)
	}
	public void streamData(OutputStream dataOutputStream, OutputStream rsrcOutputStream, PauserControlleeIF pauser,
	        FileCopyProgressIndicatorIF progress) 
		throws IOException, AbortedException
	{
		if (stream == null)
		{
			if (storageFile == null)
				throw new InternalError("Can't call streamData twice on a AppleSingleFormat constructed with a stream");
			stream = new BufferedInputStream(new FileInputStream(storageFile));
			stream.skip(offset);
		}
		FileCopy copier = SystemInfo.getSystemInfo().getFileCopy();
		// Now, we need to sort by offset and read or copy all of the data
		AppleSingleEntry [] sortEntries = new AppleSingleEntry[entries.size()];
		sortEntries = entries.toArray(sortEntries);
		java.util.Arrays.sort(sortEntries);
		long curOffset = offset;
		for (int curEntryNum = 0; curEntryNum < sortEntries.length; curEntryNum++)
		{
			AppleSingleEntry curEntry = sortEntries[curEntryNum];
			if (curOffset > curEntry.offset)
				throw new IOException("file is corrupted - offsets go negative");
			if (curOffset < curEntry.offset)
			{
				stream.skip(curEntry.offset - curOffset);
				curOffset = curEntry.offset;
			}
			ConstrainedInputStream readStream = new ConstrainedInputStream(stream, 0, curEntry.length);

			switch(curEntry.entryID)
			{
				case AppleSingleEntry.kDataForkID:
					copier.copyFork(readStream, dataOutputStream, pauser, progress);
					curEntry.dataSource = null; // No going back
					break;
				case AppleSingleEntry.kResourceForkID:
					copier.copyFork(readStream, rsrcOutputStream, pauser, progress);
					curEntry.dataSource = null;
					break;
				default:
					byte [] entryDataBuf = new byte[(int)curEntry.length];
					if (curEntry.length != readStream.read(entryDataBuf))
						throw new IOException("Premature end of file");
					ByteArrayDataSource entryDataSource = new ByteArrayDataSource(entryDataBuf);
					curEntry.setDataSource(entryDataSource);
					break;
			}
			curOffset += curEntry.length;
		}
		stream.close();
		stream = null;
	}
		
	void init()
	throws IOException
	{
		if (storageFile.exists() && storageFile.length() > 0)
		{
			FileInputStream fileIn = new FileInputStream(storageFile);
			offset = readEntries(fileIn);
			fileIn.close();
		}
	}
	int readEntries(InputStream inStream)
	throws IOException
	{
		int totalBytesRead = 0;
		byte [] header = new byte[kHeaderSize];
		int bytesRead = inStream.read(header);
		if (bytesRead != kHeaderSize)
		{
			throw new IOException("Couldn't read complete header");
		}
		totalBytesRead += bytesRead;
		int magicNumber = BitTwiddle.byteArrayToInt(header, kMagicNumberOffset, BitTwiddle.kBigEndian);
		if (magicNumber != 0x00051600 && magicNumber != 0x00051607)
			throw new IOException("Magic number does not match");
		int versionNumber = BitTwiddle.byteArrayToInt(header, kVersionNumberOffset, BitTwiddle.kBigEndian);
		if (versionNumber != 0x0020000)
			throw new IOException("Unsupported version");
		int numberOfEntries = ((int)BitTwiddle.byteArrayToShort(header, kNumberOfEntriesOffset, BitTwiddle.kBigEndian)) & 0x0000ffff; // Strip off high bits - short is signed, we don't want to be negative
		
		byte entriesBuf[] = new byte[numberOfEntries * kEntrySize];
		
		bytesRead = inStream.read(entriesBuf);
		if (bytesRead != entriesBuf.length)
		{
			throw new IOException("Couldn't read all entries");
		}
		totalBytesRead += bytesRead;
		for (int curEntryNum = 0; curEntryNum < numberOfEntries; curEntryNum++)
		{
			int curEntryOffset = curEntryNum * 12;
			int curEntryID = BitTwiddle.byteArrayToInt(entriesBuf, curEntryOffset, BitTwiddle.kBigEndian);
			if (curEntryID == 0)
				throw new IOException("EntryID == 0 at offset " + curEntryOffset);
			long curEntryDataOffset = ((long)BitTwiddle.byteArrayToInt(entriesBuf, curEntryOffset + 4, BitTwiddle.kBigEndian)) & 0xffffffffL;
			long curEntryDataLength = ((long)BitTwiddle.byteArrayToInt(entriesBuf, curEntryOffset + 8, BitTwiddle.kBigEndian)) & 0xffffffffL;
			ConstrainedFileDataSource entryDataSource = new ConstrainedFileDataSource(storageFile, curEntryDataOffset, curEntryDataLength);
			AppleSingleEntry curEntry = new AppleSingleEntry(curEntryID, curEntryDataLength, entryDataSource);		
			curEntry.setOffset(curEntryDataOffset);
			entries.add(curEntry);
		}
		return(totalBytesRead);
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.ClientFile#getNumForks()
	 */
	public synchronized int getNumEntries()
	{
		return entries.size();
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.ClientFile#getForkNames()
	 */
	public synchronized int [] getEntryIDs()
	{
		int [] returnIDs = new int[entries.size()];
		for (int curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			AppleSingleEntry curEntry = entries.get(curEntryNum);
			returnIDs[curEntryNum] = curEntry.getEntryID();
		}
		return(returnIDs);
	}
	public long getEntryOffset(int entryID)
	{
		AppleSingleEntry curEntryDesc = null;
		for (int curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			AppleSingleEntry checkEntry = entries.get(curEntryNum);
			if (checkEntry.entryID == entryID)
			{
				curEntryDesc = checkEntry;
				break;
			}
		}
		if (curEntryDesc != null)
			return(curEntryDesc.offset);
		
		return(-1);
	}
	
	public long getEntryLength(int entryID)
	{
		AppleSingleEntry curEntryDesc = null;
		for (int curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			AppleSingleEntry checkEntry = entries.get(curEntryNum);
			if (checkEntry.entryID == entryID)
			{
				curEntryDesc = checkEntry;
				break;
			}
		}
		if (curEntryDesc != null)
			return(curEntryDesc.length);
		
		return(-1);
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.ClientFile#getForkInputStream(java.lang.String)
	 */
	public InputStream getEntryInputStream(int entryID) throws ForkNotFoundException
	{
		AppleSingleEntry curEntryDesc = null;
		
		for (int curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			AppleSingleEntry checkEntry = entries.get(curEntryNum);
			if (checkEntry.entryID == entryID)
			{
				curEntryDesc = checkEntry;
				break;
			}
		}
		if (curEntryDesc == null)
			throw new ForkNotFoundException("Fork for entry ID "+Integer.toHexString(entryID)+" not found");
		if (curEntryDesc.dataSource == null)
			throw new InternalError("Cannot use getEntryInputStream if no file was specified at construction time");
		
		// TODO Auto-generated method stub
		try
		{
			return curEntryDesc.getDataSource().getInputStream();
		}
		catch (IOException e1)
		{
			throw new ForkNotFoundException("Fork for entry ID "+Integer.toHexString(entryID)+" not found");
		}
	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.ClientFile#getForkOutputStream(java.lang.String)
	 */
	public synchronized void createEntry(int entryID, int size, DataSource data)
	throws IOException
	{
		AppleSingleEntry curEntryDesc = null;
		
		for (AppleSingleEntry checkEntry:entries)
		{
			if (checkEntry.entryID == entryID)
			{
				curEntryDesc = checkEntry;
				break;
			}
		}
		if (curEntryDesc != null)
			throw new IOException("Writing to existing forks not allowed yet");
		curEntryDesc = new AppleSingleEntry(entryID, size, data);
		entries.add(curEntryDesc);
	}
	
	public synchronized void writeEntries(PauserControlleeIF pauser, FileCopyProgressIndicatorIF progress)
	throws IOException, AbortedException
	{
		if (storageFile == null)
			throw new IOException("storageFile was not specified");
		File outFile;
		
		if (storageFile.exists())
			outFile = new File(storageFile.getAbsolutePath()+".t");
		else
			outFile = storageFile;
		FileOutputStream outStream = new FileOutputStream(outFile);
		writeEntries(outStream, pauser, progress);
		outStream.close();
		if (outFile != storageFile)
		{
			if (!storageFile.delete())
			{
				outFile.delete();
				throw new IOException("Could not replace Apple Double file "+outFile.getAbsolutePath());
			}

			if (!outFile.renameTo(storageFile))
			{
				outFile.delete();
				throw new IOException("Could not rename Apple Double temp file "+outFile+" to "+storageFile.getAbsolutePath());
			}
		}
	}
	
	public synchronized void writeEntries(OutputStream writeOut, PauserControlleeIF pauser, FileCopyProgressIndicatorIF progress)
	throws IOException, AbortedException
	{
		if (entries.size() == 0)
			return;
		if (pauser != null)
			pauser.checkPauseAndAbort();
		BufferedOutputStream bufWriteOut = new BufferedOutputStream(writeOut);
		byte [] header = new byte[kHeaderSize];
		BitTwiddle.intToByteArray(getMagicNumber(), header, kMagicNumberOffset, BitTwiddle.kBigEndian);
		BitTwiddle.intToByteArray(0x0020000, header, kVersionNumberOffset, BitTwiddle.kBigEndian);
		BitTwiddle.shortToByteArray((short)entries.size(), header, kNumberOfEntriesOffset, BitTwiddle.kBigEndian);
		bufWriteOut.write(header);
		
		int startDataOffset, curDataOffset;
		startDataOffset = kEntriesOffset + (entries.size() * kEntrySize);
		curDataOffset = startDataOffset;
		int curEntryNum;
		AppleSingleEntry curEntry;
		// Rearrange entries to ensure that resource fork is always last (otherwise certain POS
		// libraries crash)
		for (curEntryNum = 0; curEntryNum < entries.size()-1; curEntryNum++)
		{
			curEntry = entries.get(curEntryNum);
			if (curEntry.entryID == AppleSingleEntry.kResourceForkID)
			{
				entries.remove(curEntryNum);
				entries.add(curEntry);
				break;
			}
		}
		byte [] entriesBuf = new byte[entries.size() * kEntrySize];
		for (curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			curEntry = entries.get(curEntryNum);
			curEntry.setOffset(curDataOffset);
			curDataOffset += curEntry.length;
			int curEntryOffset = curEntryNum * kEntrySize;
			BitTwiddle.intToByteArray(curEntry.entryID, entriesBuf, curEntryOffset, BitTwiddle.kBigEndian);
			int intOffset = (int)curEntry.offset;
			BitTwiddle.intToByteArray(intOffset, entriesBuf, curEntryOffset + 4, BitTwiddle.kBigEndian);
			int intLength = (int)curEntry.length;
			BitTwiddle.intToByteArray(intLength, entriesBuf, curEntryOffset + 8, BitTwiddle.kBigEndian);
		}
		bufWriteOut.write(entriesBuf);
		FileCopy fileCopier = SystemInfo.getSystemInfo().getFileCopy();
		
		for (curEntryNum = 0; curEntryNum < entries.size(); curEntryNum++)
		{
			curEntry = entries.get(curEntryNum);
			if (curEntry.getLength() > 0)
			{
			    InputStream dataInStream = curEntry.getDataSource().getInputStream();
			    long bytesCopied = fileCopier.copyFork(dataInStream, bufWriteOut, pauser, progress);
			    dataInStream.close();
			    if ((int) bytesCopied != curEntry.length)
			        throw new IOException("bytes copied "+bytesCopied+" does not match expected segment length "+curEntry.length);
			}
		}
		bufWriteOut.flush();
	}

    protected int getMagicNumber()
    {
        return kAppleSingleMagicNumber;
    }
}
