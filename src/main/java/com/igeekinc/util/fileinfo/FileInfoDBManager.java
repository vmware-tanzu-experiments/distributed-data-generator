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
 
package com.igeekinc.util.fileinfo;

import org.xml.sax.InputSource;

import com.igeekinc.util.FileLike;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.xmlserial.XMLDocParser;

public abstract class FileInfoDBManager
{
    FileInfoDB systemDB, userDB, mergedDB;
    
    public FileInfoDBManager()
    {
        
    }
    
    public void loadDBs(PauserControlleeIF pauser) throws AbortedException
    {
        mergedDB = createFileInfoDB();
        String systemResourceName = getDBResourceName();
        InputSource systemSource = new InputSource(ClassLoader.getSystemResourceAsStream(systemResourceName));
        systemDB = parseSource(systemSource, pauser);
        mergedDB.mergeDB(systemDB);
    }
    
    FileInfoDB parseSource(InputSource source, PauserControlleeIF pauser) throws AbortedException
    {
        FileInfoDBParseHandler fileInfoDBParseHandler = createFileInfoDBParseHandler();
        XMLDocParser<FileInfoDB> parser = new XMLDocParser<FileInfoDB>("FileInfoDB", fileInfoDBParseHandler);
        FileInfoDB parsedDB = parser.parse(source, pauser);
        return parsedDB;
    }
    
    public FileInfo infoForFile(FileLike fileToCheck)
    {
        return mergedDB.infoForFile(fileToCheck);
    }
    
    protected abstract String getDBResourceName();
    
    protected abstract FileInfoDB createFileInfoDB();
    public abstract FileInfoDBParseHandler createFileInfoDBParseHandler();
    
    public FileInfoDB getFileInfoDB()
    {
        return mergedDB;
    }
}
