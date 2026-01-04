#!/bin/bash

# Script to create Kubernetes secrets from .env file
# Usage: ./create-secrets.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$(dirname "$(dirname "$SCRIPT_DIR")")")"
ENV_FILE="$PROJECT_ROOT/.env"
SECRETS_FILE="$PROJECT_ROOT/gitops/base/secrets.yaml"

echo "🔐 Creating Kubernetes secrets from .env file..."

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Error: .env file not found at $ENV_FILE"
    echo "Please create a .env file with your configuration"
    exit 1
fi

# Source the .env file
set -a
source "$ENV_FILE"
set +a

# Function to base64 encode
b64encode() {
    echo -n "$1" | base64
}

# Create secrets.yaml
cat > "$SECRETS_FILE" << EOF
apiVersion: v1
kind: Secret
metadata:
  name: carsharing-secrets
  namespace: carsharing
type: Opaque
data:
  MONGODB_URI: $(b64encode "$MONGODB_URI")
  JWT_SECRET_KEY: $(b64encode "$JWT_SECRET_KEY")
  GOOGLE_API_KEY: $(b64encode "$GOOGLE_API_KEY")
  FIREBASE_CONFIG_PATH: $(b64encode "${FIREBASE_CONFIG_PATH:-}")
  FIREBASE_DATABASE_URL: $(b64encode "${FIREBASE_DATABASE_URL:-}")
EOF

echo "✅ Secrets file created at $SECRETS_FILE"
echo ""
echo "⚠️  WARNING: This file contains sensitive data!"
echo "   - Do NOT commit this file to git"
echo "   - It's already added to .gitignore"
echo ""
echo "To apply secrets to your cluster:"
echo "   ./gitops/gitops.sh apply local"
