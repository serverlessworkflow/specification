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