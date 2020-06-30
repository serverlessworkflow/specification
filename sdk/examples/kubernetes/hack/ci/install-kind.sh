#!/bin/bash

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
        ln -s "${kind_install_dir}"/kind kind
else
    echo "KIND already installed, skipping"
fi