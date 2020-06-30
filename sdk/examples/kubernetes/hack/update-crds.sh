#!/bin/bash

source ./hack/install-operator-sdk.sh

# creating directory structure needed for the operator-sdk to update the CRD
mkdir -p ./cmd/manager
mkdir -p ./build
touch ./cmd/manager/main.go
touch ./build/Dockerfile

./bin/operator-sdk generate k8s --verbose
./bin/operator-sdk generate crds

source ./hack/run-openapi.sh

rm -rf ./cmd
rm -rf ./build