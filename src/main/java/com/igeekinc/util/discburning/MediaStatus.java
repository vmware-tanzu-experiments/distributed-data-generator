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

import java.io.Serializable;

public class MediaStatus implements Serializable
{
    private static final long serialVersionUID = -4655165075217298066L;
    public static final MediaStatus kNoMediaStatus = new MediaStatus(false, false, false, false, true, false);
    private boolean blank, erasable, overwritable, appendable, noMedia, notWritable;
    public MediaStatus(boolean blank, boolean erasable, boolean overwritable, boolean appendable, 
            boolean noMedia, boolean notWritable)
    {
        if (noMedia && (blank || erasable || overwritable || appendable || notWritable))
            throw new IllegalArgumentException("noMedia cannot be combined with any other statuses");
        this.blank = blank;
        this.erasable = erasable;
        this.overwritable = overwritable;
        this.appendable = appendable;
        this.noMedia = noMedia;
        this.notWritable = notWritable;
    }
    
    public boolean isAppendable()
    {
        return appendable;
    }
    public boolean isBlank()
    {
        return blank;
    }
    public boolean isErasable()
    {
        return erasable;
    }
    public boolean isNoMedia()
    {
        return noMedia;
    }
    public boolean isNotWritable()
    {
        return notWritable;
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (appendable ? 1231 : 1237);
        result = PRIME * result + (blank ? 1231 : 1237);
        result = PRIME * result + (erasable ? 1231 : 1237);
        result = PRIME * result + (noMedia ? 1231 : 1237);
        result = PRIME * result + (notWritable ? 1231 : 1237);
        result = PRIME * result + (overwritable ? 1231 : 1237);
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MediaStatus other = (MediaStatus) obj;
        if (appendable != other.appendable)
            return false;
        if (blank != other.blank)
            return false;
        if (erasable != other.erasable)
            return false;
        if (noMedia != other.noMedia)
            return false;
        if (notWritable != other.notWritable)
            return false;
        if (overwritable != other.overwritable)
            return false;
        return true;
    }
    
}
