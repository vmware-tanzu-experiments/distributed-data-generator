#!/bin/sh
OPID=`date +%s`
LEVELS=$1
DIRSPERLEVEL=$2
FILESPERLEVEL=$3
FILELENGTH=$4
BLOCKSIZE=$5
PASSNUM=$6
NODES=$7
export ETCDCTL_API=3
RUNNING_NODES=0
while [ $RUNNING_NODES -lt $NODES ]
do
	sleep 10
	RUNNING_NODES=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep /kibishii/nodes | wc -l`
done

echo "{\"opID\":\"$OPID\",\"cmd\":\"verify\",\"levels\":\"$LEVELS\",\"dirsPerLevel\":\"$DIRSPERLEVEL\",\"filesPerLevel\":\"$FILESPERLEVEL\",\"fileLength\":\"$FILELENGTH\",\"blockSize\":\"$BLOCKSIZE\",\"passNum\":\"$PASSNUM\"}" | etcdctl put /kibishii/control --endpoints=http://etcd-client:2379
STATUS="running"
while [ "$STATUS" = 'running' ]
do
	sleep 10
	STATUS=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".status" | sed -e 's/"//g'`
	NODES_COMPLETED=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".nodesCompleted" | sed -e 's/"//g'`
done
if [ "$NODES_COMPLETED" -ne "$NODES" ] 
then
	STATUS="failed"
fi
echo $STATUS
if [ "$STATUS" = 'success' ]
then
    nodes=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep ^kibishii-deployment`
    for i in $nodes
    do
         results=`etcdctl get /kibishii/results/1629946987/$i --endpoints=http://etcd-client:2379 --print-value-only | jq ".missingFiles"`
         echo "node [$i] results: $results"
         if [ "$results" !=  '0' ]; then
             echo "exit -1"
             exit 2
         fi
    done
	exit 0
fi

exit 1


