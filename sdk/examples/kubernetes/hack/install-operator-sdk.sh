#!/bin/bash

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

