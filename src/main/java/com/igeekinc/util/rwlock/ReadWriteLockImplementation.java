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
 
package com.igeekinc.util.rwlock;
import java.util.LinkedList;
import java.util.ListIterator;

public class ReadWriteLockImplementation implements ReadWriteLock {
	// Lock Type Constants
	private static int READ = 1;
	private static int WRITE = 2;

	// Flag to indicate if lock upgrades allowed
	private boolean _allowUpgrades;

	// List to store all the threads that have been granted a lock, 
	// waiting for a new lock, or upgrading a lock.
	private WaitingList waitingList = new WaitingList();

	private class WaitingList extends LinkedList<WaitingListElement>
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = -7462613240969160894L;

		// Return index of the first writer in the list.
		int firstWriterIndex() {
			ListIterator<WaitingListElement> iter = listIterator(0);
			int index = 0;
			while( iter.hasNext() ) {
				if( (iter.next())._lockType == WRITE )
					return(index);
				index++;
			}                                         
			return(-1);
		}

		// Return the index of the last thread granted a lock
		int lastGrantedIndex() {
			ListIterator<WaitingListElement> iter = listIterator(size());
			int index = size()-1;
			while( iter.hasPrevious() ) {
				if( (iter.previous())._granted == true )
					return(index);
				index--;
			}

			return(-1);
		}

