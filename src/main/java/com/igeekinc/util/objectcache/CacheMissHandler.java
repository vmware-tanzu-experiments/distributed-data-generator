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
 
package com.igeekinc.util.objectcache;

import java.io.IOException;
import java.util.HashMap;

public interface CacheMissHandler<K, V extends CachableObjectIF<K>>
{
    V handleMiss(K key);
    K storeObject(V objectToStore) throws IOException;
    /**
     * handleBulkMiss retrieves multiple objects in a single pass.  Keys are passed in in the keys
     * array.  The retrieved objects are inserted into the retrievedObjects HashMap that is passed in.  Keys that
     * are null will leave the corresponding entry in the retrievedObjects HashMap in whatever state it was when passed
     * in.  This allows the cache to find all cached objects and then just ask for the ones it needs
     * @param retrievedObjects
     * @param keys
     * @return
     */
    void handleBulkMiss(HashMap<K, V> retrievedObjects, K[] keys);
    
    void commit();
}
