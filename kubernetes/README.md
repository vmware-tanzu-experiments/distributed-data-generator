# Running Kibishii on Kubernetes

# These instructions and yamls are obsolete and are in the process of being removed.  Please refer
to the yaml/README.md for how to install using Kustomize

## Create namespace for Kibishii
	kubectl create namespace kibishii
*The namespace does not have to be named kibishii.  If you want a different namespace, just be sure to change
all of the commands here to use that namespace*
## Create docker secret
Currently, the Kibishii container is stored in a private repository.  Use this command to create the credentials to
retrieve it.

	kubectl create secret docker-registry regcred --docker-server=http://index.docker.io/v1/ --docker-username=dsmithuchida --docker-password=dPrMq3Fg8czCn --docker-email=dave@igeekinc.com --namespace=kibishii
## Create the appropriate storage class
Kibishii requires a storage class to operate.  By default, the storage class is named *kibishii-storage-class*.  Example storage classes are provided.

|Provider|File|
|--------|----|
|AWS|kibishiiAWSStorageClass.yaml|
|vSphere CNS/CSI|kibishiiCNSStorageClass.yaml|
|vSphere CNS/CSP (obsolete)|kibishiiCNSCSPStorageClass.yaml|

To create the storage class use this command:
	kubectl apply --namespace kibishii -f <storageclass>.yaml

## Create the etcd pods
Kibishii uses a private etcd to co-ordinate the read/writers

	kubectl apply --namespace kibishii -f etcd.yaml

## Install kibishii
	
	kubectl apply --namespace kibishii -f kibishii.yaml
