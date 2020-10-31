# Distributed Data Generator "Kibishii" - distributed file system generation and verification tool

Kibishii (Japanese for "strict") is a tool for generating and verifying data, especially targeted at the cloud native space.
Distributed applications are being built that have multiple independent storage units.  Testing that the storage system and
data protection system are correctly protecting data can be difficult.  Testing with actual application data is a good final 
verification, but the sensitivity of applications to data corruption varies as well as the ability to diagnose the problem.  

Kibishii generates a synthetic workload that can be tightly defined and data corruption or loss is easily diagnosed.  It is
a full scale-out architecture that can simulate thousands of nodes creating and verifying data.
The data generated is in algorithmically generated and can be verified without storing a reference copy.

## General function
### Generation
Kibishii generates a tree of files.  The number of files/directories per level, number of levels and size of the files are 
specified.  Each file contains checked-summed data that can be verified as correct.

### Verification
Kibishii is given a tree and the parameters used to generate it.  It then descends the tree, verifying that the expected number of files and directory exist and that the data in the files is consistent.

## Kibishii on Kubernetes
Kibishii on Kubernetes runs as a namespace with an [etcd](https://etcd.io/) KV store for co-ordination and *N* Kibishii worker 
pods.  Kibishii has a generate and verify operation, both of these are triggered via commands stored into the etcd database.
During an operation all pods run independently, generating or verifying data.  Status is stored into etcd.

### Etcd layout

All Kibishii keys start with "/kibishii"

#### Node key
Each node will create an etcd tuple named "/kibishii/node/<node id>" which it starts.  It will apply a lease to the key
and if the node exits, the tuple will be automatically removed.  If the tuple already exists, the node will
refuse to start until the tuple is removed.

#### Control key
Nodes will watch the "/kibishii/control" key.  When it is updated, all nodes will execute the given command.
The value of the control key is a JSON with the following fields:

{
	"opID":"<op ID - unique for this operation, a date/time stamp is good>",
	"cmd":"<command to execute, current options are generate and verify>",
	"levels":"<number of levels of directory to generate or verify>",
	"dirsPerLevel":"<number of directories per level to generate or verify>",
	"filesPerLevel":"<number of files per level to generate or verify>",
	"fileLength":"<length of files to generate or verify>",
	"blockSize":"<blocksize to read/write>"
}

#### Op keys and results
When a node detects that an operation is started, it will count the number of nodes existing existing and attempt to insert
a JSON with a status JSON with key "/kibishii/ops/<op id> and the following fields:

{
	"nodesStarting":"<the number of existing nodes>,
	"nodesCompleted":"<the number of nodes that have completed>",
	"nodesSuccessful":"<list of nodes that completed successfully>",
	"nodesFailed":"<list of nodes that failed generation or verification>",
	"status":"<running, success, failure - success will only be set if all nodes completed successfully>"
}

If the key already exists it will not change it.

When each node completes, it will retrieve the status json, note the revision number of the node,
then update the node with its completion status.  If the number of failed + successful nodes
= the number of starting nodes, it will set the status to be success if no nodes are marked as
failed and failed otherwise.

## Kibishii command line
The Kibishii command line can be used to generate and verify Kibishii directory trees.

# Building
	mvn clean install

# Worker node container
	docker build target/package/worker

# Jump-pad container
The jump-pad is a container that is installed in the Kubernetes cluster to interact with the etcd database and control Kibishii

	docker build target/package/jump-pad
	
# Things to do
- Multiple volumes per node and verification that volumes have been attached to nodes properly
- Verification of consistency with snapshots taken during the Kibishii run
- Application aware interfaces so that Kibishii can flush buffers, etc.
- Scale up/down of nodes and verification that number of nodes restored matches number of nodes at time of generation
