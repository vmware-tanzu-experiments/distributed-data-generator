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
 
package com.igeekinc.util.scripting;

import com.igeekinc.util.User;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.Logger;

//import com.igeekinc.util.logging.iGeekLogger;

public abstract class ScriptExecutor
{

    public ScriptExecutor()
    {
    }

    /**
     * Executes the specified script.  Returns 0 on success, some other integer
     * on failure (depends on exit value of script)
     * @param scriptToExecute
     * @return
     * @throws IOException
     */
    public abstract int executeScript(Script scriptToExecute, String [] arguments, Properties environment, User executeAsUser, Logger logger, PauserControlleeIF pauser)
    throws IOException, AbortedException;
    
    /**
     * Checks to see if the script is executable by this ScriptExecutor.  Returns a reason as
     * as String if it is not executable otherwise returns null
     * @param scriptToCheck
     * @return
     * @throws IOException
     */
    public abstract String notExecutableScriptReason(Script scriptToCheck) throws IOException;
}