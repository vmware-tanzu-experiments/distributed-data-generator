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

import org.json.simple.JSONObject;

public class GeneratorResults extends Results {
	private int filesGenerated;
	private int dirsGenerated;
	public void addGeneratedFile(File generatedFile) {
		filesGenerated++;
	}
	
	public void addGeneratedDir(File generatedDir) {
		dirsGenerated++;
	}
	public int getFilesGenerated() {
		return filesGenerated;
	}
	public int getDirsGenerated() {
		return dirsGenerated;
	}
	
	public int getGeneratedFilesAndDirs() {
		return filesGenerated + dirsGenerated;
	}
	
	public String toString() {
		String resultStr = "Total generated files and dirs = "+getGeneratedFilesAndDirs() + 
				", total generated dirs = " + getDirsGenerated() + 
				", total generated files = " + getFilesGenerated();
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
		retJSON.put("generatedFiles", getFilesGenerated());
		retJSON.put("generatedDirs", getDirsGenerated());
		addCompletionAndErrorInfo(retJSON);
		return retJSON;
	}
}
