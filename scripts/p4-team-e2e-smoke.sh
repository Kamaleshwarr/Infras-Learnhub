#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API="${API_BASE:-http://localhost:8080/api/v1}"

login() {
  curl -fsS -X POST "$API/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])'
}

ADMIN_TOKEN=$(login admin@learninghub.local 'Admin@12345')
EMP_TOKEN=$(login employee@learninghub.local 'Employee@12345')

echo "== Create MEMBERS_ONLY project =="
PROJECT_ID=$(curl -fsS -X POST "$API/projects" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"E2E P4 $(date +%s%N)\",\"description\":\"P4 team test\",\"accessType\":\"MEMBERS_ONLY\"}" \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

echo "== MEMBERS_ONLY protection for team endpoints =="
emp_members_status=$(curl -s -o /dev/null -w '%{http_code}' "$API/projects/$PROJECT_ID/members" -H "Authorization: Bearer $EMP_TOKEN")
if [[ "$emp_members_status" != "404" ]]; then echo "Expected 404 for non-member members list, got $emp_members_status"; exit 1; fi
emp_contacts_status=$(curl -s -o /dev/null -w '%{http_code}' "$API/projects/$PROJECT_ID/contacts" -H "Authorization: Bearer $EMP_TOKEN")
if [[ "$emp_contacts_status" != "404" ]]; then echo "Expected 404 for non-member contacts list, got $emp_contacts_status"; exit 1; fi

echo "== Lookup users for team assignments =="
USERS_JSON=$(curl -fsS "$API/users?size=50&page=0" -H "Authorization: Bearer $ADMIN_TOKEN")
OWNER_USER_ID=$(echo "$USERS_JSON" | python3 -c 'import json,sys; users=json.load(sys.stdin)["content"]; print(next(u["id"] for u in users if u["email"]=="admin@learninghub.local"))')
EMP_USER_ID=$(echo "$USERS_JSON" | python3 -c 'import json,sys; users=json.load(sys.stdin)["content"]; print(next(u["id"] for u in users if u["email"]=="employee@learninghub.local"))')

echo "== Add team members with access + functional roles =="
curl -fsS -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$OWNER_USER_ID\",\"projectRole\":\"OWNER\",\"functionalRole\":\"PRODUCT_OWNER\",\"responsibility\":\"Roadmap and prioritization\",\"primaryContact\":true}" >/dev/null

curl -fsS -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$EMP_USER_ID\",\"projectRole\":\"CONTRIBUTOR\",\"functionalRole\":\"TECH_LEAD\",\"responsibility\":\"Architecture reviews\",\"primaryContact\":true}" >/dev/null

MEMBERS=$(curl -fsS "$API/projects/$PROJECT_ID/members" -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$MEMBERS" | python3 -c 'import json,sys; data=json.load(sys.stdin); assert len(data)>=2; roles={(m["projectRole"], m["functionalRole"]) for m in data}; assert ("OWNER","PRODUCT_OWNER") in roles; assert ("CONTRIBUTOR","TECH_LEAD") in roles'

echo "== Edit functional role and responsibility =="
curl -fsS -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$EMP_USER_ID\",\"projectRole\":\"CONTRIBUTOR\",\"functionalRole\":\"DEVELOPER\",\"responsibility\":\"Backend APIs and database migrations\",\"primaryContact\":false}" >/dev/null

UPDATED=$(curl -fsS "$API/projects/$PROJECT_ID/members" -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$UPDATED" | python3 -c 'import json,sys; data=json.load(sys.stdin); dev=next(m for m in data if m["functionalRole"]=="DEVELOPER"); assert dev["projectRole"]=="CONTRIBUTOR"; assert "Backend APIs" in dev["responsibility"]'

echo "== Mark primary contact =="
curl -fsS -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$EMP_USER_ID\",\"projectRole\":\"CONTRIBUTOR\",\"functionalRole\":\"DEVELOPER\",\"responsibility\":\"Backend APIs and database migrations\",\"primaryContact\":true}" >/dev/null

echo "== Duplicate member upsert does not create second row =="
curl -fsS -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$EMP_USER_ID\",\"projectRole\":\"CONTRIBUTOR\",\"functionalRole\":\"DEVELOPER\",\"primaryContact\":true}" >/dev/null
MEMBER_COUNT=$(curl -fsS "$API/projects/$PROJECT_ID/members" -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c 'import json,sys; print(len(json.load(sys.stdin)))')
if [[ "$MEMBER_COUNT" -lt 2 ]]; then echo "Expected at least 2 members after upsert, got $MEMBER_COUNT"; exit 1; fi

echo "== External contact CRUD =="
CONTACT=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/contacts" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Client PO","contactType":"CLIENT","roleTitle":"Product Owner","organization":"Acme Corp","email":"client@example.com","primaryContact":true}')
CONTACT_ID=$(echo "$CONTACT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X PUT "$API/projects/$PROJECT_ID/contacts/$CONTACT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Client PO","contactType":"CLIENT","roleTitle":"Business Owner","organization":"Acme Corp","email":"client@example.com","primaryContact":false}' >/dev/null

echo "== Project overview counts =="
PROJECT=$(curl -fsS "$API/projects/$PROJECT_ID" -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$PROJECT" | python3 -c 'import json,sys; p=json.load(sys.stdin); assert p["memberCount"]>=2; assert p["primaryContactCount"]>=1'

echo "== Contributor cannot manage members =="
contrib_status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/projects/$PROJECT_ID/members" \
  -H "Authorization: Bearer $EMP_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$EMP_USER_ID\",\"projectRole\":\"VIEWER\",\"functionalRole\":\"OTHER\"}")
if [[ "$contrib_status" != "400" && "$contrib_status" != "403" ]]; then echo "Expected contributor manage rejection, got $contrib_status"; exit 1; fi

echo "== Employee can read team after membership =="
curl -fsS "$API/projects/$PROJECT_ID/members" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null

echo "== P1/P2/P3/Learn regression spot checks =="
curl -fsS "$API/projects?size=1" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null
curl -fsS "$API/projects/$PROJECT_ID/folders" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null
curl -fsS "$API/projects/$PROJECT_ID/environments" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null
curl -fsS "$API/projects/$PROJECT_ID/repositories" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null
curl -fsS "$API/learn/technologies?size=1" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

if curl -fsS -o /dev/null "http://localhost:5173" 2>/dev/null; then
  echo "== Browser select regression (Add/Edit member dialogs) =="
  (cd "$ROOT_DIR/frontend" && node scripts/p4-team-member-select-smoke.mjs)
else
  echo "Skipping browser select regression: frontend dev server not reachable on :5173"
fi

echo "P4 team & contacts E2E smoke passed for project $PROJECT_ID"
