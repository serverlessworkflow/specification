#!/bin/bash

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