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

import com.igeekinc.util.logging.WarnLogMessage;
import com.igeekinc.util.pauseabort.AbortedException;
import com.igeekinc.util.pauseabort.PauserControlleeIF;
import com.igeekinc.util.rules.Rule;
import com.igeekinc.util.rules.RuleMatch;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.Logger;



public abstract class FSTraverser
{
  protected FileLike sourceRoot;
  protected FilePath startPoint = null;
  protected Logger logger;
  protected ArrayList<Rule> includeRulesList, excludeRulesList;
  protected Rule [] includeRules, excludeRules;
  protected boolean searchForStartPoint = false;
  protected ScannerRestartInfo restartInfo;
  // Set to false so that we don't whine when things are heavily in flux (mainly for FileTreeRemover)
  protected boolean shouldntChangeTooMuch = true;
  
  public FSTraverser(Logger inLogger)
  {
    sourceRoot = null;
    logger = inLogger;
    includeRules = excludeRules = null;
	includeRulesList = excludeRulesList = null;
  }

  public void setSourceRoot(FileLike newSourceRoot)
  {
    sourceRoot = newSourceRoot;
    startPoint = null;
    searchForStartPoint = false;
  }

  public void setStartPoint(ScannerRestartInfo inRestartInfo)
  {
  	restartInfo = inRestartInfo;
  	startPoint = restartInfo.getPathToStartFrom();
  	searchForStartPoint = true;
  }
  
  public void scan(PauserControlleeIF pauser)
      throws AbortedException, IOException
  {
    if (sourceRoot == null)
      throw new RuntimeException("sourceRoot not set");
    
   pauser.checkPauseAndAbort();
	
	if (includeRulesList != null)
	{
		includeRules = new Rule [includeRulesList.size()];
		includeRulesList.toArray(includeRules);
	}
	
	if (excludeRulesList != null)
	{
		excludeRules = new Rule[excludeRulesList.size()];
		excludeRulesList.toArray(excludeRules);
	}
    
    if (searchForStartPoint && startPoint != null)
    {	
    	FilePath curPath = FilePath.getFilePath(sourceRoot);

    	if (!startPoint.startsWith(curPath))
    		throw new InternalError("Wound up in the wrong directory - startPoint = '"+startPoint+"', curDir = '"+curPath+"'");
    	if (curPath.equals(startPoint))
    	{
    		// OK, we're in the right place
    		searchForStartPoint = false;
    	}
    }
    if (!searchForStartPoint)
    	handleFile(sourceRoot, pauser);
    pauser.checkPauseAndAbort();

    if (sourceRoot.isDirectory())
    {
      recurseDirectory(sourceRoot, pauser);
      postprocessDirectory(sourceRoot, pauser);
    }
  }

