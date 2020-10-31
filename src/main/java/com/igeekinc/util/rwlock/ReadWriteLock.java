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


/**
 * A Read/Write lock
 */
public interface ReadWriteLock 
{
	/**
	 * Acquire the lock for reading (shared access).  Returns true if the lock was acquired,
	 * false if it was not
	 * @return
	 */
	public boolean forReading();
	/**
	 * Acquire the lock for reading (shared access).  Returns true if the lock was acquired,
	 * false if it was not
	 * @param waitTime - time to wait for the lock.  -1 is wait forever, 0 is do not wait
	 * @return
	 * @throws InvalidWaitTime
	 */
	public boolean forReading(int waitTime) throws InvalidWaitTime;
	/**
	 * Acquire the lock for writing (exclusive access) Returns true if the lock was acquired,
	 * false if it was not
	 * @return
	 * @throws UpgradeNotAllowed
	 */
	public boolean forWriting() throws UpgradeNotAllowed;
	/**
	 * Acquire the lock for writing (exclusive access) Returns true if the lock was acquired,
	 * false if it was not
	 * @param waitTime  - time to wait for the lock.  -1 is wait forever, 0 is do not wait
	 * @return
	 * @throws InvalidWaitTime
	 * @throws UpgradeNotAllowed
	 */
	public boolean forWriting(int waitTime) throws InvalidWaitTime, UpgradeNotAllowed;
	/**
	 * Releases the lock
	 * @throws LockNotHeld
	 */
	public void release() throws LockNotHeld;
	/**
	 * Returns true if this thread holds the lock for reading (shared access)
	 * @return
	 */
	public boolean holdsReadLock();
	/**
	 * Returns true if thread holds the lock for writing (exclusive access)
	 * @return
	 */
	public boolean holdsWriteLock();
}
