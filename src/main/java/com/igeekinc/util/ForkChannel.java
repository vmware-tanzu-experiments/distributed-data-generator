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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.igeekinc.util.exceptions.ForkNotFoundException;

public class ForkChannel implements PositionableChannel
{
    FileChannel wrappedChannel;
    ClientFile file;
    String forkName;
    
    public ForkChannel(ClientFile file, String forkName, boolean noCache, boolean writeable) throws ForkNotFoundException
    {
        this.file = file;
        this.forkName = forkName;
        wrappedChannel = file.getForkChannel(forkName, noCache, writeable);
    }
    public int read(ByteBuffer dst) throws IOException
    {
        return wrappedChannel.read(dst);
    }

    public void close() throws IOException
    {
        wrappedChannel.close();
    }

    public boolean isOpen()
    {
        return wrappedChannel.isOpen();
    }

    public int write(ByteBuffer src) throws IOException
    {
        return wrappedChannel.write(src);
    }
    public long position() throws IOException
    {
        return wrappedChannel.position();
    }
    public void position(long newPosition) throws IOException
    {
        wrappedChannel.position(newPosition);
    }
}
