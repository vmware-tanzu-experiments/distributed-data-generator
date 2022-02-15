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

import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



class SampleBucket
{
    long firstSampleTime;
    long fileOverheadTimeTotal;
    long bytesPerSecondTotal;
    long overheadSamplesInBucket, speedSamplesInBucket;
    long bytesCopiedInBucket;
    
    SampleBucket(long firstSampleTime)
    {
        this.firstSampleTime = firstSampleTime;
        fileOverheadTimeTotal = 0;
        bytesPerSecondTotal = 0;
        overheadSamplesInBucket = 0;
        speedSamplesInBucket = 0;
    }
    
    public long getFirstSampleTime()
    {
        return firstSampleTime;
    }
    
    public void addFileOverheadSample(long fileOverheadMS, long fileSize)
    {
        fileOverheadTimeTotal += fileOverheadMS;
        overheadSamplesInBucket ++;
        bytesCopiedInBucket += fileSize;
    }
    
    public void addSpeedSample(long bytesPerSecond)
    {
        bytesPerSecondTotal += bytesPerSecond;
        speedSamplesInBucket++;
    }
    
    public long getFileOverheadAverage()
    {
        if (overheadSamplesInBucket == 0)
            return -1;
        return fileOverheadTimeTotal/overheadSamplesInBucket;
    }
    
    public long getBytesPerSecondAverage()
    {
        if (speedSamplesInBucket == 0)
            return -1;
        return bytesPerSecondTotal/speedSamplesInBucket;
    }
    
}
public class FileCopyCompletionEstimator
{
    long totalBytesToBeCopied, totalFilesToBeCopied;
    long totalBytesRemaining, totalFilesRemaining;
    long estimatedCompletionTime;
    
    long currentFileStartTime, currentFileCopyStartTime, currentFileBytesToCopy, currentFileCopyStopTime;
    
    long currentAverageBytesPerMS, currentAverageFileOverhead;
    
    long lastCopyUpdateTime;
    
    Logger logger;
    
    private boolean generateLogFile = true;
    private PrintWriter logFileOutput;
    
    public static final int kMillisecondsPerBucket = 1000;
    public static final int kMaxSampleBuckets = 600; // We'll keep a running average across the last 10 minutes
    public static final int kMinSampleBuckets = 5;  // Don't make an estimate until we have at least 5 sample buckets
    
    protected LinkedList<SampleBucket>samples;
    public FileCopyCompletionEstimator()
    {
        totalBytesToBeCopied = -1;
        totalFilesToBeCopied = -1;
        currentFileStartTime = -1;
        currentFileCopyStartTime = -1;
        currentAverageBytesPerMS = -1;
        currentAverageFileOverhead = -1;
        lastCopyUpdateTime = -1;
        estimatedCompletionTime = -1;
        
        logger=LogManager.getLogger(getClass());
        samples = new LinkedList<SampleBucket>();
        if (generateLogFile)
        {
            try
            {
                logFileOutput = new PrintWriter(new FileOutputStream(new File("/tmp/fcce.log")), true);
            } catch (FileNotFoundException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
            }
        }
    }

    public long getTotalBytesToBeCopied()
    {
        return totalBytesToBeCopied;
    }

    public void setTotalBytesToBeCopied(long totalBytesToBeCopied)
    {
        this.totalBytesToBeCopied = totalBytesToBeCopied;
        this.totalBytesRemaining = totalBytesToBeCopied;
        if (generateLogFile)
            logFileOutput.println("B "+totalBytesToBeCopied);
    }

    public long getTotalFilesToBeCopied()
    {
        return totalFilesToBeCopied;
    }

    public void setTotalFilesToBeCopied(long totalFilesToBeCopied)
    {
        this.totalFilesToBeCopied = totalFilesToBeCopied;
        this.totalFilesRemaining = totalFilesToBeCopied;
        if (generateLogFile)
            logFileOutput.println("F "+totalFilesToBeCopied);
    }
    
    public void startingFile(long bytesToCopy)
    {
        startingFile(System.currentTimeMillis(), bytesToCopy);
    }
    
