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
default_kind_version="v0.8.1"
kind_install_dir="./sdk/examples/kubernetes/bin"

if [[ -z ${KIND_VERSION+x} ]]; then
    KIND_VERSION=$default_kind_version
    echo "Using default KIND version ${KIND_VERSION}"
fi

if [ ! -f "${kind_install_dir}"/kind ]; then
    echo "Installing KIND"
    curl -Lo ./kind https://github.com/kubernetes-sigs/kind/releases/download/${KIND_VERSION}/kind-linux-amd64
    chmod +x "${PWD}"/kind &&
        mkdir -p "${kind_install_dir}" &&
        mv -v "${PWD}"/kind "${kind_install_dir}"/kind
else
    echo "KIND already installed, skipping"
fi

ln -s "${kind_install_dir}"/kind kind