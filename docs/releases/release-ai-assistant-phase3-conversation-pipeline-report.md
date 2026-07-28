# AI Assistant Phase 3 — Conversation Pipeline & Mock Chat

**Phase:** AI Assistant — Phase 3 (Conversation Pipeline & Mock Chat)  
**Date:** 2026-07-28  
**Branch:** `cursor/ai-assistant-phase3-conversation-pipeline-26bf`  
**Status:** Implementation complete — awaiting PR review

---

## 1. Repository review

Verified repository: **Infras-Learnhub** (`https://github.com/Kamaleshwarr/Infras-Learnhub.git`)

Mandatory documentation reviewed before implementation:

- `.cursor/architecture.md`, `.cursor/project-context.md`, `.cursor/engineering-standards.md`
- `README.md`, `docs/project-roadmap.md`, `docs/development-workflow.md`
- AI Assistant Phase 1 and Phase 2 reports
- Completed modules: Authentication, Learn, Initiatives, Certificates, Projects, Leaderboards, Communication Framework

---

## 2. Architecture decisions

| Decision | Rationale |
|----------|-----------|
| `chat()` pipeline in `AssistantOrchestrationService` | Single orchestration entry point; reuses Phase 2 `processRequest` intent/tool routing |
| Conversation persistence before/after orchestration | USER message saved before processing; ASSISTANT message saved after response assembly |
| Optional `conversationId` with ownership validation | Client can pass prior conversation ID; mismatches return 404 |
| `AssistantResponse` with `sources` and `metadata` | Future-ready source model; tool responses HIGH confidence, MockLlm LOW |
| `MockLlmClient` template responses | Realistic explanatory text without inventing platform data |
| KNOWLEDGE and UNKNOWN both invoke `MockLlmClient` | Unknown questions receive explicit insufficient-information fallback |
| Feature flag returns HTTP 503 on chat/conversation | Status endpoint remains available; chat endpoints gated by `app.assistant.enabled` |
| No new Flyway migration | Phase 1 V21 schema sufficient |
| No frontend changes | Phase 3 scope is backend API only |

---

## 3. Chat pipeline

```text
POST /api/v1/assistant/chat
    ↓
requireEnabled()
    ↓
resolveConversation(user, conversationId?)
    ↓
appendMessage(USER)
    ↓
IntentResolver → NAVIGATION | TOOL | KNOWLEDGE | UNKNOWN
    ↓
Build response (tools from services; knowledge/unknown from MockLlmClient)
    ↓
appendMessage(ASSISTANT)
    ↓
AssistantResponse
```

---

## 4. Conversation model

- One conversation per user (`assistant_conversations.user_id` UNIQUE)
- Auto-created on first chat message
- Messages stored in `assistant_messages` with roles `USER` / `ASSISTANT`
- `GET /api/v1/assistant/conversation` returns messages ordered by `created_at` ascending
- Conversation `updated_at` touched on each append (existing auditing pattern)

---

## 5. API endpoints

| Method | Path | Auth | Feature flag | Description |
|--------|------|------|--------------|-------------|
| `GET` | `/api/v1/assistant/status` | Bearer | No (reports `enabled`) | Provider health |
| `POST` | `/api/v1/assistant/chat` | Bearer | Required | Send message, receive `AssistantResponse` |
| `GET` | `/api/v1/assistant/conversation` | Bearer | Required | Conversation history |

### AssistantRequest

- `message` (required, max 4000 chars)
- `conversationId` (optional UUID)

### AssistantResponse

- `response`, `conversationId`, `intentType`, `toolUsed`, `sources`, `metadata`

---

## 6. Backend changes

**New**

- `assistant/dto/AssistantResponse.java`
- `assistant/dto/AssistantSourceResponse.java`
- `assistant/dto/AssistantSourceConfidence.java`
- `assistant/dto/ConversationResponse.java`
- `assistant/dto/ConversationMessageResponse.java`
- `assistant/mapper/AssistantMapper.java`
- `assistant/service/AssistantDisabledException.java`

**Updated**

- `AssistantController` — `POST /chat`, `GET /conversation`
- `AssistantOrchestrationService` — full chat pipeline, MockLlm integration, source mapping
- `AssistantConversationService` — `resolveConversation`, `getConversationResponse`, overload `appendMessage`
- `AssistantRequest` — optional `conversationId`, validation
- `AssistantOrchestrationResponse` — `toolName` field; knowledge via LLM
- `MockLlmClient` — realistic template responses

**Unchanged**

- No OpenAI HTTP integration
- No streaming
- No frontend UI
- `app.assistant.enabled=false` default preserved

---

## 7. Tests executed

