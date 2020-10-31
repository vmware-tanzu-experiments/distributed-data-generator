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
 
package com.igeekinc.util.rules;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import com.igeekinc.util.FileLike;

public class RuleSet implements Rule, Serializable
{
	public static final long serialVersionUID=-8313254000947393864L;
	ArrayList<Rule> rules;
	String name;
	
	public RuleSet()
	{
		rules = new ArrayList<Rule>();
	}
	
	public void addRule(Rule newRule)
	{
		rules.add(newRule);
	}
	
	public int getNumRules()
	{
		return rules.size();
	}
	
	public Rule getRule(int ruleNum)
	{
		return (Rule)rules.get(ruleNum);
	}
	
	public void removeRule(int removeNum)
	{
		rules.remove(removeNum);
	}
	
	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.Rule#matchesRule(com.igeekinc.util.ClientFile)
	 */
	public RuleMatch matchesRule(FileLike checkFile)
	{
		for (int curCheckRuleNum = 0; curCheckRuleNum < rules.size(); curCheckRuleNum++)
		{
		    Rule curCheckRule = (Rule)rules.get(curCheckRuleNum);
		    RuleMatch ruleResult = curCheckRule.matchesRule(checkFile);
			if (ruleResult != RuleMatch.kNoMatch)
				return ruleResult;
		}
		return RuleMatch.kNoMatch;
	}

	public void init()
	{
		for (int curInitRuleNum = 0; curInitRuleNum < rules.size(); curInitRuleNum++)
		{
			Rule curInitRule = (Rule)rules.get(curInitRuleNum);
			curInitRule.init();
		}
	}
	
	public String toString()
	{
		return name;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	 @SuppressWarnings({ "unchecked", "rawtypes" })
	private void readObject(java.io.ObjectInputStream stream)
     throws IOException, ClassNotFoundException
     {
	     ObjectInputStream.GetField fields =
	         stream.readFields();
	     name = (String)fields.get("name", null);
	     Object list = fields.get("rules", null);
	     if (list instanceof ArrayList)
	         rules = (ArrayList<Rule>)list;
	     if (list instanceof Vector)
	     {
	         rules = new ArrayList<Rule>((Vector)list);
	     }
     }
}
