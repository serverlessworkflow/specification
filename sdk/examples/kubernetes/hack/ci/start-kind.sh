#!/bin/bash
# Copyright 2020 The Serverless Workflow Specification Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


set -e

if [ -z "${KUBE_VERSION}" ]; then
    KIND_KUBE_VERSION=""
else
    KIND_KUBE_VERSION="--image kindest/node:${KUBE_VERSION}"
fi

file kind

if [[ ! "$(./kind get clusters)" =~ ${CLUSTER_NAME} ]]; then
    echo "---> Creating cluster named '${CLUSTER_NAME}'"
    ./kind create cluster --name "${CLUSTER_NAME}" --wait 1m $KIND_KUBE_VERSION
else
    echo "---> Already found cluster named '${CLUSTER_NAME}'"
fi

echo "---> Checking KIND cluster conditions"
kubectl get nodes -o wide
kubectl get pods --all-namespaces -o wide