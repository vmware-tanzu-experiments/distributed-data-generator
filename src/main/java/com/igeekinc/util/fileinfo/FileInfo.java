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
 
package com.igeekinc.util.fileinfo;

import java.util.HashMap;
import java.util.Locale;

import com.igeekinc.util.FileLike;
import com.igeekinc.util.GenericTuple;
import com.igeekinc.util.rules.Rule;
import com.igeekinc.util.rules.RuleMatch;

public class FileInfo
{
    private FileClass fileClass;
    private FileGroup fileGroup;
    private Rule [] matchRules;
    
    HashMap<Locale, String>descriptions;
    
    public FileInfo(FileClass fileClass, FileGroup fileGroup, Rule [] matchRules, GenericTuple<Locale, String>[] descriptions)
    {
        this.fileClass = fileClass;
        this.fileGroup = fileGroup;
        this.matchRules = new Rule [matchRules.length];
        System.arraycopy(matchRules, 0, this.matchRules, 0, matchRules.length);
        this.descriptions = new HashMap<Locale, String>();
        for (GenericTuple<Locale, String> curDescriptionPair:descriptions)
        {
            this.descriptions.put(curDescriptionPair.getKey(), curDescriptionPair.getValue());
        }
    }
    
    public FileClass getFileClass()
    {
        return fileClass;
    }

    public FileGroup getFileGroup()
    {
        return fileGroup;
    }

    public Rule[] getMatchRules()
    {
        Rule [] returnRules = new Rule[matchRules.length];
        System.arraycopy(matchRules, 0, returnRules, 0, matchRules.length);
        return returnRules;
    }

    public RuleMatch fileMatches(FileLike fileToCheck)
    {
        for (Rule curRule:matchRules)
        {
            RuleMatch curMatchStatus = curRule.matchesRule(fileToCheck);
            if (curMatchStatus != RuleMatch.kNoMatch)
                return curMatchStatus;
        }
        return RuleMatch.kNoMatch;
    }
    public String getDescription(Locale locale)
    {
        String returnDescription = descriptions.get(locale);
        if (returnDescription == null)
        {
            if (!locale.equals(Locale.getDefault()))
                returnDescription = descriptions.get(Locale.getDefault());
            if (returnDescription == null)
                returnDescription = descriptions.get(Locale.ENGLISH);
            if (returnDescription == null && descriptions.size() > 0)
                returnDescription = descriptions.values().iterator().next();
        }
        return returnDescription;
    }
    
    public String getDescription()
    {
        return getDescription(Locale.getDefault());
    }
}
