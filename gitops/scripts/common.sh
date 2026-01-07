#!/bin/bash

# Common utilities for GitOps scripts
# Source this file in other scripts: source "$(dirname "$0")/../common.sh"

# Service list (single source of truth)
ALL_SERVICES=(
    "auth-service"
    "user-service"
    "vehicle-service"
    "trip-service"
    "ride-service"
    "notification-service"
    "api-gateway"
)

# Find project root from any script location
# Usage: PROJECT_ROOT=$(get_project_root "$SCRIPT_DIR")
get_project_root() {
    local script_dir="$1"
    local depth="${2:-2}"  # Default depth is 2 (scripts are in gitops/scripts/*)
    
    local path="$script_dir"
    for ((i=0; i<depth; i++)); do
        path="$(dirname "$path")"
    done
    echo "$path"
}

# Extract value from ConfigMap YAML
# Usage: VALUE=$(get_configmap_value "KEY_NAME" "$PROJECT_ROOT")
get_configmap_value() {
    local key=$1
    local project_root=$2
    local configmap_file="$project_root/gitops/base/configmap.yaml"
    
    if [ -f "$configmap_file" ]; then
        grep "^  $key:" "$configmap_file" 2>/dev/null | sed 's/.*: "\(.*\)"/\1/' | tr -d '"'
    fi
}

# Print styled separator line
print_separator() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Check if a command exists, exit with error if not
# Usage: require_command "kubectl" "https://kubernetes.io/docs/tasks/tools/"
require_command() {
    local cmd=$1
    local install_url=$2
    if ! command -v "$cmd" &> /dev/null; then
        echo "❌ Error: $cmd is not installed"
        echo "   Install from: $install_url"
        exit 1
    fi
}

# Validate required environment variable or ConfigMap value
# Usage: validate_config "GCP_PROJECT_ID" "$VALUE" "gitops/base/configmap.yaml"
validate_config() {
    local name=$1
    local value=$2
    local source=$3
    
    if [ -z "$value" ]; then
        echo "❌ Error: $name not found"
        echo "   Set it in $source or export $name=your-value"
        exit 1
    fi
}
