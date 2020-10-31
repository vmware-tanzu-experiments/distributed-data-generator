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
 
package com.igeekinc.util.powermanagement;

public interface PowerManagementListener
{
	/**
	 * Receive a power management event.  Return true to allow the event or false
	 * to cancel it.  All of the listeners for the event are called and if any return
	 * false the event is cancelled.
	 * @param evt
	 * @return
	 */
	public boolean powerManagementChange(PowerManagementEvent evt);
}
