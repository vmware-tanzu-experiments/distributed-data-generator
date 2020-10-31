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

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

public class iGeekLoggingEvent extends LoggingEvent implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 2070086288901677962L;
public LogMessage indelibleMessage;
  public iGeekLoggingEvent(String fqnOfCategoryClass, Category logger,
                      long timeStamp, Priority priority, LogMessage message,
		      Throwable throwable)
  {
    super(fqnOfCategoryClass, logger, timeStamp, priority, message, throwable);
    indelibleMessage = message;
  }
  public iGeekLoggingEvent(String fqnOfCategoryClass, Category logger,
		      Priority priority, LogMessage message, Throwable throwable)
  {
    super(fqnOfCategoryClass, logger, priority, message, throwable);
    indelibleMessage = message;
  }
}