		// Return index of the element for this thread
		int findMe() {
			return(indexOf(new WaitingListElement()));
		}
	}

	// Element stored in the waiting list.
	private class WaitingListElement {
		java.lang.Thread _key;	// set equal to the Thread ID
		int _lockType;			// READ or WRITE	
		int _lockCount;			// # of times lock was taken
		boolean _granted;	// Has this thread been granted the lock

		WaitingListElement() {
			this(0);
		}
		WaitingListElement(int type) {
			_key = java.lang.Thread.currentThread();
			_lockType = type;
			_lockCount =  0;       
			_granted = false;
		}

		// Two elements are equal if their keys are equal.
		public boolean equals(Object o) {
			if( !(o instanceof WaitingListElement) )
				return(false);
			WaitingListElement element = (WaitingListElement)o;
			if( element._key == this._key )
				return(true);

			return(false);
		}
	}

	// Constructors...
	public ReadWriteLockImplementation() {
		this(false);
	}
	
	public ReadWriteLockImplementation(boolean allowUpgrades) {
		_allowUpgrades = allowUpgrades;      
	}

	///////////////////////////////////////////////////////////////
	//								 //
	// All of the following methods are from the RWLock interface//
	//								 //
	///////////////////////////////////////////////////////////////

	synchronized  public boolean forReading() {
		try {
			// Delegate to the other one
			return(forReading(-1));
		}
		catch( InvalidWaitTime e ) {
			return(false);
		}
	}

	synchronized  public boolean forReading(int waitTime) 
	throws InvalidWaitTime
	{
		if( waitTime < -1 )
			throw new InvalidWaitTime();

		WaitingListElement element = null;

		// Is there a node for this thread already in the list?
		// If not, create a new one.
		int index = waitingList.findMe();
		if( index != -1 )
			element = (WaitingListElement)waitingList.get(index);
		else {
			element = new WaitingListElement(READ);
			waitingList.add(element);
		}

		// If a lock has already been granted once
		// just increment the count and return.
		// It does not matter whether the lock granted initially was a
		// READ or WRITE lock. (see forWriting where it does matter!)
		if( element._lockCount > 0 ) {
			element._lockCount++;       
			return(true);
		}

		long startTime = System.currentTimeMillis();
		do       {
			// If there is a writer in front of me I have to wait...
			int nextWriter = -1;
			nextWriter = waitingList.firstWriterIndex();       
			index = waitingList.findMe();
			if( nextWriter == -1 || nextWriter > index ) {
				element._lockCount++;
				element._granted = true;
				return(true);
			}

			// Non-blocking version, just return.       
			// Do not need notifyAll here since we added and removed the
			// new element within the same synchronized block and so
			// no other thread ever saw it.
			if( waitTime == 0 ) {
				waitingList.remove(element);                               
				return(false);
			}

			// I guess I have to wait...
			try {
				// -1 indicates wait forever in our implementation
				if( waitTime == -1 )
					wait();
				else {
					// How much more time do we wait?
					long delta = (long)java.lang.Math.abs
		   				 (System.currentTimeMillis() - startTime);
					wait(waitTime-delta);              
				}
			}
			catch( java.lang.InterruptedException e ) {
			}

		} while( waitTime == -1 || 
		     	 ((startTime + waitTime) > System.currentTimeMillis()) );  

		// Could not get the lock and timed out.
		waitingList.remove(element);             
		// Important to notify all threads of our removal.
		notifyAll();
		// Failed to get lock. 
		return(false);       
	}

	synchronized public boolean forWriting() throws UpgradeNotAllowed
	{
		try {
			// Delegate to the other one.
			return(forWriting(-1));
		}
		catch( InvalidWaitTime e ) {
			return(false);
		}
	}

	synchronized public boolean forWriting(int waitTime) 
	throws InvalidWaitTime, UpgradeNotAllowed
	{
		if( waitTime < -1 )
			throw new InvalidWaitTime();

		WaitingListElement element = null;

		// Is there a node for this thread already in the list?
		// If not, create a new one.
		int index = waitingList.findMe();
		if( index != -1 )
			element = (WaitingListElement)waitingList.get(index);
		else {
			element = new WaitingListElement(WRITE);
			waitingList.add(element);
		}

		// If the thread has a READ lock, we need to upgrade
		if( element._granted == true && element._lockType == READ ) {
			try {
				if( !upgrade(waitTime) )
					return(false);
			}
			catch( LockNotHeld e ) {
				return(false);
			}
		}

		// If a lock has already been granted once
		// just increment the count and return.
		// At this point the thread either had a WRITE lock or was
		// upgraded to have one.
		if( element._lockCount > 0 ) {
			element._lockCount++;       
			return(true);
		}

		long startTime = System.currentTimeMillis();
		do {
			// If there are any readers in front of me
			// I have to wait...
			index = waitingList.findMe();
			if( index == 0 ) {
				element._lockCount++;
				element._granted = true;
				return(true);
			}

			// Non-blocking version, just return.       
			// Do not need notifyAll here since we added and removed the
			// new element within the same synchronized block and so
			// no other thread ever saw it.
			if( waitTime == 0 ) {
				waitingList.remove(element);                               
				return(false);
			}

			// I guess I have to wait...
			try {
				// -1 indicates wait forever in our implementation
				if( waitTime == -1 )
					wait();
				else {
					// How much more time do we wait?
					long delta = (long)java.lang.Math.abs
						 (System.currentTimeMillis() - startTime);              
					wait(waitTime-delta);              
				}
			}
			catch( java.lang.InterruptedException e ) {
			}

		} while( waitTime == -1 
		    	 || ((startTime + waitTime) > System.currentTimeMillis()) );  

		// Could not get the lock and timed out.
		waitingList.remove(element);                   
		// Important to notify all threads of our removal.
		notifyAll();
		// Failed to get the lock.
		return(false);
	}

	synchronized public boolean upgrade() 
	throws UpgradeNotAllowed, LockNotHeld
	{
		try {
			// Delegate to the other one.
			return(upgrade(-1));     
		}
		catch( InvalidWaitTime e ) {
			return(false);
		}
	}

	synchronized public boolean upgrade(int waitTime) 
	throws InvalidWaitTime, UpgradeNotAllowed, LockNotHeld
	{
		if( !_allowUpgrades )
			throw new UpgradeNotAllowed();
		if( waitTime < -1 )
			throw new InvalidWaitTime();

		// We should already be in the list.
		// If not, it is an error.
		int index = waitingList.findMe();
		if( index == -1 )
			throw new LockNotHeld();

		// Get the actual element
		// If the lock type is already WRITE, just return.
		WaitingListElement element = 
		(WaitingListElement)waitingList.get(index);
		if( element._lockType == WRITE )
			return(true);

		// What is the index of the last granted lock?       
		int lastGranted = waitingList.lastGrantedIndex();

		// lastReader can not be -1, after all we are granted a READ lock!
		if( lastGranted == -1 )
			throw new LockNotHeld();

		// If we are not the last granted lock, 
		// then we will position ourselves as such.
		// See why in the next step.
		if( index != lastGranted ) {
			waitingList.remove(index);
			ListIterator<WaitingListElement> iter = waitingList.listIterator(lastGranted);
			iter.add(element);
		}

		// We want new readers to think this is a write lock      
		// This is important so that they block i.e. do not get granted.
		// Since we are now waiting for a write lock it is
		// important that we were after all granted read locks.
		element._lockType = WRITE;

		long startTime = System.currentTimeMillis();
		do {
			index = waitingList.findMe();
			if( index == 0 ) {
				return(true);
			}

			// Non-blocking version.
			// Do not need notifyAll here since we changed the lock type 
			// back and forth within the same synchronized block and so
			// no other thread ever saw it.
			if( waitTime == 0 ) {
				// Back to READ type            
				element._lockType = READ;            
				// No need to readjust position since it does not matter
				// for already granted locks.
				return(false);
			}

			// I guess I have to wait...
			try {
				// -1 indicates wait forever in our implementation
				if( waitTime == -1 )
					wait();
				else {
					// How much more time do we wait?
					long delta = (long)java.lang.Math.abs
		   				 (System.currentTimeMillis() - startTime);              
					wait(waitTime-delta);              
				}
			}
			catch( java.lang.InterruptedException e ) {
			}

		} while( waitTime == -1 
		       	 || ((startTime + waitTime) > System.currentTimeMillis()) );  

		// We failed to upgrade. Go back to original lock type
		element._lockType = READ;
		// Important to notify all threads that we are back 
		// to being a READ lock.
		notifyAll();       
		// Failed to upgrade.
		return(false);       
	}

	synchronized public boolean downgrade() throws LockNotHeld
	{
		// We should already be in the list.
		// If not, it is an error.       
		int index = waitingList.findMe();
		if( index == -1 )
			throw new LockNotHeld();

		// Get the element for this thread
		WaitingListElement element = 
		(WaitingListElement)waitingList.get(index);

		// Downgrade the WRITE lock and notify all threads of the change.
		if( element._lockType == WRITE ) {
			element._lockType = READ;
			notifyAll();       
		}

		return(true);
	}

	synchronized public void release() throws LockNotHeld
	{
		// We should already be in the list.
		// If not, it is an error.       
		if( waitingList.isEmpty() )
			throw new LockNotHeld();

		// We should already be in the list.
		// If not, it is an error.       
		int index = waitingList.findMe();
		if( index == -1 )
			throw new LockNotHeld();

		// Get the element for this thread
		WaitingListElement e = (WaitingListElement)waitingList.get(index);

		// If the lock count goes down to zero, 
		// remove the lock and notify all threads of the change.
		if( (--e._lockCount) == 0 ) {
			waitingList.remove(index);            
			notifyAll();      
		}
	}    
	
    public boolean holdsReadLock()
    {
        int index = waitingList.findMe();
        if (index == -1)
            return false;
        WaitingListElement e = (WaitingListElement)waitingList.get(index);
        // READ or WRITE is OK for reading
        if ((e._lockType == READ || e._lockType == WRITE) && e._granted)
            return true;
        return false;
    }
    
    public boolean holdsWriteLock()
    {
        int index = waitingList.findMe();
        if (index == -1)
            return false;
        WaitingListElement e = (WaitingListElement)waitingList.get(index);
        if (e._lockType == WRITE && e._granted)
            return true;
        return false;
    }
}
