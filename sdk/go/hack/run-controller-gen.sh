#!/bin/bash

set -e

which ./bin/controller-gen >/dev/null || go build -o ./bin/controller-gen sigs.k8s.io/controller-tools/cmd/controller-gen

./bin/controller-gen object paths=./pkg/apis/serverlessworkflow