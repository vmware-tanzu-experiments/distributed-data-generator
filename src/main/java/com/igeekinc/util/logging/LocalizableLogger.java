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
import org.apache.logging.log4j.Logger;
public class LocalizableLogger
{
	Logger logger;
	/**
	 * 
	 */
	public LocalizableLogger(Logger inLogger)
	{
		logger = inLogger;
	}

	public void debug(DebugLogMessage message)
	{
		logger.debug(message);
	}
	
	public void info(InfoLogMessage message)
	{
		logger.info(message);
	}
	public void warn(WarnLogMessage message)
	{
		logger.warn(message);
	}
	
	public void error(ErrorLogMessage message)
	{
		logger.error(message);
	}
	
	public void fatal(FatalLogMessage message)
	{
		logger.fatal(message);
	}
}
