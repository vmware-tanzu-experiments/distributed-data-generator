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
 
package com.igeekinc.util.fsevents;

import java.io.Serializable;

/**
 * FSStreamOffset is an interface placeholder for the actual event stream offset.  Event stream offsets (if used) should identify
 * a point in time in the event stream.  They are expected to be serializable objects (e.g. Longs) that are 
 * treated as opaque - that is, you can't compare the underlying values or add them and get anything meaningful.  
 * The event stream offset should be persistent if used.  Implementations of the interface should implement Comparable (this
 * isn't included in the interface because you cannot compare different implementations of FSStreamOffset)
 */
public interface FSStreamOffset extends Serializable
{
}