```bash
mvn -f backend/pom.xml clean compile
mvn -f backend/pom.xml test -Dtest="com.company.learninghub.assistant.**"
cd frontend && npm run build
cd frontend && npm test -- --run
```

| Suite | Result |
|-------|--------|
| Assistant module tests | **52 passed, 0 failed** |
| `mvn clean compile` | **Success** |
| Frontend `npm run build` | **Success** |
| Full backend `mvn test` | 475 run — 3 pre-existing failures in unrelated modules (`NotificationControllerTest`, `UserManagementServiceTest`) |
| Frontend `npm test` | 457 passed — 3 pre-existing failures (router context) |

---

## 8. Startup verification

| Check | Result |
|-------|--------|
| PostgreSQL + Flyway V21 | **Success** |
| `mvn spring-boot:run` (`APP_ASSISTANT_ENABLED=true`) | **Success** — no BeanCreationException |
| `GET /api/v1/health` | `{"status":"UP"}` |
| `GET /api/v1/assistant/status` | `{"enabled":true,"llmProvider":"mock","llmHealthy":true}` |
| `POST /api/v1/assistant/chat` (navigation) | Structured response with metadata.navigation |
| `POST /api/v1/assistant/chat` (tool) | Tool data from LeaderboardService, HIGH confidence source |
| `POST /api/v1/assistant/chat` (knowledge) | Spring Boot explanation from MockLlmClient |
| `POST /api/v1/assistant/chat` (unknown) | Insufficient-information fallback |
| `GET /api/v1/assistant/conversation` | USER/ASSISTANT messages with timestamps |
| Frontend `npm run dev` | **Success** — ready on port 5173 |
| Docker Compose | **Not available** in cloud agent environment; validated with local PostgreSQL + Spring Boot instead |

---

## 9. Regression verification

- All 52 assistant tests pass (Phase 1 + 2 + 3)
- `GET /api/v1/assistant/status` unchanged
- No schema changes
- No frontend code changes
- Feature flag default unchanged (`false`)

---

## 10. Documentation updates

- `.cursor/architecture.md` — AI Assistant Phase 3 section
- `docs/releases/release-ai-assistant-phase3-conversation-pipeline-report.md` (this file)

---

## 11. Files changed

**Backend (new)**

- `assistant/dto/AssistantResponse.java`
- `assistant/dto/AssistantSourceResponse.java`
- `assistant/dto/AssistantSourceConfidence.java`
- `assistant/dto/ConversationResponse.java`
- `assistant/dto/ConversationMessageResponse.java`
- `assistant/mapper/AssistantMapper.java`
- `assistant/service/AssistantDisabledException.java`

**Backend (modified)**

- `assistant/controller/AssistantController.java`
- `assistant/dto/AssistantRequest.java`
- `assistant/dto/AssistantOrchestrationResponse.java`
- `assistant/llm/MockLlmClient.java`
- `assistant/service/AssistantConversationService.java`
- `assistant/service/AssistantOrchestrationService.java`

**Tests (modified/new coverage)**

- `assistant/controller/AssistantControllerTest.java`
- `assistant/service/AssistantOrchestrationServiceTest.java`
- `assistant/service/AssistantConversationServiceTest.java`
- `assistant/llm/MockLlmClientTest.java`

**Documentation**

- `.cursor/architecture.md`
- `docs/releases/release-ai-assistant-phase3-conversation-pipeline-report.md`

---

## 12. Risks

| Risk | Mitigation |
|------|------------|
| MockLlm responses may not cover all knowledge queries | Template-based responses + explicit fallback message; real LLM deferred |
| Single conversation per user | By design for Phase 3; multi-conversation deferred |
| Chat/conversation return 503 when disabled | Clients should check `/assistant/status` first |
| Tool structured data in metadata may grow large | Acceptable for Phase 3; pagination/citations deferred |

---

## 13. Architecture Compliance Checklist

| Item | Status |
|------|--------|
| Existing services reused (Profile, Leaderboard, Certificate, Initiative) | Yes |
| No repository access from orchestration | Yes — orchestration uses `AssistantConversationService` only |
| Read-only tools only | Yes |
| Feature flag respected | Yes — chat/conversation require `app.assistant.enabled=true` |
| No duplicated business logic | Yes — tools delegate to existing services |
| No unrelated modules modified | Yes |
| Conversation persistence uses existing auditing conventions | Yes — `touch(updatedAt)` on append |
| No OpenAI integration | Yes |
| No frontend UI | Yes |
| No streaming | Yes |
| No Phase 4 work | Yes |

---

## 14. Explicit confirmation

**Only Phase 3 implemented.**

- No OpenAI HTTP integration
- No frontend UI
- No streaming
- No Phase 4 work
