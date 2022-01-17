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
i=0
while ( [ "$STATUS" = 'running' ] && [ $i -le 36 ] )
do
    echo i:$i
	sleep 10
	STATUS=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".status" | sed -e 's/"//g'`
	NODES_COMPLETED=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".nodesCompleted" | sed -e 's/"//g'`
    NODES_STARTING=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".nodesStarting" | sed -e 's/"//g'`
    NODES_FAILED=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".nodesFailed" | sed -e 's/"//g'`
    RESULT=`etcdctl get /kibishii/results/ --prefix --endpoints=http://etcd-client:2379`
    echo RESULT:$RESULT
    NODE_LIST=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379`
    echo NODE_LIST:$NODE_LIST
    CTL=`etcdctl get /kibishii/control --endpoints=http://etcd-client:2379`
    echo CTL:$CTL
    OPS=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only`
    echo OPS:$OPS
    echo STATUS:$STATUS
    ((i++))
    date
done


if [ "$NODES_COMPLETED" != "$NODES" ]; then
	STATUS="failed"
    echo STATUS:$STATUS
    echo NODES_COMPLETED:$NODES_COMPLETED
    exit 80
fi
if [ "$NODES_STARTING" != "$NODES" ]; then
	STATUS="failed"
    echo STATUS:$STATUS
    echo NODES_STARTING:$NODES_STARTING
    exit 81
fi
if [ "$NODES_FAILED" != "" ]; then
	STATUS="failed"
    echo STATUS:$STATUS
    echo NODES_FAILED:$NODES_FAILED
    exit 82
fi

if [ "$STATUS" = 'success' ]
then
    nodes=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep ^kibishii-deployment`
    for node in $nodes
    do
        results=`etcdctl get /kibishii/results/$OPID/$node --endpoints=http://etcd-client:2379 --print-value-only | jq ".missingDirs,.missingFiles"`
        for result in $results
        do
            if [ -z $result ]; then
                exit 2
            fi
            if [ "$result" !=  '0' ]; then
                exit $result
            fi
        done
    done
    echo END_STATUS:$STATUS
    exit 0
fi
RESULT=`etcdctl get /kibishii/results/ --prefix --endpoints=http://etcd-client:2379`
echo RESULT:$RESULT
NODES=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379`
echo NODES:$NODES
CTL=`etcdctl get /kibishii/control --endpoints=http://etcd-client:2379`
echo CTL:$CTL
OPS=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only`
echo OPS:$OPS
echo END_ERR_STATUS:$STATUS
exit 1





