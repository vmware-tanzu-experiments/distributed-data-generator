# Copyright 2019 VMware, Inc..
# SPDX-License-Identifier: Apache-2.0
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# VERSION is <git branch>-<git commit>-<date
# Uses ifndef instead of ?= so that date will only be evaluated once, not each time VERSION is used
ifndef VERSION
VERSION := $(shell echo `git rev-parse --abbrev-ref HEAD`-`git log -1 --pretty=format:%h`-`date "+%d.%b.%Y.%H.%M.%S"`)
endif

# Please reference links below to log in to gcloud before pushing image to GCR
# https://cloud.google.com/container-registry/docs/advanced-authentication#gcloud-helper
# https://cloud.google.com/iam/docs/creating-managing-service-account-keys#iam-service-account-keys-create-console
REGISTRY ?= gcr.io/velero-gcp

JUMP_PAD_IMAGE = $(REGISTRY)/jump-pad
WORKER_IMAGE = $(REGISTRY)/kibishii-worker

target: FORCE
	mvn clean install

FORCE:

push: push-jump-pad-container push-worker-container

containers: jump-pad-container worker-container

jump-pad-container: target
	docker build -t $(JUMP_PAD_IMAGE):$(VERSION) target/package/jump-pad

push-jump-pad-container: jump-pad-container
	docker push $(JUMP_PAD_IMAGE):$(VERSION)

worker-container: target
	docker build -t $(WORKER_IMAGE):$(VERSION) target/package/worker

push-worker-container: worker-container
	docker push $(WORKER_IMAGE):$(VERSION)


