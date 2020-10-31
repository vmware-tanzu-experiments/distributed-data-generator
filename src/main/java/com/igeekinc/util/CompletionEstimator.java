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

import java.util.LinkedList;

class RunningAverage
{
	private double total;
	private int numPoints;
	private int pointsToAverage;
	public RunningAverage(int pointsToAverage)
	{
		total = 0.0;
		numPoints = 0;
		this.pointsToAverage = pointsToAverage;
	}
	
	/*
	 * Adds a value and removes a value.  If removeValue is null, increases the number of
	 * points in the average
	 */
	public void update(Long addValue, Long removeValue)
	{
		if (addValue == null)
			throw new IllegalArgumentException("addValue cannot be null");
		if (removeValue == null)
		{
			numPoints++;
			total += addValue;
		}
		else
		{
			total -= ((double)removeValue);
			total += ((double)addValue);
		}
	}
	
	public void update(Double addValue, Double removeValue)
	{
		if (addValue == null)
			throw new IllegalArgumentException("addValue cannot be null");
		if (removeValue == null)
		{
			numPoints ++;
			total += addValue;
		}
		else
		{
			total -= removeValue;
			total += addValue;
		}
		if (numPoints > pointsToAverage)
			throw new InternalError("Too many points");
	}
	public double getAverage()
	{
		if (numPoints > 0)
			return total/numPoints;
		else
			return total;
	}
	
	public int getNumPoints()
	{
		return numPoints;
	}
	
	public int getPointsToAverage()
	{
		return pointsToAverage;
	}
}
/**
 * CompletionEstimator keeps track of the rate of completion of n different
 * types of items being completed and will produce an estimate of when the entire process
 * will be completed.<br/>
 * Internally, there are n fifos of rates (currently set to hold 10 rates each).  As
 * items are completed, a new rate for each item type is calculated and then averaged against
 * the previous rates and then this average rate is used with the number of items remaining
 * to calculate how much time completing the remaining items will take.  The longest time from
 * all the item types is returned.
 */
public class CompletionEstimator
{
    protected LinkedList<Double> [] rates;
    protected long [] totalItems;
    protected long [] itemsRemaining;
    protected long lastUpdateTime;
    protected static final int kRatesToAverage = 10000;
    protected RunningAverage [] everything, last100, last500;
    protected long lastTimeRemaining = -1;
    /**
     * Initialize a CompletionEstimator with the number of item types
     * to track and the number of items for each type.  The position of each type in the 
     * inItemsRemaining is the position that they need to be in in calls to
     * getNextEstimate()
     * @param numItemTypesToTrack
     * @param totalItems
     */

	public CompletionEstimator(long [] totalItems)
    {
        init(totalItems); 
    }


    protected CompletionEstimator()
    {
    	
    }
    
	@SuppressWarnings("unchecked")
	protected void init(long[] totalItems)
	{
        rates = new LinkedList[totalItems.length];
        itemsRemaining = new long[totalItems.length];
        this.totalItems = new long[totalItems.length];
        everything = new RunningAverage[totalItems.length];
        last100 = new RunningAverage[totalItems.length];
        last500 = new RunningAverage[totalItems.length];
        
        for (int curRateNum = 0; curRateNum < totalItems.length; curRateNum++)
        {
            rates[curRateNum] = new LinkedList<Double>();
            itemsRemaining[curRateNum] = totalItems[curRateNum];
            this.totalItems[curRateNum] = totalItems[curRateNum];
            everything[curRateNum] = new RunningAverage(kRatesToAverage);
            last100[curRateNum] = new RunningAverage(kRatesToAverage/10);
            last500[curRateNum] = new RunningAverage(kRatesToAverage/2);
        }
        lastUpdateTime = -1;
        
	}
    

    /**
     * Calculates how much longer (in milliseconds) it will take to 
     * handle the remaining items based on how long it has been taking
     * to handle items.
     * @param itemsHandled
     * @return
     */
    public long getNextEstimate(long [] itemsHandled)
    {
        return getNextEstimate(itemsHandled, System.currentTimeMillis());
    }
    
