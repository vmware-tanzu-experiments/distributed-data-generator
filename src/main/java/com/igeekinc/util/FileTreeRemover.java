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

import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.Logger;



public class FileTreeRemover extends FSTraverser
{

    public FileTreeRemover(Logger inLogger)
    {
        super(inLogger);
        shouldntChangeTooMuch = false;
    }

    @Override
    public void preprocessDirectory(FileLike curDirectory,
            PauserControlleeIF pauser) throws AbortedException, IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleFile(FileLike curFile, PauserControlleeIF pauser)
            throws AbortedException, IOException
    {
        pauser.checkPauseAndAbort();
        if (!curFile.isDirectory() && !((File)curFile).delete())
            throw new IOException("Could not delete file "+curFile.getAbsolutePath());
    }

    @Override
    public void postprocessDirectory(FileLike curDirectory,
            PauserControlleeIF pauser) throws AbortedException, IOException
    {
        if (!((File)curDirectory).delete())
            throw new IOException("Could not delete file "+curDirectory.getAbsolutePath());
    }

    @Override
    public void handleMountPoint(FileLike mountPoint, PauserControlleeIF pauser)
            throws AbortedException, IOException
    {
        // TODO Auto-generated method stub

    }

}
