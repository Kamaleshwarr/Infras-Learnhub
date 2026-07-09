#!/usr/bin/env bash
set -euo pipefail

API="${API_BASE:-http://localhost:8080/api/v1}"

login() {
  curl -fsS -X POST "$API/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["accessToken"])'
}

ADMIN_TOKEN=$(login admin@learninghub.local 'Admin@12345')
EMP_TOKEN=$(login employee@learninghub.local 'Employee@12345')

PROJECT_ID=$(curl -fsS -X POST "$API/projects" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"name\":\"E2E P3 $(date +%s%N)\",\"description\":\"P3 test\",\"accessType\":\"MEMBERS_ONLY\"}" \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

echo "== MEMBERS_ONLY protection =="
employee_status=$(curl -s -o /dev/null -w '%{http_code}' "$API/projects/$PROJECT_ID/environments" -H "Authorization: Bearer $EMP_TOKEN")
if [[ "$employee_status" != "404" ]]; then echo "Expected 404, got $employee_status"; exit 1; fi

echo "== Environments =="
QA=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/environments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"QA","description":"Quality"}')
QA_ID=$(echo "$QA" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X POST "$API/projects/$PROJECT_ID/environments/$QA_ID/references" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Swagger","referenceType":"SWAGGER","url":"https://example.com/swagger"}' >/dev/null

curl -fsS -X POST "$API/projects/$PROJECT_ID/environments/$QA_ID/references" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Application","referenceType":"APPLICATION","url":"https://example.com/app"}' >/dev/null

UAT=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/environments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"UAT","description":"User acceptance"}')
UAT_ID=$(echo "$UAT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X POST "$API/projects/$PROJECT_ID/environments/$UAT_ID/references" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"API","referenceType":"API_BASE","url":"https://example.com/api"}' >/dev/null

PROD=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/environments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Production","description":"Live"}')
PROD_ID=$(echo "$PROD" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X POST "$API/projects/$PROJECT_ID/environments/$PROD_ID/references" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Monitoring","referenceType":"MONITORING","url":"https://example.com/grafana"}' >/dev/null

curl -fsS "$API/projects/$PROJECT_ID/environments?search=swagger" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; data=json.load(sys.stdin); assert len(data)>=1'

cred_status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/projects/$PROJECT_ID/environments/$QA_ID/references" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Bad","referenceType":"OTHER","url":"https://user:pass@example.com/x"}')
if [[ "$cred_status" != "400" ]]; then echo "Expected credential URL rejection 400, got $cred_status"; exit 1; fi

echo "== Repositories =="
BACKEND=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/repositories" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Backend Service","description":"API","repositoryType":"BACKEND","provider":"GITHUB","repositoryUrl":"https://github.com/example/backend","defaultBranch":"main"}')
BACKEND_ID=$(echo "$BACKEND" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X POST "$API/projects/$PROJECT_ID/repositories" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Frontend Application","repositoryType":"FRONTEND","provider":"GITHUB","repositoryUrl":"https://github.com/example/frontend","defaultBranch":"main"}' >/dev/null

AUTO=$(curl -fsS -X POST "$API/projects/$PROJECT_ID/repositories" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Automation Framework","repositoryType":"AUTOMATION","provider":"GITHUB","repositoryUrl":"https://github.com/example/automation"}')
AUTO_ID=$(echo "$AUTO" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

curl -fsS -X PUT "$API/projects/$PROJECT_ID/repositories/$BACKEND_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Backend Service","description":"Updated API","repositoryType":"BACKEND","provider":"GITHUB","repositoryUrl":"https://github.com/example/backend","defaultBranch":"develop"}' >/dev/null

curl -fsS -X DELETE "$API/projects/$PROJECT_ID/repositories/$AUTO_ID" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null

PROJECT=$(curl -fsS "$API/projects/$PROJECT_ID" -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$PROJECT" | python3 -c 'import json,sys; d=json.load(sys.stdin); assert d.get("environmentCount",0)>=3; assert d.get("repositoryCount",0)>=2'

echo "== P2 regression =="
bash /workspace/scripts/p2-e2e-smoke.sh

echo "== Learn regression =="
curl -fsS "$API/learn/journey" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null
TECH_ID=$(curl -fsS "$API/learn/technologies?size=1" -H "Authorization: Bearer $ADMIN_TOKEN" | python3 -c 'import json,sys; print(json.load(sys.stdin)["content"][0]["id"])')
curl -fsS "$API/learn/technologies/$TECH_ID" -H "Authorization: Bearer $EMP_TOKEN" >/dev/null

echo "P3 E2E smoke passed"
