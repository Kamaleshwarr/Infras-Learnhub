#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api/v1}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@learninghub.local}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin@12345}"
EMPLOYEE_EMAIL="${EMPLOYEE_EMAIL:-employee@learninghub.local}"
EMPLOYEE_PASSWORD="${EMPLOYEE_PASSWORD:-Employee@12345}"

login() {
  local email="$1"
  local password="$2"
  curl -sS -X POST "$API_BASE_URL/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])'
}

echo "=== Initiative visibility investigation ==="
echo "API base: $API_BASE_URL"
echo

ADMIN_TOKEN="$(login "$ADMIN_EMAIL" "$ADMIN_PASSWORD")"
EMPLOYEE_TOKEN="$(login "$EMPLOYEE_EMAIL" "$EMPLOYEE_PASSWORD")"

echo "1. Admin visibility diagnostics (all initiatives + exclusion reasons)"
curl -sS "$API_BASE_URL/initiatives/visibility-diagnostics" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -m json.tool
echo

echo "2. ADMIN GET /api/v1/initiatives?size=100&status=ACTIVE"
curl -sS "$API_BASE_URL/initiatives?size=100&status=ACTIVE" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -m json.tool
echo

echo "3. EMPLOYEE GET /api/v1/initiatives?size=100&status=ACTIVE"
curl -sS "$API_BASE_URL/initiatives?size=100&status=ACTIVE" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  | python3 -m json.tool
echo

echo "4. EMPLOYEE GET /api/v1/me/submissions?size=100&sort=submittedAtUtc,desc"
curl -sS "$API_BASE_URL/me/submissions?size=100&sort=submittedAtUtc,desc" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  | python3 -m json.tool
