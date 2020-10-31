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

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.igeekinc.util.FileLike;
import com.igeekinc.util.FilePath;

class PathNode implements Serializable
{
	static final long serialVersionUID = 8952328894913052525L;
	String nodeName;
	boolean include;
	Hashtable<String, PathNode> children;
	PathNode(String nodeName, boolean include)
	{
		children = null;
		this.nodeName = nodeName;
		this.include = include;
	}
	
	void addChild(PathNode newChild)
	{
		if (children == null)
			children = new Hashtable<String, PathNode>();
		children.put(newChild.nodeName, newChild);
	}
	
	PathNode getChild(String childName)
	{
		if (children == null)
			return null;
		return((PathNode)children.get(childName));
	}
	
	void clearChildren()
	{
		children = null;
	}
}

/**
 * IncludeExcludeRule allows for inclusion/exclusion points to be set within 
 * a tree.  Internally it contains a sparse tree with nodes for any points where 
 * and inclusion/exclusion has been specifically set along with any needed intermediate
 * nodes.  When a file is presented to matchesRule(), the file's path is followed through
 * the tree until it hits a leaf node or the path is exhausted.  If the path is exhausted first
 * the result always matches.  If a leaf node is hit, the result of matchRule is the include value of the
 * leaf node.  
 */
public class IncludeExcludeRule implements Rule, Serializable
{
	static final long serialVersionUID = -2161507365395941578L;
	PathNode root;
	boolean inverted = false;
	
	public IncludeExcludeRule()
	{
		root = null;
	}
	
	public void setRoot(String rootPath, boolean rootIncluded)
	{
		root = new PathNode(rootPath, rootIncluded);
	}
	
	public void includeExclude(FilePath path, boolean pathIncluded)
	{
		includeExclude(path.toString(), pathIncluded);
	}
	
	/**
	 * Sets include/excluded for the path and all of its children
	 * @param path
	 * @param pathIncluded
	 */
	public void includeExclude(String path, boolean pathIncluded)
	{
		if (!path.startsWith(root.nodeName))
			throw new IllegalArgumentException("Path "+path+" does not begin with root path "+root.nodeName); //$NON-NLS-1$ //$NON-NLS-2$
		String relativePath = path.substring(root.nodeName.length());
		StringTokenizer tokenizer = new StringTokenizer(relativePath, File.separator);

		PathNode curNode = root;
		while (tokenizer.hasMoreTokens())
		{
			String curNodeName = tokenizer.nextToken();
			PathNode nextNode = curNode.getChild(curNodeName);
			if (nextNode == null)
			{
				nextNode = new PathNode(curNodeName, pathIncluded);
				curNode.addChild(nextNode);
			}
            else
            {
                nextNode.include = pathIncluded;
            }
			curNode = nextNode;
		}
		curNode.include = pathIncluded;
		curNode.clearChildren();	// Setting an include/exclude value sets it for all children
	}
    
	public boolean nodeExists(FilePath path)
	{
		return nodeExists(path.toString());
	}
	
    public boolean nodeExists(String absolutePath)
    {
        if (!absolutePath.startsWith(root.nodeName))
        {   
            return false;   // No node here
        }
        String relativePath = absolutePath.substring(root.nodeName.length());
        StringTokenizer tokenizer = new StringTokenizer(relativePath, File.separator);
        PathNode curNode = root;
        if (tokenizer.hasMoreElements())
        {
            while (true)
            {
                String curNodeName = tokenizer.nextToken();
                PathNode nextNode = curNode.getChild(curNodeName);
                if (nextNode == null)
                {
                    return false;   // Ran out of tree before we ran out of path
                }
                curNode = nextNode;
                if (!tokenizer.hasMoreTokens())
                {
                    // We're out of path
                    // curNode cannot be null (we would have bailed above)
                    // Therefore, we're matched against some kind of node

                    return true;
                }

            }
        }
        else
        {
            return true;
        }
    }
	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.Rule#matchesRule(com.igeekinc.util.ClientFile)
	 */
	public RuleMatch matchesRule(FileLike checkFile)
	{
		String absolutePath = checkFile.getAbsolutePath();
		return(matchesRule(absolutePath));
	}
	
	public RuleMatch matchesRule(FilePath checkPath)
	{
		String absolutePath = checkPath.toString();
		return(matchesRule(absolutePath));
	}
	
	public RuleMatch matchesRule(String absolutePath)
	{
        /*
         * 
         */
		if (!absolutePath.startsWith(root.nodeName))
		{	
			if (inverted)
				return RuleMatch.kFileMatches;
			else
				return RuleMatch.kNoMatch;
		}
		String relativePath = absolutePath.substring(root.nodeName.length());
		StringTokenizer tokenizer = new StringTokenizer(relativePath, File.separator);
		boolean matches = false;
		PathNode curNode = root;
		if (tokenizer.hasMoreElements())
		{
		    while (true)
		    {
		        String curNodeName = tokenizer.nextToken();
		        PathNode nextNode = curNode.getChild(curNodeName);
		        if (nextNode == null)
		        {
		            // No match on next node.  Is our current node a leaf or a piece of another path?
		            boolean isLeaf = curNode.children == null || curNode.children.size() == 0;
		            if (isLeaf)
		                matches = curNode.include;
		            else
		            {
		                if (tokenizer.hasMoreTokens())
		                    matches = false;    // Got more path but no more nodes.  No match
		            }
		            curNode = null;
		            break;
		        }
		        curNode = nextNode;
		        if (!tokenizer.hasMoreTokens())
		        {
		            // We're out of path
		            // curNode cannot be null (we would have bailed above)
		            // Therefore, we're either ending matched against a leaf or an interior node
		            // If we're ending on an interior node then it will be matched
		            // If we're ending on a leaf node we need to find out the leaf node's state

		            if (curNode.children != null)
		                matches = true;
		            else
		                matches = curNode.include;
		            break;
		        }

		    }
		}
        else
        {
            // We matched against the root.  If it's an interior node then we return true
            // if it's a leaf we return the value of the root
            if (root.children != null && root.children.size() > 0)
                matches = true;
            else
                matches = root.include;
        }

		if (matches)
		{	
			if (inverted)
				return RuleMatch.kNoMatch;
			else
				return RuleMatch.kSubdirsMatch;
		}
		if (inverted)
			return RuleMatch.kSubdirsMatch;
		else
			return RuleMatch.kNoMatch;

	}

	/* (non-Javadoc)
	 * @see com.igeekinc.util.rules.Rule#init()
	 */
	public void init()
	{
		// TODO Auto-generated method stub

	}

	public void setInverted(boolean inverted)
	{
		this.inverted = inverted;
	}
}
