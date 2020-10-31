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
 
package com.igeekinc.util.windows;

import java.io.File;

import com.igeekinc.util.User;

public class WindowsUser extends User
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -7172211261090075960L;
public WindowsUser(String inUserName)
  {
    userName = inUserName;
  }

  public String toString()
  {
    return userName;
  }
  public boolean equals(Object checkObject)
  {
    if (checkObject == null)
      return false;
    if (checkObject.getClass() != WindowsUser.class)
      return false;
    WindowsUser checkUser = (WindowsUser)checkObject;
    if (checkUser.userName.equals(userName))
      return true;
    return false;
  }
	/* (non-Javadoc)
	 * @see com.igeekinc.util.User#getHomeDirectory()
	 */
	public File getHomeDirectory()
	{
	    File userParentDir = new File("C:/Documents and Settings");
		return new File(userParentDir, userName);
	}

}