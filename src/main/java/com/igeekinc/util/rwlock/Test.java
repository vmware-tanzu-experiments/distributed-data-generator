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
public class Test {
	private static int READ = 1;
	private static int WRITE = 2;
	private ReadWriteLock lock = new ReadWriteLockImplementation(true);

	public static void main(String[] args) {
		new Test();
	}

	public Test() {
	    try {
		// Our test strategy is to start up several threads that exercise
		// the reader/writer lock
		// The delays help ensure that the threads start 
		// up in order. If the threads do not start in order then
		// the output of the program may change.
		// For example if thread 2 starts first then it will not 
		// block and will get the "write" lock.
		new TestThread(1,READ).start();
		Thread.sleep(1);
		new TestThread2(2,WRITE).start();
		Thread.sleep(20);
		new TestThread(3,READ).start();
		Thread.sleep(1);
		new TestThread3(4,READ).start();       
		Thread.sleep(1);
		new TestThread(5,READ).start();
		Thread.sleep(1);
		new TestThread4(6,WRITE).start();
		Thread.sleep(1);
		new TestThread(7,READ).start();                                 
	    }
	    catch(InterruptedException e) {                                            
	    }      
	}

	// Get the desired lock, sleep for 2 seconds, and release the lock
	class TestThread extends java.lang.Thread {
		int type;                              
		int id;       
		TestThread(int id, int type) {
			this.id = id;
			this.type = type;
		}
		public void run() {
			if( type == READ ) {
				System.out.println(id + ": Trying to get Read Lock...");
				lock.forReading();
				System.out.println(id + ": Got Read Lock...");              
			}
			else {
				System.out.println(id + ": Trying to get Write Lock...");
				try {
					lock.forWriting();
				}
				catch( UpgradeNotAllowed e ) {
				}
				System.out.println(id + ": Got Write Lock...");                                      
			}

			System.out.println(id + ": Sleeping for 2 seconds now...");
			try {
				sleep(2000);
				System.out.println(id + ": Releasing lock...");              
				lock.release();
			}
			catch( LockNotHeld e1 ) {
				System.out.println(id + ": Error: No Lock to release...");
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	// Get the desired lock two times, sleep for 2 seconds, 
	// and release the lock three times 
	class TestThread2 extends java.lang.Thread {
		int type;
		int id;

		TestThread2(int id, int type) {
			this.id = id;
			this.type = type;
		}
		public void run() {
			if( type == READ ) {
				System.out.println(id + ": Trying to get Read Lock...");
				lock.forReading();
				System.out.println(id + ": Got Read Lock...");              
				System.out.println(id + ": Trying to get Read Lock Again...");
				lock.forReading();
				System.out.println(id + ": Got Read Lock Again...");              
			}
			else {
				System.out.println(id + ": Trying to get Write Lock...");
				try {
					lock.forWriting();
					System.out.println(id + ": Got Write Lock...");              
					System.out.println(id + 
									   ": Trying to get Write Lock Again...");
					lock.forWriting();              
				}
				catch( UpgradeNotAllowed e ) {
				}
				System.out.println(id + ": Got Write Lock Again...");              
			}

			System.out.println(id + ": Sleeping for 2 seconds now...");
			try {
				sleep(2000);
				System.out.println(id + ": Releasing lock...");              
				lock.release();
				System.out.println(id + ": Releasing lock Again...");              
				lock.release();              
			}
			catch( LockNotHeld e ) {
				System.out.println(id + ": Error: No Lock to release...");
				return;
			}
			catch( Exception e ) {
				e.printStackTrace();
				return;
			}

			try {
				System.out.println(id + ": Releasing lock Again [Should fail this time]...");              
				lock.release();              
			}
			catch( LockNotHeld e ) {
				System.out.println(id + ": And it did...");
			}

		}
	}

	// Get the desired lock, sleep for 1 second, upgrade the lock, 
	// sleep for 2 seconds, and release the lock
	class TestThread3 extends java.lang.Thread {
		int type;
		int id;                                                                     
		TestThread3(int id, int type) {
			this.id = id;
			this.type = type;
		}
		public void run() {
			if( type == READ ) {
				System.out.println(id + ": Trying to get Read Lock...");
				lock.forReading();
				System.out.println(id + ": Got Read Lock...");          
				try {
					System.out.println(id + 
									   ": Sleeping before trying to upgrade...");
					sleep(1000);                     
					System.out.println(id + ": Trying to Upgrade Lock...");
					lock.forWriting();
					System.out.println(id + ": Upgraded Lock...");
				}
				catch( LockNotHeld e1 ) {
					System.out.println(id + ": Error: No Lock to upgrade...");
				}
				catch( UpgradeNotAllowed e2 ) {
					System.out.println(id + ": Upgrade not allowed...");
				}
				catch( Exception e ) {
				}
			}
			else {
				System.out.println(id + ": Trying to get Write Lock...");
				try {
					lock.forWriting();
				}
				catch( UpgradeNotAllowed e ) {
				}
				System.out.println(id + ": Got Write Lock...");                       
				try {
					System.out.println(id + 
									   ": Sleeping before trying to upgrade...");
					sleep(1000);                     
					System.out.println(id + ": Trying to Upgrade Lock...");
					lock.forWriting();
					System.out.println(id + ": Upgraded Lock...");
				}
				catch( LockNotHeld e1 ) {
					System.out.println(id + ": Error: No Lock to upgrade...");
				}
				catch( UpgradeNotAllowed e2 ) {
					System.out.println(id + ": Upgrade not allowed...");
				}
				catch( Exception e ) {
				}
			}    

			System.out.println(id + ": Sleeping for 2 seconds now...");
			try {
				sleep(2000);
				System.out.println(id + ": Releasing lock...");              
				lock.release();
			}
			catch( LockNotHeld e1 ) {
				System.out.println(id + ": Error: No Lock to release...");
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	// Get the desired lock, sleep for 1 second, downgrade the lock, 
	// sleep for 2 seconds, and release the lock
	class TestThread4 extends java.lang.Thread {
		int type;
		int id;                                  
		TestThread4(int id, int type) {
			this.id = id;
			this.type = type;
		}
		public void run() {
			if( type == READ ) {
				System.out.println(id + ": Trying to get Read Lock...");
				lock.forReading();
				System.out.println(id + ": Got Read Lock...");                          
				try {
					System.out.println(id + 
									   ": Sleeping for 1 second before " + 
									   "downgrading the write lock...");
					sleep(1000);
					lock.release();
					System.out.println(id + ": Write lock downgraded...");
				}
				catch( LockNotHeld e1 ) {
					System.out.println(id + ": Error: No Lock to downgrade...");
				}
				catch( Exception e2 ) {
				}
			}
			else {
				System.out.println(id + ": Trying to get Write Lock...");
				try {
					lock.forWriting();
				}
				catch( UpgradeNotAllowed e ) {
				}
				System.out.println(id + ": Got Write Lock...");                                      
				try {
					System.out.println(id + 
									   ": Sleeping for 1 second " + 
									   "before downgrading the write lock...");
					sleep(1000);
					lock.forReading();
					System.out.println(id + ": Write lock downgraded...");
				}
				catch( LockNotHeld e1 ) {
					System.out.println(id + ": Error: No Lock to downgrade...");
				}
				catch( Exception e2 ) {
				}
			}

			System.out.println(id + ": Sleeping for 2 seconds now...");
			try {
				sleep(2000);
				System.out.println(id + ": Releasing lock...");              
				lock.release();
			}
			catch( LockNotHeld e1 ) {
				System.out.println(id + ": Error: No Lock to release...");
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}                  
}
