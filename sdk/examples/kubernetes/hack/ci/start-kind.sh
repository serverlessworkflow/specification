#!/bin/bash

set -e

if [ -z "${KUBE_VERSION}" ]; then
    KIND_KUBE_VERSION=""
else
    KIND_KUBE_VERSION="--image kindest/node:${KUBE_VERSION}"
fi

file kind

if [[ ! "$(kind get clusters)" =~ ${CLUSTER_NAME} ]]; then
    echo "---> Creating cluster named '${CLUSTER_NAME}'"
    kind create cluster --name "${CLUSTER_NAME}" --wait 1m $KIND_KUBE_VERSION
else
    echo "---> Already found cluster named '${CLUSTER_NAME}'"
fi

echo "---> Checking KIND cluster conditions"
kubectl get nodes -o wide
kubectl get pods --all-namespaces -o wide