  protected void recurseDirectory(FileLike curDirectory, PauserControlleeIF pauser)
  throws AbortedException, IOException
  {
	  long      curDirectoryModTime;
	  String [] fileNames;
	  ArrayList<String>    toProcess, processed, directories;

	  curDirectoryModTime = curDirectory.lastModified();
	  fileNames = curDirectory.list();
	  if (fileNames == null)
		  return;
	  toProcess = new ArrayList<String>(fileNames.length);
	  processed = new ArrayList<String>(fileNames.length);
	  directories = new ArrayList<String>(fileNames.length);
	  /* Add all of the files/directories in the current directory to the "toProcess"
	   * list */
	  for (int curFileNum = 0; curFileNum < fileNames.length; curFileNum++)
	  {
		  toProcess.add(fileNames[curFileNum]);
	  }
	  if (searchForStartPoint && startPoint != null)
	  {	
		  FilePath curPath = FilePath.getFilePath(curDirectory);

		  if (!startPoint.startsWith(curPath))
			  throw new InternalError("Wound up in the wrong directory - startPoint = '"+startPoint+"', curDir = '"+curPath+"'");
		  if (curPath.equals(startPoint))
		  {
			  // OK, we're in the right place
			  searchForStartPoint = false;
		  }
		  else
		  {
			  String curComponent = startPoint.getComponent(curPath.getNumComponents());
			  if (toProcess.contains(curComponent))
			  {
				  if (startPoint.getNumComponents() > curPath.getNumComponents() + 1)
				  {
					  // We process files before processing directories.  If we need to go deeper into the tree, then we
					  // should have already processed all the files.  Run through, find everything that's a file and trash it
					  Iterator<String> iterator = toProcess.iterator();
					  while(iterator.hasNext())
					  {
						  String curName = iterator.next();
						  FileLike curFile = curDirectory.getChild(curName);
						  if (!curFile.isDirectory())
						  {
							  iterator.remove();	// Not a directory, we should have already processed it
						  }
					  }
				  }
				  Iterator<String> iterator = toProcess.iterator();
				  while (iterator.hasNext())
				  {
					  if (curComponent.equals(iterator.next()))
						  break;
					  iterator.remove();
				  }
				  // TODO- fix FilePath so we can get a child and compare properly
				  if (curPath.getNumComponents() + 1 == startPoint.getNumComponents())
					  searchForStartPoint = false;
			  }
			  else
			  {
				  // Hmmm - our start point cannot be found.  Probably something was removed
				  // Let's throw an exception and restart
			  }
		  }
	  }
	  boolean recheckedOnce = false;
	  while (toProcess.size()>0)
	  {
		  if (curDirectory == null)
			  continue;
		  if (toProcess == null)
			  continue;
		  String curName = toProcess.get(0);
		  if (curName == null)
			  continue;
		  FileLike curFile = curDirectory.getChild(curName);
		  pauser.checkPauseAndAbort();
		  try
		  {
			  if (curFile != null)
			  {
				  if (curFile.isMountPoint())
				  {
					  // handle a mount point (only really applies on Unix and unix-like system - or, everything except Windows)
					  if (shouldHandle(curFile))
						  handleMountPoint(curFile, pauser);
					  processed.add(curName);
				  }
				  else
				  {	
					  if (curFile.isDirectory())
					  {
						  directories.add(curName);
					  }
					  else
					  {
						  if (shouldHandle(curFile))
							  handleFile(curFile, pauser);
						  processed.add(curName);
					  }
				  }
			  }
		  }
		  catch (IOException e)
		  {
			  logger.error("Got IOException processing "+curFile, e);
		  }
		  finally
		  {
			  toProcess.remove(0);
		  }

		  if (curDirectory.lastModified() != curDirectoryModTime)
			  /* Handle an update to the directory */
		  {
			  if (recheckedOnce)
			  {
				  if (shouldntChangeTooMuch)
					  logger.warn(new WarnLogMessage("Directory {0} was updated multiple times while scanning - ignoring new changes",
							  new Serializable []{curDirectory.getAbsolutePath()}));
			  }
			  else
			  {
				  recheckedOnce = true;
				  fileNames = curDirectory.list();
				  directories.clear();
				  if (fileNames != null)
				  {
					  for (int curFileNum = 0; curFileNum < fileNames.length; curFileNum++)
					  {
						  if (!toProcess.contains(fileNames[curFileNum]) &&
								  !processed.contains(fileNames[curFileNum]))
						  {
							  toProcess.add(fileNames[curFileNum]);
						  }
					  }
				  }
				  else
				  {
					  logger.warn(new WarnLogMessage("{0} changed from directory to file while scanning, continuing", new Serializable[]{curDirectory.getFilePath()}));
				  }
			  }
			  curDirectoryModTime = curDirectory.lastModified();
		  }
	  }
	  while (directories.size() > 0)
	  {
		  String curName = directories.get(0);
		  if (curName == null)
			  continue;
		  FileLike curFile = curDirectory.getChild(curName);
		  pauser.checkPauseAndAbort();
		  try
		  {
			  if (curFile != null)
			  {
				  try
				  {
					  if (curFile.isMountPoint())
					  {
						  // handle a mount point (only really applies on Unix and unix-like system - or, everything except Windows)
						  if (shouldHandle(curFile))
							  handleMountPoint(curFile, pauser);
						  processed.add(curName);
					  }
					  else
					  {	
						  if (curFile.isDirectory())
						  {
							  if (!searchForStartPoint)
							  {	
								  preprocessDirectory(curFile, pauser);
								  if (shouldHandle(curFile))
									  handleFile(curFile, pauser);
							  }
							  if (shouldHandleDirectory(curFile))
								  recurseDirectory(curFile, pauser);
							  postprocessDirectory(curFile, pauser);
							  processed.add(curName);
						  }
						  else
						  {
							  // We still handle files even though we should only have directories at this point because it's possible
							  // that a directory was changed into a file while we were scanning or that the directory was changed
							  // in which case we reload everything
							  if (shouldHandle(curFile))
								  handleFile(curFile, pauser);
							  processed.add(curName);
						  }
					  }
				  }
				  finally
				  {
					  directories.remove(0);
				  }
			  }
		  }
		  catch (IOException e)
		  {
			  logger.error("Got IOException processing "+curFile, e);
		  }

		  if (curDirectory.lastModified() != curDirectoryModTime)
			  /* Handle an update to the directory */
		  {
			  if (recheckedOnce)
			  {
				  if (shouldntChangeTooMuch)
					  logger.warn(new WarnLogMessage("Directory {0} was updated multiple times while scanning - ignoring new changes",
							  new Serializable []{curDirectory.getAbsolutePath()}));
			  }
			  else
			  {
				  recheckedOnce = true;
				  fileNames = curDirectory.list();
				  directories.clear();
				  if (fileNames != null)
				  {
					  for (int curFileNum = 0; curFileNum < fileNames.length; curFileNum++)
					  {
						  if (!toProcess.contains(fileNames[curFileNum]) &&
								  !processed.contains(fileNames[curFileNum]))
						  {
							  directories.add(fileNames[curFileNum]);
						  }
					  }
				  }
				  else
				  {
					  logger.warn(new WarnLogMessage("{0} changed from directory to file while scanning, continuing", new Serializable[]{curDirectory.getFilePath()}));
				  }
			  }
			  curDirectoryModTime = curDirectory.lastModified();
		  }
	  }
  }
  
