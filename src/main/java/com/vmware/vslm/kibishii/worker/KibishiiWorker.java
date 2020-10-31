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
package com.vmware.vslm.kibishii.worker;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.vmware.vslm.kibishii.core.ExecutionThread;
import com.vmware.vslm.kibishii.core.GeneratorThread;
import com.vmware.vslm.kibishii.core.Results;
import com.vmware.vslm.kibishii.core.VerifierThread;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Util;
import io.etcd.jetcd.Watch.Listener;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;

public class KibishiiWorker {

	private static final String KIBISHII_OPS_PREFIX = "/kibishii/ops/";
	private static final String KIBISHII_CONTROL_NODE_KEY = "/kibishii/control";
	private static final String KIBISHII_NODES_PREFIX = "/kibishii/nodes/";

	public static void main(String[] args) {
		String [] endpoints = new String[] {args[1]};
		for (int i = 2; i < args.length; i++) {
			File root = new File(args[i]);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						new KibishiiWorker(args[0], endpoints, root);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}

	private final String nodeID;
	private final Client client;
	private final long leaseID;
	private int leaseSecs = 10;
	private long leaseMS = leaseSecs * 1000;
	private String nodeKey, controlKey, resultsKey, statusKey;
	private ExecutionThread executionThread;
	private File root;
	private Timer updateTimer;

	public KibishiiWorker(String nodeID, String [] endpoints, File root) throws InterruptedException, ExecutionException {
		this.nodeID = nodeID;
		if (nodeID.contains(",") || nodeID.contains("/"))
			throw new IllegalArgumentException("nodeID may not contain commas or slashes");
		this.root = root;
		client = Client.builder().endpoints(Util.toURIs(Arrays.asList(endpoints))).build();
		leaseID = client.getLeaseClient().grant(leaseSecs).get().getID();

		client.getLeaseClient().keepAlive(leaseID, new StreamObserver<LeaseKeepAliveResponse>() {

			@Override
			public void onNext(LeaseKeepAliveResponse value) {
				//System.out.println("On next called, value = " + value);
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("onError called");
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				System.out.println("onCompleted called");
			}
		});
		int retries = 3;
		boolean succeeded = false;
		nodeKey = KIBISHII_NODES_PREFIX + nodeID + root.getName();;
		controlKey = KIBISHII_CONTROL_NODE_KEY;
		while (retries > 0) {
			Txn checkTxn = client.getKVClient().txn();
			ByteSequence key = toBS(nodeKey);
			ByteSequence value = toBS(nodeID);
			CompletableFuture<TxnResponse> resFuture = checkTxn
					.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.createRevision(0)))
					.Then(io.etcd.jetcd.op.Op.put(key, value, PutOption.newBuilder().withLeaseId(leaseID).build()))
					.commit();
			TxnResponse res = resFuture.get();
			System.out.println("txn completed");
			if (res.getPutResponses().size() > 0) {
				System.out.println("Succeded, we are owner for " + nodeID);
				succeeded = true;
				break;
			} else {
				System.out.println("Failed to create key, another owner present for " + nodeID);
				Thread.sleep(leaseMS);
			}
			retries--;
		}
		client.getWatchClient().watch(toBS(controlKey), new Listener() {

			@Override
			public void onNext(WatchResponse response) {
				System.out.println("On next called, response = " + response);
				for (WatchEvent curEvent:response.getEvents()) {
					if (curEvent.getEventType() == EventType.PUT) {
						controlNodeUpdated(nodeID, root);
					}
				}
			}

			@Override
			public void onError(Throwable t) {
				System.out.println("onError called");
				t.printStackTrace();
			}

			@Override
			public void onCompleted() {
				System.out.println("onCompleted called");
			}
		});
	}


	private void controlNodeUpdated(String nodeID, File root) {
		try {
			GetResponse getResponse = client.getKVClient().get(toBS(controlKey)).get();
			String jsonValue = getResponse.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8);
			JSONParser parser = new JSONParser();
			System.out.println("Parsing " + jsonValue);
			JSONObject responseObject = (JSONObject) parser.parse(jsonValue);
			String cmd = (String) responseObject.get("cmd");
			if (cmd != null) {
				if (cmd.equals("generate") || cmd.equals("verify")) {
					int opID = getInteger(responseObject, "opID");
					int levels = getInteger(responseObject, "levels");
					int dirsPerLevel = getInteger(responseObject, "dirsPerLevel");
					int filesPerLevel = getInteger(responseObject, "filesPerLevel");
					long fileLength = getLong(responseObject, "fileLength");
					int blockSize = getInteger(responseObject, "blockSize");
					int passNum = getInteger(responseObject, "passNum");
					System.out.println("Generate started, root = " + root +
							", levels = " + levels +
							", dirsPerLevel = " + dirsPerLevel +
							", filesPerLevel = " + filesPerLevel +
							", fileLength = " + fileLength +
							", blockSize = " + blockSize +
							", passNum = " + passNum);
					resultsKey = "/kibishii/results/"+opID+"/"+nodeID;
					statusKey = "/kibishii/status/"+opID+"/"+nodeID;
					createExecutionNode(opID);
					if (cmd.equals("generate")) {
						executionThread = new GeneratorThread(root, levels, dirsPerLevel,
								filesPerLevel, fileLength, blockSize, passNum);
					}
					if (cmd.equals("verify")) {
						executionThread = new VerifierThread(root, levels, dirsPerLevel,
								filesPerLevel, fileLength, blockSize, passNum);
					}
					executionThread.start();
					updateTimer = new Timer();
					updateTimer.scheduleAtFixedRate(new TimerTask() {

						@Override
						public void run() {
							updateStatus();
							if (executionThread.getResults().isCompleted())
								updateTimer.cancel();
						}
					}, new Date(), 10000L);

					executionThread.getFuture().handle((results, t)->{
						if (results == null)
							results = KibishiiWorker.this.executionThread.getResults();
						client.getKVClient().put(toBS(resultsKey),
								toBS(results.toJSON().toString()));
						try {
							updateCompletion(opID, results.getError() == null);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					});
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void updateStatus() {
		client.getKVClient().put(toBS(statusKey),
				toBS(executionThread.getResults().toJSON().toString()));
	}

	private int getInteger(JSONObject responseObject, String key) throws ParseException {
		String stringValue = (String)responseObject.get(key);
		if (stringValue == null)
			throw new ParseException(1);
		return Integer.parseInt(stringValue);
	}

	private long getLong(JSONObject responseObject, String key) throws ParseException {
		String stringValue = (String)responseObject.get(key);
		if (stringValue == null)
			throw new ParseException(1);
		return Long.parseLong(stringValue);
	}

	private void createExecutionNode(int opID) throws InterruptedException, ExecutionException {
		int nodes;
		List<KeyValue>nodeList = client.getKVClient().get(toBS(KIBISHII_NODES_PREFIX),
				GetOption.newBuilder().withPrefix(toBS(KIBISHII_NODES_PREFIX)).build()).get().getKvs();
		nodes = nodeList.size();
		String completionNodeKey = KIBISHII_OPS_PREFIX + opID;
		Txn checkTxn = client.getKVClient().txn();
		ByteSequence key = toBS(completionNodeKey);
		JSONObject completionJSON = new JSONObject();
		completionJSON.put("nodesStarting", Integer.toString(nodes));
		completionJSON.put("nodesCompleted", "0");
		completionJSON.put("nodesSuccessful", "");
		completionJSON.put("nodesFailed", "");
		completionJSON.put("status", "running");

		ByteSequence value = toBS(completionJSON.toJSONString());

		// Only insert if we're the first one to insert something
		CompletableFuture<TxnResponse> resFuture = checkTxn
				.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.createRevision(0)))
				.Then(io.etcd.jetcd.op.Op.put(key, value, PutOption.newBuilder().withLeaseId(leaseID).build()))
				.commit();
		resFuture.get();	// We actually don't care if it succeeds, if it fails it means someone else
							// succeeded
		return;
	}

	/*
	 * When we complete, we will lock the overall completion node, add 1 to the completion count,
	 * count how many node are registered and compare that to the completion count.  If the completion
	 * count == number of nodes registered, we will set the operation as completed
	 */
	private void updateCompletion(int opID, boolean success) throws InterruptedException, ExecutionException, ParseException {
		String completionNodeKey = KIBISHII_OPS_PREFIX + opID;
		boolean updated = false;
		while (!updated) {
			GetResponse res = client.getKVClient().get(toBS(completionNodeKey)).get();
			KeyValue completionNodeKV = res.getKvs().get(0);
			JSONParser parser = new JSONParser();
			JSONObject completionValue = (JSONObject)parser.parse(completionNodeKV.getValue().toString(StandardCharsets.UTF_8));
			int nodesStarting = Integer.parseInt((String)completionValue.get("nodesStarting"));
			int nodesCompleted = Integer.parseInt((String)completionValue.get("nodesCompleted"));

			String successNodesStr = (String)completionValue.get("nodesSuccessful");
			String failedNodesStr = (String)completionValue.get("nodesFailed");
			nodesCompleted ++;
			completionValue.put("nodesCompleted", Integer.toString(nodesCompleted));

			if (success) {
				if (successNodesStr.length() > 0)
					successNodesStr += ",";
				successNodesStr += nodeID;
				completionValue.put("nodesSuccessful", successNodesStr);

			} else {
				if (failedNodesStr.length() > 0)
					failedNodesStr += ",";
				failedNodesStr += nodeID;
				completionValue.put("nodesFailed", failedNodesStr);
			}
			if (nodesCompleted == nodesStarting) {
				if (failedNodesStr.length() > 0) {
					completionValue.put("status", "failed");
				} else {
					completionValue.put("status", "success");
				}
			}
			Txn updateTxn = client.getKVClient().txn();
			ByteSequence key = toBS(completionNodeKey);
			ByteSequence value = toBS(completionValue.toJSONString());

			// We attempt to update the key, but only if the mod revision is the same
			// as the one we got, if not, there was a collision and we will retrieve
			// and update again
			CompletableFuture<TxnResponse> insertFuture = updateTxn
					.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.modRevision(completionNodeKV.getModRevision())))
					.Then(io.etcd.jetcd.op.Op.put(key, value, PutOption.newBuilder().build()))
					.commit();
			TxnResponse insertRes = insertFuture.get();
			System.out.println("txn completed");
			if (insertRes.getPutResponses().size() > 0) {
				System.out.println("Succeded, updated completion");
				updated = true;
				break;
			} else {
				System.out.println("Collided, sleeping before retry of completion update");
				Thread.sleep(100);
			}
		}
	}
	private static ByteSequence toBS(String string) {
		return ByteSequence.from(string, StandardCharsets.UTF_8);
	}

	private String endKey(String key) {
		String endKey;
		if (key.length() > 1)
			endKey = key.substring(0, key.length() - 2);
		key += String.valueOf(key.charAt(key.length() - 1) + 1);
		return key;
	}
}
