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
 
package com.igeekinc.util.discburning;

public interface BurnProgressIndicator
{
    public void updateBurnProgress(float percentComplete);
    public void updateVerifyProgress(float percentComplete);
    public void updatePreparingProgress(float percentComplete);
    public void updateTrackOpenProgress(float percentComplete);
    public void updateTrackCloseProgress(float percentComplete);
    public void updateSessionOpenProgress(float percentComplete);
    public void updateSessionCloseProgress(float percentComplete);
    public void updateErasingProgress(float burnProgressPercent);
    public void burnAborting();
}
