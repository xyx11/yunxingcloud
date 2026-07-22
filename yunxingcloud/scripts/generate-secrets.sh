#!/bin/bash
# Generate K8s secrets for yunxingcloud deployment
# Usage: bash scripts/generate-secrets.sh [namespace]

set -euo pipefail

NAMESPACE="${1:-yunxingcloud}"

echo "=== yunxingcloud K8s Secret Generator ==="
echo "Namespace: ${NAMESPACE}"
echo ""

# Generate random secrets
DB_PASSWORD=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | head -c 24)
JWT_SECRET=$(openssl rand -base64 64 | tr -dc 'a-zA-Z0-9' | head -c 64)

# Base64 encode (macOS compatible)
if [[ "$(uname)" == "Darwin" ]]; then
  DB_PASSWORD_B64=$(echo -n "$DB_PASSWORD" | base64)
  JWT_SECRET_B64=$(echo -n "$JWT_SECRET" | base64)
else
  DB_PASSWORD_B64=$(echo -n "$DB_PASSWORD" | base64 -w0)
  JWT_SECRET_B64=$(echo -n "$JWT_SECRET" | base64 -w0)
fi

cat > k8s/secret.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: yunxingcloud-secrets
  namespace: ${NAMESPACE}
type: Opaque
data:
  DB_PASSWORD: ${DB_PASSWORD_B64}
  JWT_SECRET: ${JWT_SECRET_B64}
EOF

echo "[OK] Generated k8s/secret.yaml"
echo ""
echo "=== Generated Secrets (save these in your password manager!) ==="
echo "DB_PASSWORD: ${DB_PASSWORD}"
echo "JWT_SECRET: ${JWT_SECRET}"
echo ""
echo "=== IMPORTANT ==="
echo "1. Store these secrets securely (password manager, vault)"
echo "2. Apply with: kubectl apply -f k8s/secret.yaml"
echo "3. Rotate secrets regularly (every 90 days recommended)"
echo "4. Never commit unencrypted secrets to git"
