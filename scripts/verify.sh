#!/bin/sh
OPID=`date +%s`
LEVELS=$1
DIRSPERLEVEL=$2
FILESPERLEVEL=$3
FILELENGTH=$4
BLOCKSIZE=$5
PASSNUM=$6
NODES=$7
NAMPSPACE=$8
export ETCDCTL_API=3
RUNNING_NODES=0

ret=1
i=0
while  [ $i -lt 60 ] && [ $RUNNING_NODES -lt $NODES ]
do
    sleep 10
    etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep /kibishii/nodes | wc -l
	RUNNING_NODES=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep /kibishii/nodes | wc -l` 
    ret=$?
    if [ $ret -ne 0 ]
    then
        RUNNING_NODES=0
    fi
    i=$((i+1))
    echo "Next round of getting kibishii node:$i"
done

if [ $RUNNING_NODES -lt $NODES ]
then
    exit $((100+1))
fi


echo "{\"opID\":\"$OPID\",\"cmd\":\"verify\",\"levels\":\"$LEVELS\",\"dirsPerLevel\":\"$DIRSPERLEVEL\",\"filesPerLevel\":\"$FILESPERLEVEL\",\"fileLength\":\"$FILELENGTH\",\"blockSize\":\"$BLOCKSIZE\",\"passNum\":\"$PASSNUM\"}" | etcdctl put /kibishii/control --endpoints=http://etcd-client:2379
STATUS="running"
i=0
echo ""
while  [ $i -lt 60 ] && { test -z "$STATUS" || [ "$STATUS" != "success" ]; }
do
	sleep 10
	STATUS=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".status" | sed -e 's/"//g'`
	ret=$?
    if [ $ret -eq 0 ] && [ "$STATUS" == "success" ]
    then
        break
    fi
    i=$((i+1))
    echo "Next round of get ops status: $i"
done

NODES_COMPLETED=0
ret=1
i=0
while  [ $i -lt 60 ] && { test -z "$NODES_COMPLETED" || [ "$NODES_COMPLETED" -ne "$NODES" ]; }  
do
    sleep 10
    NODES_COMPLETED=`etcdctl get /kibishii/ops/$OPID --endpoints=http://etcd-client:2379 --print-value-only | jq ".nodesCompleted" | sed -e 's/"//g'`
	ret=$?
    if [ $ret -eq 0 ] && [ "$NODES_COMPLETED" == "$NODES" ] 
    then
        break
    fi
    i=$((i+1))
    echo "Next round of get nodesCompleted: $i"
done

if [ "$NODES_COMPLETED" != "$NODES" ] 
then
	STATUS="failed_nodes"
fi
echo STATUS:$STATUS
if [ "$STATUS" = 'success' ]
then
    nodes=`etcdctl get /kibishii/nodes/ --prefix --endpoints=http://etcd-client:2379 | grep ^kibishii-deployment`
    echo "nodes"
    echo $nodes
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
	exit 0
fi

exit 1


