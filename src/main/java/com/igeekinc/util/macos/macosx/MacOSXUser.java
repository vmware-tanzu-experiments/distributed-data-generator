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

import java.io.File;
import java.io.Serializable;

import com.igeekinc.util.User;

public class MacOSXUser extends User
    implements Serializable
{
  static final long serialVersionUID = -5195180958018451118L;
  private int uid, gid;
  private String password;
  private File homeDir;

  public MacOSXUser(int uid, int gid, String userName, String longName, File homeDir, String password)
  {
    this.uid = uid;
    this.gid = gid;
    this.userName = userName;
    this.longName = longName;
    this.homeDir = homeDir;
    this.password = password;
  }
  
  public String toString()
  {
    return(Integer.toString(uid));
  }
  
	/* (non-Javadoc)
	 * @see com.igeekinc.util.User#getHomeDirectory()
	 */
	public File getHomeDirectory()
	{
		// TODO Auto-generated method stub
		return homeDir;
	}
	
	public int getUID()
	{
		return uid;
	}
	
	public int getGID()
	{
		return gid;
	}

	public String getPasswordHash()
	{
		return password;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + uid;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MacOSXUser other = (MacOSXUser) obj;
		if (uid != other.uid)
			return false;
		return true;
	}
}