  public void addIncludeRule(Rule addRule)
  {
  	if (includeRulesList == null)
  		includeRulesList = new ArrayList<Rule>();
  	includeRulesList.add(addRule);
  }
  
  public void addExcludeRule(Rule addRule)
  {
  	if (excludeRulesList == null)
  		excludeRulesList = new ArrayList<Rule>();
  	excludeRulesList.add(addRule);
  }
  
  public void setExcludeRules(Rule [] inExcludeRules)
  {
  	excludeRules = null;
  	excludeRulesList = new ArrayList<Rule>();
  	if (inExcludeRules != null)
  	{	
  		for (int curExcludeRuleNum = 0; curExcludeRuleNum < inExcludeRules.length; curExcludeRuleNum++)
  		{
  			Rule curExcludeRule = inExcludeRules[curExcludeRuleNum];
  			curExcludeRule.init();
  			excludeRulesList.add(curExcludeRule);
  		}
  	}
  }
  
  public void setIncludeRules(Rule [] inIncludeRules)
  {
	includeRules = null;
	includeRulesList = new ArrayList<Rule>();
	if (inIncludeRules != null)
	{	
		for (int curIncludeRuleNum = 0; curIncludeRuleNum < inIncludeRules.length; curIncludeRuleNum++)
		{
			Rule curIncludeRule = inIncludeRules[curIncludeRuleNum];
			curIncludeRule.init();
			includeRulesList.add(curIncludeRule);
		}
	}

  }
  protected boolean shouldHandle(FileLike checkFile)
  {
  	// First check to see if this file should be included
  	// If there's no include rules or if there are include rules 
  	// and it matches, then check to see if it's excluded
  	if (includeRules == null || includeRules.length == 0 || checkRules(includeRules, checkFile))
  	{
  		if (excludeRules != null && checkRules(excludeRules, checkFile))
  			return false;
  		return true;
  	}
  	return false; // Not in the include list
  }
  
  protected boolean checkRules(Rule [] rules, FileLike checkFile)
  {
  	for (int curRuleNum = 0; curRuleNum < rules.length; curRuleNum++)
  	{
  		if (rules[curRuleNum].matchesRule(checkFile) != RuleMatch.kNoMatch)
  			return true;
  	}
  	return false;
  }
  
  protected boolean shouldHandleDirectory(FileLike checkFile)
  {
    // First check to see if this file should be included
    // If there's no include rules or if there are include rules 
    // and it matches, then check to see if it's excluded
    //if (includeRules == null || checkRulesForDirectory(includeRules, checkFile))
    //{
        if (excludeRules != null && checkRulesForDirectory(excludeRules, checkFile))
            return false;
        return true;
    //}
    //return false; // Not in the include list
  }
  
  protected boolean checkRulesForDirectory(Rule [] rules, FileLike checkFile)
  {
    for (int curRuleNum = 0; curRuleNum < rules.length; curRuleNum++)
    {
        if (rules[curRuleNum].matchesRule(checkFile) == RuleMatch.kSubdirsMatch)
            return true;
    }
    return false;
  }
  
  public abstract void preprocessDirectory(FileLike curDirectory, PauserControlleeIF pauser)
  	throws AbortedException, IOException;
  	
  public abstract void handleFile(FileLike curFile, PauserControlleeIF pauser)
      throws AbortedException, IOException;


  public abstract void postprocessDirectory(FileLike curDirectory, PauserControlleeIF pauser)
      throws AbortedException, IOException;

  public abstract void handleMountPoint(FileLike mountPoint, PauserControlleeIF pauser)
      throws AbortedException, IOException;

}
