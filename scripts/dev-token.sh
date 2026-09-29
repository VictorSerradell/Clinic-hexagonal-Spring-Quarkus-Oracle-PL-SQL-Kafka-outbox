#!/usr/bin/env bash
# Prints a 1-hour HS256 JWT for local development. Usage: scripts/dev-token.sh [secret]
set -euo pipefail
SECRET="${1:-${JWT_SECRET:-dev-secret-change-me-dev-secret-change-me-32b}}"
b64() { openssl base64 -A | tr '+/' '-_' | tr -d '='; }
header=$(printf '{"alg":"HS256","typ":"JWT"}' | b64)
payload=$(printf '{"sub":"dev-user","exp":%s}' "$(( $(date +%s) + 3600 ))" | b64)
signature=$(printf '%s.%s' "$header" "$payload" | openssl dgst -sha256 -hmac "$SECRET" -binary | b64)
echo "$header.$payload.$signature"
