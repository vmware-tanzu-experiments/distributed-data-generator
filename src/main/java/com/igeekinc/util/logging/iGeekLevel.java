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
 
package com.igeekinc.util.logging;

import java.io.Serializable;

import org.apache.log4j.Level;

public class iGeekLevel extends Level implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4255454580425867981L;
	public final static int POLLER_DEBUG_INT=DEBUG_INT-1000;
  public final static int STATUS_INT=INFO_INT-5000;
  final static public iGeekLevel STATUS  = new iGeekLevel(STATUS_INT, "STATUS",  6);
  final static public iGeekLevel POLLER_DEBUG = new iGeekLevel(POLLER_DEBUG_INT, "POLLER_DEBUG", 6);
  protected iGeekLevel(int level, String levelStr, int syslogEquivalent)
  {
    super(level, levelStr, syslogEquivalent);
  }
  /**
    Convert an integer passed as argument to a level. If the
    conversion fails, then this method returns {@link #DEBUG}.

  */
  public
  static
  Level toLevel(int val) {
    return (Level) toLevel(val, Level.DEBUG);
  }
  /**
    Convert an integer passed as argument to a level. If the
    conversion fails, then this method returns the specified default.
  */
  public
  static
  Level toLevel(int val, Level defaultLevel)
  {
    if (val == STATUS_INT)
      return iGeekLevel.STATUS;
    if (val == POLLER_DEBUG_INT)
        return iGeekLevel.POLLER_DEBUG;
    return Level.toLevel(val, defaultLevel);
  }
}