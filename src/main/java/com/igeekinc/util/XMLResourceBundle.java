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
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

public class XMLResourceBundle extends ResourceBundle
{
    XMLProperties lookup;
    public XMLResourceBundle(InputStream loadStream)
    throws IOException
    {
        lookup = new XMLProperties();
        lookup.load(loadStream);
    }

    public XMLResourceBundle(String resourceName)
    throws IOException
    {
        String resourcePath = resourceName.replace('.','/')+".xmlproperties";
        InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
        try
        {
            lookup = new XMLProperties();
        }
        catch (Error err)
        {
            err.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        try
        {
            lookup.load(resourceStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (Error e)
        {
            e.printStackTrace();
        }
    }

    public ImageIcon getImageIcon(String resourceName)
    {
        String imagePathName = lookup.getProperty(resourceName);
        if (imagePathName == null)
            return null;
        URL imageURL = ClassLoader.getSystemClassLoader().getResource(imagePathName);
        ImageIcon returnIcon = new ImageIcon(imageURL, resourceName);
        return(returnIcon);
    }
    /**
     * Override of ResourceBundle, same semantics
     */
    public Object handleGetObject(String key) {
        Object obj = lookup.get(key);
        return obj; // once serialization is in place, you can do non-strings
    }

    /**
     * Implementation of ResourceBundle.getKeys.
     */
    public Enumeration<String> getKeys() {
        Enumeration<String> result = null;
        final Enumeration<Object> myKeys = lookup.keys();
        Enumeration<String> interimParentKeys = null;
        if (parent != null)
            interimParentKeys = parent.getKeys();
        final Enumeration<String> parentKeys = interimParentKeys;


        result = new Enumeration<String>() 
        {
            public boolean hasMoreElements() 
            {
                if (temp == null)
                    nextElement();
                return temp != null;
            }

            public String nextElement() 
            {
                String returnVal = temp;
                if (myKeys.hasMoreElements())
                {
                    temp = (String)myKeys.nextElement();
                }
                else 
                {
                    temp = null;
                    if (parentKeys != null)
                    {
                        while (temp == null && parentKeys.hasMoreElements()) 
                        {
                            temp = parentKeys.nextElement();
                            if (lookup.containsKey(temp))
                                temp = null;
                        }
                    }
                }
                return returnVal;
            }

            String temp = null;
        };
        return result;
    }
}