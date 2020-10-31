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
import java.util.concurrent.CompletableFuture;

public abstract class ExecutionThread implements Runnable {
	protected Results results;
	protected CompletableFuture<Results> future = new CompletableFuture<Results>();
	protected final Thread thread;
	protected final File rootDir;
	protected final int levels;
	protected final int dirsPerLevel;
	protected final int filesPerLevel;
	protected final long fileLength;
	protected final int blockSize;
	protected final int passNum;

	public ExecutionThread(File rootDir, int levels, int dirsPerLevel, int filesPerLevel, long fileLength,
			int blockSize, int passNum) {
		this.rootDir = rootDir;
		thread = new Thread(this);
		this.levels = levels;
		this.dirsPerLevel = dirsPerLevel;
		this.filesPerLevel = filesPerLevel;
		this.fileLength = fileLength;
		this.blockSize = blockSize;
		this.passNum = passNum;
	}

	public CompletableFuture<Results> getFuture() {
		return future;
	}

	public Results getResults() {
		return results;
	}

	public void start() {
		thread.start();
	}
}
