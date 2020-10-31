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


package com.vmware.vslm.kibishii.cmd;

import java.io.File;
import java.util.concurrent.ExecutionException;

import com.vmware.vslm.kibishii.core.ExecutionThread;
import com.vmware.vslm.kibishii.core.GeneratorThread;
import com.vmware.vslm.kibishii.core.VerifierThread;

public class KibishiiCmdLine {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		File rootDir = new File("/tmp/kibishii-test");
		generate(rootDir);
		verify(rootDir);
	}

	private static void generate(File rootDir) throws InterruptedException, ExecutionException {
		rootDir.mkdir();
		long startTime = System.currentTimeMillis();
		ExecutionThread generator = new GeneratorThread(rootDir, 1, 1, 1, 128*1024 * 1024, 16*1024*1024, 0);
		generator.start();
		generator.getFuture().get();
		long endTime = System.currentTimeMillis();
		System.out.println("Generated 128M in "+(endTime - startTime) + " ms");
	}

	private static void verify(File rootDir) {
		rootDir.mkdir();
		VerifierThread verifier = new VerifierThread(rootDir, 1, 1, 1, 128 * 1024 * 1024, 16*1024*1024, 0);
		verifier.start();
		try {
			System.out.println(verifier.getFuture().get());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
