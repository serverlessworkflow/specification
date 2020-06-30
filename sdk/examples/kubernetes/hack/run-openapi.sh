#!/bin/bash

which ./bin/openapi-gen >/dev/null || go build -o ./bin/openapi-gen k8s.io/kube-openapi/cmd/openapi-gen

echo "Generating Open API files"
./bin/openapi-gen --logtostderr=true -o "" -i ./pkg/apis/serverlessworkflow/v1alpha2 -O zz_generated.openapi -p ./pkg/apis/serverlessworkflow/v1alpha2 -h ./hack/license.go.txt -r "-"