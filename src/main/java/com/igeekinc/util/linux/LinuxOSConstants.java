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
 
package com.igeekinc.util.linux;

public class LinuxOSConstants
{
	public static final int S_IFMT = 0170000;		/* type of file mask */
	public static final int S_IFIFO = 0010000;		/* named pipe (fifo) */
	public static final int S_IFCHR = 0020000;		/* character special */
	public static final int S_IFDIR = 0040000;		/* directory */
	public static final int S_IFBLK = 0060000;		/* block special */
	public static final int S_IFREG = 0100000;		/* regular */
	public static final int S_IFLNK = 0120000;		/* symbolic link */
	public static final int S_IFSOCK = 0140000;		/* socket */
	public static final int S_IFWHT = 0160000;		/* whiteout */
	
	
	public static final int  O_ACCMODE  =        0003;
	public static final int  O_RDONLY   =          00;
	public static final int  O_WRONLY   =          01;
	public static final int  O_RDWR     =          02;
	public static final int  O_CREAT    =        0100; /* not fcntl */
	public static final int  O_EXCL     =        0200; /* not fcntl */
	public static final int  O_NOCTTY   =        0400; /* not fcntl */
	public static final int  O_TRUNC    =       01000; /* not fcntl */
	public static final int  O_APPEND   =       02000;
	public static final int  O_NONBLOCK =       04000;
	public static final int  O_NDELAY   =     O_NONBLOCK;
	public static final int  O_SYNC     =    04010000;
	public static final int  O_FSYNC    =      O_SYNC;
	public static final int  O_ASYNC    =      020000;
}
