#!/bin/sh
NAMESPACE=$1

kubectl create secret docker-registry regcred1 --docker-server=http://index.docker.io/v1/ --docker-username=dsmithuchida --docker-password=dPrMq3Fg8czCn --docker-email=dave@igeekinc.com --namespace="$NAMESPACE"

kubectl create secret docker-registry regcred --docker-server=http://index.docker.io/v1/ --docker-username=6002 --docker-password=ujwalahalambi --docker-email=ujwalahalambi@gmail.com --namespace="$NAMESPACE"
#kubectl apply --namespace "$NAMESPACE" -f kibishiiCNSStorageClass.yaml
kubectl apply --namespace "$NAMESPACE" -f kibishiiAWSStorageClass.yaml
kubectl apply --namespace kibishii -f etcd.yaml
kubectl apply --namespace kibishii -f jump-pad.yaml
kubectl apply --namespace kibishii -f kibishii-1g.yaml
ATTEMPTS=0
ROLLOUT_STATUS_CMD="kubectl rollout status statefulset.apps/kibishii-deployment -n $NAMESPACE"
until $ROLLOUT_STATUS_CMD || [ $ATTEMPTS -eq 60 ]; do
  $ROLLOUT_STATUS_CMD
  ATTEMPTS=$((attempts + 1))
  sleep 10
done
