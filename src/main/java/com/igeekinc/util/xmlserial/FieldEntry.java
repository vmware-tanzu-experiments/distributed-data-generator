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
 
package com.igeekinc.util.xmlserial;

public class FieldEntry
{
    private String fieldName;
    private Object fieldValue;
    
    public FieldEntry(String fieldName, Object fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public FieldEntry(String fieldName, int fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = new Integer(fieldValue);
    }
    
    public FieldEntry(String fieldName, long fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = new Long(fieldValue);
    }
    
    public FieldEntry(String fieldName, boolean fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = Boolean.valueOf(fieldValue);
    }
    
    public FieldEntry(String fieldName, float fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = new Float(fieldValue);
    }
    
    public FieldEntry(String fieldName, double fieldValue)
    {
        this.fieldName = fieldName;
        this.fieldValue = new Double(fieldValue);
    }
    
    public String getFieldName()
    {
        return fieldName;
    }

    public Object getFieldValue()
    {
        return fieldValue;
    }
}
