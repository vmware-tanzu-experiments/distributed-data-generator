# Kustomize resources

These yamls can be merged together using the kubectl kustomize option.

# base
These are base yamls for Kibishii installation

## etcd.yaml
This yaml installs the etcd database that Kibishii uses to co-ordinate the various worker nodes.
It relies on the kibishii-storage-class being defined to allocate persistent volumes for storage.

## kibishii.yaml

This deploys the Kibishii application itself.  It relies on the kibishii-storage-class being defined 
to allocate persistent volumes for storage.   A stateful set is used for the Kibishii app, replicas controls the
number of worker nodes (default: 2) and storage controls the size of the PVs allocated (default: 1GB)

## jump-pad.yaml

The jump-pad contains scripts and is used to control Kibishii via CLI scripts.

# aws

This contains the Kibishii AWS storage class and kustomization.yaml.  Use kubectl kustomize aws to generate the 
yaml needed to deploy Kibishii in an AWS environment.

#vsphere
This contains the Kibishii vSphere storage class and kustomization.yaml.  Use kubectl kustomize vsphere to generate the 
yaml needed to deploy Kibishii in an vSphere environment.