    public void startingFile(long startFileTime, long bytesToCopy)
    {

        // First, calculate remaining overhead for previous file
        if (currentFileStartTime > -1)
        {
            long totalFileTime = startFileTime - currentFileStartTime;
            long copyTime = currentFileCopyStopTime - currentFileCopyStartTime;
            long currentOverheadTime = totalFileTime - copyTime;
            totalFilesRemaining --;
            updateBucketOverheadTime(currentOverheadTime, currentFileBytesToCopy, startFileTime);
        }
        currentFileBytesToCopy = bytesToCopy;
        currentFileStartTime = startFileTime;
        currentFileCopyStartTime = -1;
        currentFileCopyStopTime = -1;
        
        lastCopyUpdateTime = 0;
        if (generateLogFile)
            logFileOutput.println("FS "+startFileTime+" "+bytesToCopy);
       
    }
    
    public void startingCopying()
    {
        startingCopying(System.currentTimeMillis());
    }
    
    public void startingCopying(long startCopyTime)
    {
        if (currentFileCopyStartTime > -1)
            logger.debug("startCopying called before finishCopying");
        currentFileCopyStartTime = startCopyTime;
        lastCopyUpdateTime = startCopyTime;

        if (generateLogFile)
            logFileOutput.println("CB "+startCopyTime);
    }
    
    public void copyUpdate(long bytesCopiedThisUpdate)
    {
        copyUpdate(System.currentTimeMillis(), bytesCopiedThisUpdate);
    }
    
    public void copyUpdate(long updateTime, long bytesCopiedThisUpdate)
    {
        if (generateLogFile)
            logFileOutput.println("CU "+updateTime+" "+bytesCopiedThisUpdate);
        long timeDelta = updateTime - lastCopyUpdateTime;
        if (timeDelta <= 0)  // Something got done in less than 1ms?  Kinda fishy
            timeDelta = 1;
        long bytesPerMS = bytesCopiedThisUpdate/timeDelta;
        lastCopyUpdateTime = updateTime;
        totalBytesRemaining -= bytesCopiedThisUpdate;
        updateBucketSpeed(bytesPerMS, updateTime);
    }
    
    public void finishedCopying()
    {
        finishedCopying(System.currentTimeMillis());
    }
    
    public void finishedCopying(long stopCopyTime)
    {
        currentFileCopyStopTime = stopCopyTime;
        /*
        long timeDelta = stopCopyTime - currentFileCopyStartTime;
        if (timeDelta <= 0)
            timeDelta = 1;
        if (currentFileBytesToCopy > 0)
        {
            long bytesPerMS = currentFileBytesToCopy/timeDelta;
            
            updateBucketSpeed(bytesPerMS, stopCopyTime);
        }*/
        if (generateLogFile)
            logFileOutput.println("CF "+stopCopyTime);
    }
    
    public long getTimeRemaining(long now)
    {
        if (estimatedCompletionTime < 0)
            return -1;
        long estimatedTimeRemaining = estimatedCompletionTime - now;
        
        while (estimatedTimeRemaining < 0)
        {
            // recalculate estimatedCompletionTime
            estimatedTimeRemaining = estimatedCompletionTime - now;
            
        }
        
        return estimatedTimeRemaining;
    }
    
    protected void updateBucketSpeed(long bytesPerMS, long now)
    {
        SampleBucket curBucket = getCurrentSampleBucket(now);
        curBucket.addSpeedSample(bytesPerMS);
    }

    protected void updateBucketOverheadTime(long curFileOverheadTime, long bytesCopied, long now)
    {
        SampleBucket curBucket = getCurrentSampleBucket(now);
        curBucket.addFileOverheadSample(curFileOverheadTime, bytesCopied);
    }
    
    protected SampleBucket getCurrentSampleBucket(long now)
    {
        SampleBucket curBucket = null;
        if (samples.size() > 0)
        {
            curBucket = samples.get(samples.size() - 1);
            if (now - curBucket.getFirstSampleTime() > kMillisecondsPerBucket)
                curBucket = null;   // Time to start a new bucket
        }
        
        if (curBucket == null)
        {
            curBucket = new SampleBucket(now);
            samples.add(curBucket);
            while(samples.size() > kMaxSampleBuckets)
                samples.remove(0);
            updateFinishEstimate(now);
        }
        return curBucket;
    }
    