    protected long getNextEstimate(long [] itemsHandled, long currentTime)
    {
        if (itemsHandled.length != itemsRemaining.length)
            throw new IllegalArgumentException("Number of items in itemsHandled must equal number of rates");
        for (int curItemNum = 0; curItemNum < itemsHandled.length ; curItemNum++)
        {
            itemsRemaining[curItemNum] -= itemsHandled[curItemNum];
            if (itemsRemaining[curItemNum] < 0)
            	itemsRemaining[curItemNum] = 0;
        }
        long prevUpdateTime = lastUpdateTime;
        lastUpdateTime = currentTime;
        if (prevUpdateTime >= 0)
        {
            long timeDelta = currentTime - prevUpdateTime;
            if (timeDelta <= 0)
            	timeDelta = 1;
            long [] estimatePerRate = new long[rates.length];
            
            for (int curItemNum = 0; curItemNum < rates.length; curItemNum++)
            {
                double curRate = ((double)itemsHandled[curItemNum])/((double)timeDelta);
                LinkedList<Double> curRateQueue = rates[curItemNum];
                curRateQueue.add(new Double(curRate));
                if (curRateQueue.size() > everything[curItemNum].getPointsToAverage())
                {
                	everything[curItemNum].update(curRate, curRateQueue.get(0));
                	while (curRateQueue.size() > everything[curItemNum].getPointsToAverage())
                		curRateQueue.remove(0);
                }
                else
                {
                	everything[curItemNum].update(curRate, null);
                }
                if (curRateQueue.size() > last100[curItemNum].getPointsToAverage())
                {
                	last100[curItemNum].update(curRate, curRateQueue.get(curRateQueue.size() - last100[curItemNum].getPointsToAverage()));
                }
                else
                {
                	last100[curItemNum].update(curRate, null);
                }
                if (curRateQueue.size() > last500[curItemNum].getPointsToAverage())
                {
                	last500[curItemNum].update(curRate, curRateQueue.get(curRateQueue.size() - last500[curItemNum].getPointsToAverage()));
                }
                else
                {
                	last500[curItemNum].update(curRate, null);
                }
                double avgRate = 0.0;
                avgRate = everything[curItemNum].getAverage();
                int weights = 1;
                if (last100[curItemNum].getNumPoints() >= last100[curItemNum].getPointsToAverage())
                {
                	avgRate += 10*last100[curItemNum].getAverage();
                	weights += 10;
                }
                if (last500[curItemNum].getNumPoints() >= last500[curItemNum].getPointsToAverage())
                {
                	avgRate += 5 * last500[curItemNum].getAverage();
                	weights += 5;
                }
                //avgRate = (10*last100[curItemNum].getAverage()) + (5 * last500[curItemNum].getAverage()) + (everything[curItemNum].getAverage());
                avgRate = avgRate / weights;
                long checkTimeRemaining = (long)(itemsRemaining[curItemNum]/avgRate);
                estimatePerRate[curItemNum] = checkTimeRemaining;
            }
            long timeRemaining = 0;
            for (int checkItemNum = 0; checkItemNum < rates.length; checkItemNum++)
            {
            	if (estimatePerRate[checkItemNum] > timeRemaining)
            		timeRemaining = estimatePerRate[checkItemNum];
            }
            if (lastTimeRemaining > 0)
            {
            	// Squelch big swings
            	long maxDelta = timeDelta * 50;
            	long estimateDifference = timeRemaining - lastTimeRemaining;
				if (Math.abs(estimateDifference) > maxDelta)
            	{
            		timeRemaining = lastTimeRemaining + (long)(estimateDifference * .1);
            	}
            }
            lastTimeRemaining = timeRemaining;
            return timeRemaining;
        }
        else
            return -1;
    }
    
    public long [] getTotalItems()
    {
    	return totalItems;
    }
}
