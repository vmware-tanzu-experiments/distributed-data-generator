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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

public class iGeekLogger extends Logger
{
  /**
     The fully qualified name of the Level class. See also the
     getFQCN method. */
  private static final String FQCN = Level.class.getName();

  public iGeekLogger(String name)
  {
    super(name);
  }

  public
  void debug(LogMessage message) {
    if(repository.isDisabled(Level.DEBUG_INT))
      return;
    if(Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.DEBUG, message, null);
    }
  }

  public
  void debug(LogMessage message, Throwable t) {
    if(repository.isDisabled(Level.DEBUG_INT))
      return;
    if(Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.DEBUG, message, t);
  }
  public
  void status(Object message) {
    if(repository.isDisabled(iGeekLevel.STATUS_INT))
      return;
    if(iGeekLevel.STATUS.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, iGeekLevel.STATUS, message, null);
    }
  }

  public
  void status(Object message, Throwable t) {
    if(repository.isDisabled(iGeekLevel.STATUS_INT))
      return;
    if(iGeekLevel.STATUS.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, iGeekLevel.STATUS, message, t);
  }
  public
  void info(Object message) {
    if(repository.isDisabled(Level.INFO_INT))
      return;
    if(Level.INFO.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.INFO, message, null);
    }
  }

  public
  void info(Object message, Throwable t) {
    if(repository.isDisabled(Level.INFO_INT))
      return;
    if(Level.INFO.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.INFO, message, t);
  }

  public
  void warn(LogMessage message) {
    if(repository.isDisabled(Level.WARN_INT))
      return;
    if(Level.WARN.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.WARN, message, null);
    }
  }

  public
  void warn(LogMessage message, Throwable t) {
    if(repository.isDisabled(Level.WARN_INT))
      return;
    if(Level.WARN.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.WARN, message, t);
  }

  public
  void error(LogMessage message) {
    if(repository.isDisabled(Level.ERROR_INT))
      return;
    if(Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.ERROR, message, null);
    }
  }

  public
  void error(LogMessage message, Throwable t) {
    if(repository.isDisabled(Level.ERROR_INT))
      return;
    if(Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.ERROR, message, t);
  }

  public
  void fatal(LogMessage message) {
    if(repository.isDisabled(Level.FATAL_INT))
      return;
    if(Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.FATAL, message, null);
    }
  }

  public
  void fatal(LogMessage message, Throwable t) {
    if(repository.isDisabled(Level.FATAL_INT))
      return;
    if(Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.FATAL, message, t);
  }
  /**
     This method creates a new logging event and logs the event
     without further checks.  */
  protected
  void forcedLog(String fqcn, Priority level, Object message, Throwable t)
  {
    if (message instanceof LogMessage)
      callAppenders(new iGeekLoggingEvent(fqcn, this, level, (LogMessage)message, t));
    else
      callAppenders(new LoggingEvent(fqcn, this, level, message, t));
  }

}