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


which ./bin/gojsonschema >/dev/null || go build -o ./bin/gojsonschema github.com/atombender/go-jsonschema/cmd/gojsonschema

echo "Generating specification types"

./bin/gojsonschema -o generated.types_spec.go -p serverlessworkflow ../../specification/schema/workflow.json

# fix interface{} types
# this is a quick and dirty method to adapt the types to Kubernetes format
# we should write a better parse to handle this and/or add metadata info to the schema and suggest a PR to the tool to handle it
sed -i '8 i\import "k8s.io/apimachinery/pkg/runtime"' generated.types_spec.go
sed -i 's/Extensions \[\]WorkflowExtensionsElem/Extensions []runtime.RawExtension/g' generated.types_spec.go
sed -i 's/Metadata map\[string\]interface{}/Metadata map\[string\]string/g' generated.types_spec.go
sed -i 's/States \[\]interface{}/States []runtime.RawExtension/g' generated.types_spec.go
sed -i 's/\[\]interface{}{/\[\]string{/g' generated.types_spec.go
sed -i 's/\[\]interface{}/runtime.RawExtension/g' generated.types_spec.go
sed -i 's/ interface{}/ runtime.RawExtension/g' generated.types_spec.go
sed -i 's/var raw map\[string\]interface{}/var raw placeholder/g' generated.types_spec.go
sed -i 's/map\[string\]interface{}/runtime.RawExtension/g' generated.types_spec.go
sed -i 's/var raw placeholder/var raw map[string]interface{}/g' generated.types_spec.go
sed -i '/runtime.RawExtension/i \/\/ \+kubebuilder:pruning:PreserveUnknownFields' generated.types_spec.go

mv -v generated.types_spec.go ./pkg/apis/serverlessworkflow/

go fmt ./pkg/...

sh ./hack/run-controller-gen.sh