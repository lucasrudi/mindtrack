#!/usr/bin/env bash
set -euo pipefail

INFRA_DIR="$(cd "$(dirname "$0")/../.." && pwd)"

echo "=== Terraform Unit Tests ==="

echo ">> Checking format..."
terraform -chdir="$INFRA_DIR" fmt -check -recursive

echo ">> Initializing (no backend)..."
terraform -chdir="$INFRA_DIR" init -backend=false

echo ">> Validating..."
terraform -chdir="$INFRA_DIR" validate

echo ">> Initializing github-settings (no backend)..."
terraform -chdir="$INFRA_DIR/github-settings" init -backend=false

echo ">> Validating github-settings..."
terraform -chdir="$INFRA_DIR/github-settings" validate

if command -v tflint &> /dev/null; then
    echo ">> Initializing tflint plugins..."
    tflint --init --chdir="$INFRA_DIR"
    tflint --init --chdir="$INFRA_DIR/github-settings"

    echo ">> Running tflint..."
    tflint --recursive --minimum-failure-severity=error --chdir="$INFRA_DIR"
else
    echo ">> tflint not installed, skipping."
fi

if command -v tfsec &> /dev/null; then
    echo ">> Running tfsec..."
    tfsec "$INFRA_DIR/"
else
    echo ">> tfsec not installed, skipping."
fi

echo "=== All Terraform unit tests passed ==="
