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

echo "== Employee create project denied =="
status=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$API/projects" \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"E2E Blocked","description":"x","accessType":"PUBLIC"}')
test "$status" = "403"

PROJECT_NAME="E2E P1 Portal Project $(date +%s)"
echo "== Admin create project =="
PROJECT_JSON=$(curl -fsS -X POST "$API/projects" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"name\":\"$PROJECT_NAME\",\"description\":\"P1 smoke test project\",\"accessType\":\"MEMBERS_ONLY\"}")
PROJECT_ID=$(echo "$PROJECT_JSON" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')
echo "Created project $PROJECT_ID"

echo "== Employee cannot access members-only project =="
status=$(curl -s -o /dev/null -w '%{http_code}' "$API/projects/$PROJECT_ID" \
  -H "Authorization: Bearer $EMP_TOKEN")
test "$status" = "404"

echo "== Admin can access project overview =="
curl -fsS "$API/projects/$PROJECT_ID" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; p=json.load(sys.stdin); assert p["status"]=="ACTIVE"; assert p["accessType"]=="MEMBERS_ONLY"'

echo "== Employee assigned filter empty =="
curl -fsS "$API/projects?assigned=true" -H "Authorization: Bearer $EMP_TOKEN" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["totalElements"]==0'

echo "== Technology cross-navigation =="
TECH_ID=$(curl -fsS "$API/learn/technologies?size=1" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["content"][0]["id"])')

curl -fsS -X POST "$API/learn/manage/technologies/$TECH_ID/project-links" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"projectId\":\"$PROJECT_ID\"}" > /dev/null

curl -fsS "$API/learn/technologies/$TECH_ID" -H "Authorization: Bearer $EMP_TOKEN" \
  | python3 -c 'import json,sys; assert json.load(sys.stdin)["relatedProjects"]==[]'

curl -fsS "$API/projects/$PROJECT_ID" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | python3 -c 'import json,sys; p=json.load(sys.stdin); assert len(p.get("relatedTechnologies",[]))>=1'

echo "== Learn regression endpoints =="
for path in \
  "$API/learn/journey" \
  "$API/learn/technologies?search=spring" \
  "$API/learn/technologies/spring-boot/roadmap"; do
  status=$(curl -s -o /dev/null -w '%{http_code}' "$path" -H "Authorization: Bearer $EMP_TOKEN")
  test "$status" = "200"
done

echo "P1 E2E smoke passed"