    protected void updateFinishEstimate(long now)
    {
        if (samples.size() > kMinSampleBuckets)
        {
            long totalFileOverhead = 0, totalBytesPerSecond = 0;
            int numFileOverheadSamples = 0, numTotalBytesPerSecondSamples = 0;  // Some buckets may not have any samples of a given type
            long filesProcessedInSamples = 0, bytesCopiedInSamples = 0;
            for (SampleBucket curBucket:samples)
            {
                long curBucketFileOverheadAverage = curBucket.getFileOverheadAverage();
                if (curBucketFileOverheadAverage > -1)
                {
                    totalFileOverhead += curBucketFileOverheadAverage;
                    numFileOverheadSamples++;
                    filesProcessedInSamples += curBucket.overheadSamplesInBucket;
                }
                long curBucketBytesPerSecondAverage = curBucket.getBytesPerSecondAverage();
                if (curBucketBytesPerSecondAverage > -1)
                {
                    totalBytesPerSecond += curBucketBytesPerSecondAverage;
                    numTotalBytesPerSecondSamples++;
                    bytesCopiedInSamples += curBucket.bytesCopiedInBucket;
                }
            }
            
            if (numFileOverheadSamples > 0)
                currentAverageFileOverhead = totalFileOverhead/numFileOverheadSamples;
            if (numTotalBytesPerSecondSamples > 0)
                currentAverageBytesPerMS = totalBytesPerSecond/numTotalBytesPerSecondSamples;
            
            long newEstimatedTimeRemaining = 0;
            if (currentAverageBytesPerMS > 0)
                newEstimatedTimeRemaining += totalBytesRemaining/currentAverageBytesPerMS;
            newEstimatedTimeRemaining += totalFilesRemaining * currentAverageFileOverhead;
            
            // Now, how far off is our estimate from reality?
            long estimatedTimeLookingBack = filesProcessedInSamples * currentAverageFileOverhead;
            if (currentAverageBytesPerMS > 0)
                estimatedTimeLookingBack += bytesCopiedInSamples/currentAverageBytesPerMS;

            long actualTimeLookingBack = now-samples.get(0).firstSampleTime;
            double fudgeFactor = (double)actualTimeLookingBack/(double)estimatedTimeLookingBack;
            
            //newEstimatedTimeRemaining = (long)(newEstimatedTimeRemaining * fudgeFactor);
            long newEstimatedCompletionTime = now + newEstimatedTimeRemaining;
            if (estimatedCompletionTime < now)
            {
                // If we think we're already done, that would be a problem.  Just take the new estimate no matter how wildly different it is
                estimatedCompletionTime = newEstimatedCompletionTime;
            }
            else
            {
                long oldEstimatedTimeRemaining = estimatedCompletionTime - now;
                long delta = newEstimatedTimeRemaining - oldEstimatedTimeRemaining;
                
                // Try not to bounce the time estimate up and down too quickly.  Let's move it no more than 10% per bucket interval 
                if (Math.abs(delta) > oldEstimatedTimeRemaining/10)
                {
                    if (delta > 0)
                        delta = oldEstimatedTimeRemaining/10;
                    else
                        delta = -(oldEstimatedTimeRemaining/10);
                }
                
                estimatedCompletionTime = estimatedCompletionTime + delta;
            }
        }
    }
    
    public long getEstimatedCompletionTime()
    {
        return estimatedCompletionTime;
    }
    
    public long getEstimatedTimeRemaining()
    {
        return getEstimatedTimeRemaining(System.currentTimeMillis());
    }
    
    public long getEstimatedTimeRemaining(long now)
    {
        if (estimatedCompletionTime < 0)
            return -1;  // Still calculating
        long timeRemaining = estimatedCompletionTime - now;
        if (timeRemaining < 0)  // This is naughty - maybe the system went to sleep?  Redo the estimate immediately
        {
            updateFinishEstimate(now);
            timeRemaining = estimatedCompletionTime - now;
        }
        
        return timeRemaining;
    }
}
