/*
Copyright 2020 the Distributed Data Generator contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.vmware.vslm.kibishii.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.json.simple.JSONObject;

public class VerifierResults extends Results {
	private ArrayList<File> verifiedDirs = new ArrayList<File>();
	private ArrayList<File> missingDirs = new ArrayList<File>();
	private ArrayList<File> verifiedFiles = new ArrayList<File>();
	private ArrayList<File> missingFiles = new ArrayList<File>();
	private ArrayList<File> corruptedFiles = new ArrayList<File>();
	public VerifierResults() {
		completed = false;
	}
	
	public void addVerifiedDir(File verifiedFile) {
		verifiedDirs.add(verifiedFile);
	}
	
	public void addMissingDir(File missingDir) {
		missingDirs.add(missingDir);
	}
	
	public void addVerifiedFile(File verifiedFile) {
		verifiedFiles.add(verifiedFile);
	}
	
	public void addMissingFile(File missingFile) {
		missingFiles.add(missingFile);
	}
	
	public void addCorruptedFile(File corruptedFile) {
		corruptedFiles.add(corruptedFile);
	}

	public ArrayList<File> getVerifiedDirs() {
		return verifiedDirs;
	}

	public ArrayList<File> getMissingDirs() {
		return missingDirs;
	}

	public ArrayList<File> getVerifiedFiles() {
		return verifiedFiles;
	}

	public ArrayList<File> getMissingFiles() {
		return missingFiles;
	}

	public ArrayList<File> getCorruptedFiles() {
		return corruptedFiles;
	}
	
	public int getCheckedFilesAndDirs() {
		return verifiedDirs.size() + missingDirs.size() + verifiedFiles.size() + 
				missingFiles.size() + corruptedFiles.size();
	}
	
	public void setCompleted() {
		completed = true;
		completedTime = new Date();
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public String toString() {
		String resultStr = "Total checked files and dirs = "+getCheckedFilesAndDirs() + 
				", total verified dirs = " + verifiedDirs.size() + 
				", total missing dirs = " + missingDirs.size() +
				", total verified files = " + verifiedFiles.size() +
				", total missing files = " + missingFiles.size() +
				", total corrupted files = " + corruptedFiles.size();
		if (completed) {
			resultStr += " completed at " + completedTime;
		} else {
			resultStr += " not completed";
		}
		return resultStr;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject retJSON = new JSONObject();
		retJSON.put("missingFiles", missingFiles.size());
		retJSON.put("corruptedFiles", corruptedFiles.size());
		retJSON.put("verifiedFiles", verifiedFiles.size());
		retJSON.put("verifiedDirs", verifiedDirs.size());
		retJSON.put("missingDirs", missingDirs.size());
		addCompletionAndErrorInfo(retJSON);
		return retJSON;
	}
}
