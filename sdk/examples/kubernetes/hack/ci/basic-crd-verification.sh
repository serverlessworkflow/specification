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
EXIT=0
FILE_FOUND=0

if [ -z "${NAMESPACE}" ]; then
  NAMESPACE="default"
fi

shopt -s nullglob
for file in sdk/examples/kubernetes/deploy/crds/{*_crd.yaml,*_cr.yaml}; do
  FILE_FOUND=1
  # see here why we need the flag --validate=false: https://github.com/kubernetes/kubernetes/issues/88252
  if ! kubectl apply -f "$file" -n "${NAMESPACE}" --validate=false; then
    EXIT=1
    break # Don't try other files if one fails
  fi
done
shopt -u nullglob

if [[ FILE_FOUND -eq 0 ]]; then
  echo "No CRDs/CRs file found" >&2
  EXIT=3
fi

exit ${EXIT}