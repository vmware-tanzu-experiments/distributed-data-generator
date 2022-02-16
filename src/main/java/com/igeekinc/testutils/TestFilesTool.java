/*
 * Copyright 2002-2014 iGeek, Inc.
 * All Rights Reserved
 * @Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.@
 */
package com.igeekinc.testutils;

import com.igeekinc.util.BitTwiddle;
import com.igeekinc.util.ClientFile;
import com.igeekinc.util.ClientFileMetaData;
import com.igeekinc.util.FileCopyProgressIndicator;
import com.igeekinc.util.SHA1HashID;
import com.igeekinc.util.SystemInfo;
import com.igeekinc.util.logging.ErrorLogMessage;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauseAbort;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class TestFilesTool 
{	
	static File createDir(File root, int curDirNum)
	{
		File newDir = new File(root, "dir"+curDirNum);
		newDir.mkdir();
		return newDir;
	}
	
	static void createFile(File root, int curFileNum)
	throws IOException
	{
		File newFile = new File(root, "file"+curFileNum);
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(newFile));
		bufWriter.write("file"+curFileNum+"\n");
		bufWriter.close();
	}
	
	public static int makeTestHierarchy(File root, int levels, int dirsPerLevel, int filesPerLevel)
	throws IOException
	{
		int filesCreated = 0;
		if (levels > 1)
		{	
			for (int curDirNum = 0; curDirNum < dirsPerLevel; curDirNum++)
			{	
				File curDir = createDir(root, curDirNum);
				filesCreated++;
				filesCreated += makeTestHierarchy(curDir, levels - 1, dirsPerLevel, filesPerLevel);
			}
		}
		for (int curFileNum = 0; curFileNum <filesPerLevel; curFileNum++)
		{
			createFile(root, curFileNum);
			filesCreated++;
		}
		return(filesCreated);
	}
    static final int kBufSize = 1024*1024;    
    public static SHA1HashID createTestFile(File fileToCreate, long size) throws IOException
    {

        FileOutputStream outputStream = new FileOutputStream(fileToCreate);
        return writeTestDataToOutputStream(outputStream, size);
    }

    public static SHA1HashID writeTestDataToOutputStream(OutputStream outputStream, long size) throws IOException
    {
        SHA1HashID returnID = new SHA1HashID();
        byte [] buffer = new byte[kBufSize];
        long bytesRemaining = size;
        Random randomBytes = new Random(size);
        while(bytesRemaining > 0)
        {
            if (bytesRemaining < kBufSize)
                buffer = new byte[(int)bytesRemaining];
            randomBytes.nextBytes(buffer);
            returnID.update(buffer);
            outputStream.write(buffer);
            bytesRemaining -= buffer.length;
        }
        returnID.finalizeHash();
        return returnID;
    }
    
    public static boolean verifyFile(File fileToCheck, SHA1HashID checkHash, long checkSize) throws IOException
    {
        if (fileToCheck.length() != checkSize)
            return false;
 
        FileInputStream inputStream = new FileInputStream(fileToCheck);
        return verifyInputStream(inputStream, checkSize, checkHash);
    }

    public static boolean verifyInputStream(InputStream inputStream, long checkSize, SHA1HashID checkHash) throws IOException
    {
        byte [] buffer = new byte[kBufSize];
        long totalBytesRead = 0;
        int bytesRead;
        SHA1HashID calcCheckHash = new SHA1HashID();
        while ((bytesRead = inputStream.read(buffer)) > 0)
        {
            calcCheckHash.update(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        
        calcCheckHash.finalizeHash();
        if (totalBytesRead == checkSize && checkHash.equals(calcCheckHash))
            return true;
        
        return false;
    }
    
    public static void deleteTree(
            File directoryToDelete)
            throws IOException
        {

            if (directoryToDelete.delete())
                return;
            File[] filesToDelete = directoryToDelete.listFiles();
            if (filesToDelete != null)
            {
                for (int curFileNum = 0;
                    curFileNum < filesToDelete.length;
                    curFileNum++)
                {
                    File curFileToDelete = filesToDelete[curFileNum];
                    if (curFileToDelete.isDirectory())
                        deleteTree(curFileToDelete);
                    else
                    {
                        curFileToDelete.delete();
                    }
                }
            }
            directoryToDelete.delete();

        }
    
    public static void copyFile(File source, File destination) throws IOException
    {
        PauseAbort pauser = new PauseAbort(LogManager.getLogger(TestFilesTool.class));
        ClientFile sourceCF = SystemInfo.getSystemInfo().getClientFileForFile(source);
        ClientFile destinationCF = SystemInfo.getSystemInfo().getClientFileForFile(destination);
        try
        {
            SystemInfo.getSystemInfo().getFileCopy().copyFile(sourceCF, false, destinationCF, false, true, pauser, null);
        } catch (AbortedException e)
        {
            LogManager.getLogger(TestFilesTool.class).error(new ErrorLogMessage("Caught exception"), e);
        }
    }
    
    /**
     * Copies all files from source to destination, recursively
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copyTree(File source, File destination) throws IOException
    {
        PauseAbort pauser = new PauseAbort(LogManager.getLogger(TestFilesTool.class));
        ClientFile sourceCF = SystemInfo.getSystemInfo().getClientFileForFile(source);
        ClientFile destinationCF = SystemInfo.getSystemInfo().getClientFileForFile(destination);
        ClientFile[] filesToCopy = sourceCF.listClientFiles();
        if (filesToCopy != null)
        {
            for (int curFileNum = 0;
                curFileNum < filesToCopy.length;
                curFileNum++)
            {
                ClientFile curFileToCopy = filesToCopy[curFileNum];
                ClientFile curFileDestCF = destinationCF.getChild(curFileToCopy.getName());
                if (curFileToCopy.isDirectory())
                {
                    curFileDestCF.mkdir();
                    copyTree(curFileToCopy, curFileDestCF);
                }
                else
                {
                    
                    try
                    {
                        SystemInfo.getSystemInfo().getFileCopy().copyFile(curFileToCopy, false, curFileDestCF, false, true, pauser, new FileCopyProgressIndicator());
                    } catch (AbortedException e)
                    {
                        LogManager.getLogger(TestFilesTool.class).error(new ErrorLogMessage("Caught exception"), e);
                    }
                }
            }
        }
        ClientFileMetaData sourceMD = sourceCF.getMetaData();
        destinationCF.setMetaData(sourceMD);
    }
    
    public void testBlockFile(File fileToTest, long length, long timestamp)
    {
    	
    }
    
    public static final int kBlockOffsetOffset = 0;
    public static final int kBlockBlockNumOffset = kBlockOffsetOffset + 8;
    public static final int kPassNumOffset = kBlockBlockNumOffset + 8;
    public static final int kBlockHeaderBytes = kPassNumOffset + 4;
    
    public static void writeBlock(RandomAccessFile file, long blockNum, int blockSize, int passNum) throws IOException
    {
    	long offset = blockNum * blockSize;
    	ByteBuffer curBlockBytes = generateTestPatternByteBuffer(blockNum, blockSize,
				passNum, offset);
    	curBlockBytes.rewind();
    	FileChannel writeChannel = file.getChannel();
    	writeChannel.position(offset);
    	writeChannel.write(curBlockBytes);
    }

	public static byte[] generateTestPatternBlock(long blockNum, int blockSize,
			int passNum, long offset)
	{
		ByteBuffer byteBuffer = generateTestPatternByteBuffer(blockNum, blockSize, passNum, offset);
		byte [] returnBytes = new byte[byteBuffer.limit()];
		byteBuffer.position(0);
		byteBuffer.get(returnBytes);
		return returnBytes;
		/*
		byte [] curBlockBytes = new byte[blockSize];
    	BitTwiddle.longToJavaByteArray(offset, curBlockBytes, kBlockOffsetOffset);
    	BitTwiddle.longToJavaByteArray(blockNum, curBlockBytes, kBlockBlockNumOffset);
    	BitTwiddle.intToJavaByteArray(passNum, curBlockBytes, kPassNumOffset);
    	int bytesToFill = curBlockBytes.length - kBlockHeaderBytes - SHA1HashID.kSHA1ByteLength;
    	String fillPattern="iGeekTest-"+passNum+"-"+blockNum;
    	byte [] fillBytes = fillPattern.getBytes(Charset.forName("UTF-8"));
    	for (int fillByteOffset = kBlockHeaderBytes; fillByteOffset < bytesToFill; fillByteOffset += fillBytes.length)
    	{
    		int curBytesToFill = fillBytes.length;
    		if (curBytesToFill > bytesToFill - fillByteOffset)
    			curBytesToFill = bytesToFill - fillByteOffset;
    		System.arraycopy(fillBytes, 0, curBlockBytes, fillByteOffset, curBytesToFill);
    	}
    	SHA1HashID hashID = new SHA1HashID();
    	hashID.update(curBlockBytes, 0, bytesToFill + kBlockHeaderBytes);
    	hashID.finalizeHash();
    	hashID.getBytes(curBlockBytes, bytesToFill + kBlockHeaderBytes);
		return curBlockBytes;*/
	}
    
	public static ByteBuffer generateTestPatternByteBuffer(long blockNum, int blockSize,
			int passNum, long offset)
	{
		ByteBuffer returnBuffer = ByteBuffer.allocateDirect(blockSize);
		returnBuffer.putLong(offset);
		returnBuffer.putLong(blockNum);
		returnBuffer.putInt(passNum);
    	int bytesToFill = returnBuffer.limit() - kBlockHeaderBytes - SHA1HashID.kSHA1ByteLength;
    	String fillPattern="iGeekTest-"+passNum+"-"+blockNum;
    	byte [] fillBytes = fillPattern.getBytes(Charset.forName("UTF-8"));
    	for (int fillByteOffset = kBlockHeaderBytes; fillByteOffset < bytesToFill; fillByteOffset += fillBytes.length)
    	{
    		int curBytesToFill = fillBytes.length;
    		if (curBytesToFill > bytesToFill - fillByteOffset)
    			curBytesToFill = bytesToFill - fillByteOffset;
    		returnBuffer.put(fillBytes, 0, curBytesToFill);
    	}
    	SHA1HashID hashID = new SHA1HashID();
    	returnBuffer.position(0);
    	returnBuffer.limit(bytesToFill + kBlockHeaderBytes);
    	hashID.update(returnBuffer);
    	hashID.finalizeHash();
    	byte [] hashBytes = hashID.getBytes();
    	returnBuffer.limit(returnBuffer.capacity());
    	returnBuffer.position(bytesToFill + kBlockHeaderBytes);
    	returnBuffer.put(hashBytes);
		return returnBuffer;
	}
    public static boolean verifyBlock(RandomAccessFile file, long blockNum, int blockSize, int passNum) throws IOException
    {
    	long offset = blockNum * blockSize;
    	byte [] curBlockBytes = new byte[blockSize];
    	file.seek(offset);
    	file.read(curBlockBytes);
    	
    	return verifyTestPatternBlock(blockNum, passNum, offset, curBlockBytes);
    }

	
	public static boolean verifyTestPatternBlock(long blockNum, int passNum,
			long offset, byte[] curBlockBytes)
	{
		long checkOffset, checkBlockNum;
    	int checkPassNum;
    	checkOffset = BitTwiddle.javaByteArrayToLong(curBlockBytes, kBlockOffsetOffset);
    	checkBlockNum = BitTwiddle.javaByteArrayToLong(curBlockBytes, kBlockBlockNumOffset);
    	checkPassNum = BitTwiddle.javaByteArrayToInt(curBlockBytes, kPassNumOffset);
    	
		Logger logger = LogManager.getLogger(TestFilesTool.class);
		
    	boolean verified = true;
    	if (checkOffset != offset)
    	{
			logger.error("Verify of block "+blockNum+" failed, offset = "+offset+", checkOffset = "+checkOffset);
    		verified = false;
    	}
    	if (checkBlockNum != blockNum)
    	{
			logger.error("Verify of block "+blockNum+" failed, blockNum = "+blockNum+", checkBlockNum = "+checkBlockNum);
    		verified = false;
    	}
    	if (checkPassNum != passNum)
    	{
			logger.error("Verify of block "+blockNum+" failed, passNum = "+passNum+", checkPassNum = "+checkPassNum);
    		verified = false;
    	}
    	
    	int bytesToVerifyHash = curBlockBytes.length - kBlockHeaderBytes - SHA1HashID.kSHA1ByteLength;
    	
    	SHA1HashID verifyHash = new SHA1HashID();
    	verifyHash.update(curBlockBytes, 0, bytesToVerifyHash + kBlockHeaderBytes);
    	verifyHash.finalizeHash();
    	
    	SHA1HashID blockHash = new SHA1HashID(curBlockBytes, bytesToVerifyHash + kBlockHeaderBytes);
    	if (!blockHash.equals(verifyHash))
    	{
			logger.error("Verify of block "+blockNum+", blockHash = "+blockHash+", blockHash = "+blockHash);
    		verified = false;
    	}
    	
    	return verified;
	}
    
    public static void createBlockFileSequential(File blockFile, long length, int blockSize, int passNum) throws IOException
    {
    	if (length % blockSize != 0)
    		throw new IllegalArgumentException("Length ("+length+") is not a multiple of "+blockSize);
    	long numBlocks = length/blockSize;
    	RandomAccessFile writeFile = new RandomAccessFile(blockFile, "rws");
 
    	for (long curBlockNum = 0; curBlockNum < numBlocks; curBlockNum++)
    	{
    		writeBlock(writeFile, curBlockNum, blockSize, passNum);
    	}
    	writeFile.close();
    }
    
    public static boolean verifyBlockFileSequential(File blockFile, long length, int blockSize, int passNum) throws IOException
    {
    	// We let the caller pass in the file length so we can verify things like block devices
    	if (length % blockSize != 0)
    		throw new IllegalArgumentException("Length ("+length+") is not a multiple of "+blockSize);
    	long numBlocks = length/blockSize;
    	RandomAccessFile readFile = new RandomAccessFile(blockFile, "r");
    	boolean passed = true;
    	for (long curBlockNum = 0; curBlockNum < numBlocks; curBlockNum++)
    	{
    		if (!verifyBlock(readFile, curBlockNum, blockSize, passNum))
    			passed = false;
    	}
    	return passed;
    }

	public static void createBlockFileRandomly(File blockFile, long length, int blockSize, int passNum) throws IOException
	{
    	if (length % blockSize != 0)
    		throw new IllegalArgumentException("Length ("+length+") is not a multiple of "+blockSize);
    	long numBlocks = length/blockSize;
    	RandomAccessFile writeFile = new RandomAccessFile(blockFile, "rws");
    	ArrayList<Long>blocksToWrite = new ArrayList<Long>();
    	for (long curBlockNum = 0; curBlockNum < numBlocks; curBlockNum++)
    	{
    		blocksToWrite.add(curBlockNum);
    	}
    	Random blockToWriteRandom = new Random(passNum);
    	for (long curBlockNum = 0; curBlockNum < numBlocks; curBlockNum++)
    	{
    		int blockListIndex = (blockToWriteRandom.nextInt() & Integer.MAX_VALUE) % blocksToWrite.size();
    		long blockToWriteNum = blocksToWrite.remove(blockListIndex);
    		writeBlock(writeFile, blockToWriteNum, blockSize, passNum);
    	}
	}
}
