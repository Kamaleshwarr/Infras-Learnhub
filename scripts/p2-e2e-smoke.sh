#!/usr/bin/env bash
set -euo pipefail

API="${API_BASE:-http://localhost:8080/api/v1}"

login() {
  local email="$1" password="$2"
  curl -fsS -X POST "$API/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$password\"}" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])'
}

ADMIN_TOKEN=$(login admin@learninghub.local 'Admin@12345')
EMP_TOKEN=$(login employee@learninghub.local 'Employee@12345')

PROJECT_JSON=$(curl -fsS -X POST "$API/projects" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"E2E P2 KB $(date +%s%N)\",\"description\":\"P2 knowledge depth test\",\"accessType\":\"MEMBERS_ONLY\"}")
PROJECT_ID=$(echo "$PROJECT_JSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

echo "== Employee blocked from members-only knowledge =="
employee_status=$(curl -s -o /dev/null -w '%{http_code}' "$API/projects/$PROJECT_ID/folders" -H "Authorization: Bearer $EMP_TOKEN")
if [[ "$employee_status" != "404" ]]; then
  echo "Expected 404 for non-member folder list, got $employee_status"
  exit 1
fi

echo "== Three-level folder hierarchy =="
QA_FOLDER=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/folders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"QA","description":"Quality area"}')
QA_ID=$(echo "$QA_FOLDER" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

AUTO_FOLDER=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/folders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"Automation\",\"description\":\"Automation\",\"parentId\":\"$QA_ID\"}")
AUTO_ID=$(echo "$AUTO_FOLDER" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

API_FOLDER=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/folders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"API Testing\",\"description\":\"API tests\",\"parentId\":\"$AUTO_ID\"}")
API_ID=$(echo "$API_FOLDER" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

depth_status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/projects/$PROJECT_ID/folders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"Postman\",\"description\":\"blocked level 4\",\"parentId\":\"$API_ID\"}")
if [[ "$depth_status" != "400" ]]; then
  echo "Expected 400 for fourth folder level, got $depth_status"
  exit 1
fi

LINK_JSON=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/items/links" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"folderId\":\"$API_ID\",\"title\":\"QA Postman Collection\",\"description\":\"Postman\",\"category\":\"KT_DOCUMENTS\",\"externalUrl\":\"https://example.com/postman\"}")
ITEM_ID=$(echo "$LINK_JSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS "$API/projects/$PROJECT_ID/items?search=postman&sourceType=LINK" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["totalElements"]>=1'

curl -fsS "$API/projects/$PROJECT_ID/folders/$API_ID" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["name"]=="API Testing"'

delete_status=$(curl -s -o /dev/null -w '%{http_code}' -X DELETE "$API/projects/$PROJECT_ID/items/$ITEM_ID" -H "Authorization: Bearer $EMP_TOKEN")
if [[ "$delete_status" != "404" && "$delete_status" != "403" && "$delete_status" != "400" ]]; then
  echo "Expected non-member delete denial, got $delete_status"
  exit 1
fi

curl -fsS -X DELETE "$API/projects/$PROJECT_ID/items/$ITEM_ID" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

echo "== P1 regression spot checks =="
curl -fsS "$API/projects?assigned=true" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null
TECH_ID_LEARN=$(curl -fsS "$API/learn/technologies?size=1" -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c 'import json,sys; print(json.load(sys.stdin)["content"][0]["id"])')
curl -fsS "$API/learn/technologies/$TECH_ID_LEARN" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null
curl -fsS "$API/learn/journey" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null

echo "P2 depth refinement E2E passed"
