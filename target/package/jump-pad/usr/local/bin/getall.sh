#!/bin/sh
export ETCDCTL_API=3
etcdctl get / --from-key --endpoints=http://etcd-client:2379
