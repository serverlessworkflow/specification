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


mkdir -p ./bin/

if [[ -z ${OPERATOR_SDK_VERSION} ]]; then
    OPERATOR_SDK_VERSION=0.18.2
fi

if [ ! -f ./bin/operator-sdk ]; then
    echo "Installing Operator SDK version ${OPERATOR_SDK_VERSION}"
    curl -LO https://github.com/operator-framework/operator-sdk/releases/download/v${OPERATOR_SDK_VERSION}/operator-sdk-v${OPERATOR_SDK_VERSION}-x86_64-linux-gnu

    chmod +x "${PWD}"/operator-sdk-v${OPERATOR_SDK_VERSION}-x86_64-linux-gnu &&
        mkdir -p ./bin &&
        cp "${PWD}"/operator-sdk-v${OPERATOR_SDK_VERSION}-x86_64-linux-gnu ./bin/operator-sdk &&
        rm "${PWD}"/operator-sdk-v${OPERATOR_SDK_VERSION}-x86_64-linux-gnu
else
    echo "Operator already installed, skipping"
fi

