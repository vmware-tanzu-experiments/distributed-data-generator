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
 
package com.igeekinc.util.macos.macosx;

import com.igeekinc.util.Group;
import com.igeekinc.util.SystemInfo;
import com.igeekinc.util.User;
import com.igeekinc.util.exceptions.UserNotFoundException;
import com.igeekinc.util.logging.ErrorLogMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;



public class MacOSXGroup extends Group
{
	private static final long serialVersionUID = -8109069971431217255L;
	int gid;
    String [] members;
    
    public MacOSXGroup(int gid, String groupName, String [] members)
    {
    	this.gid = gid;
    	this.groupName = groupName;
    	this.members = members;
    }
    
    public int getGID()
    {
        return gid;
    }
    
    @Override
    public Iterator<User> listUsers() throws IOException
    {
        ArrayList<User> usersList = new ArrayList<User>();
        for (String curMember:members)
        {
            User curUser = null;
            try
            {
                curUser = SystemInfo.getSystemInfo().getUserInfoForName(curMember);
            } catch (UserNotFoundException e)
            {
                LogManager.getLogger(getClass()).error(new ErrorLogMessage("Caught exception"), e);
            }
            if (curUser != null)
                usersList.add(curUser);
        }
        return usersList.iterator();
    }
    
    public String toString()
    {
        String returnString;
        returnString = "gid = "+gid+" groupName = "+groupName;
        return returnString;
    }
}
