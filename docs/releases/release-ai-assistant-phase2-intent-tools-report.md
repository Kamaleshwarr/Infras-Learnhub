# AI Assistant Phase 2 — Intent Resolution & Tool Registry

**Phase:** AI Assistant — Phase 2 (Intent Resolution & Tool Registry)  
**Date:** 2026-07-28  
**Branch:** `cursor/ai-assistant-phase2-intent-tools-d41b`  
**Status:** Implementation complete — awaiting PR review

---

## 1. Repository review

Verified repository: **Infras-Learnhub** (`https://github.com/Kamaleshwarr/Infras-Learnhub.git`)

Mandatory documentation reviewed before implementation:

- `.cursor/architecture.md`, `.cursor/project-context.md`, `.cursor/engineering-standards.md`
- `README.md`, `docs/project-roadmap.md`, `docs/development-workflow.md`
- Completed modules: Authentication, Learn, Learning Initiatives, Certificate Workflow, Projects, Leaderboards, Communication Framework, AI Assistant Phase 1

---

## 2. Architecture decisions

| Decision | Rationale |
|----------|-----------|
| Rule-based `IntentResolver` (no LLM) | Phase 2 scope explicitly excludes LLM calls |
| `NavigationIntentResolver` as separate component | Clear separation of navigation vs tool classification |
| `AssistantTool` + Spring bean registration | Extensible registry pattern; mirrors provider abstraction |
| `ToolResult` with structured data + extension fields | Supports current read-only tools and future citations/markdown |
| Tools delegate to existing services only | Preserves authorization (`@PreAuthorize`) and avoids duplicated business logic |
| `processRequest` gated by `app.assistant.enabled` | Feature flag remains off by default; status endpoint always available |
| No new HTTP endpoint | Chat/orchestration API deferred; orchestration is service-layer only |
| Admin certifications via `listAll` filtered by own user ID | `listOwn` requires `EMPLOYEE` role; admins use existing admin read path |

---

## 3. Intent model

`AssistantIntentType`: `NAVIGATION`, `TOOL`, `KNOWLEDGE`, `UNKNOWN`

`ResolvedIntent` carries:

- Intent type
- Optional `NavigationTarget` (path + label)
- Optional tool name (slug)
- Normalized message text

Classification order: navigation → tool keywords → knowledge heuristics → unknown.

---

## 4. Tool registry

`AssistantToolRegistry` receives all `AssistantTool` Spring beans, finds the first tool whose `supports(ResolvedIntent)` returns true, and delegates `execute(AssistantToolContext)`.

`AssistantToolContext` contains the authenticated user and original message.

---

## 5. Tools implemented

| Tool name | User phrases (examples) | Service reused |
|-----------|-------------------------|----------------|
| `my-profile` | my profile, show my profile | `ProfileService.getProfile` |
| `my-leaderboard-rank` | my rank, my leaderboard rank | `LeaderboardService.getPersonalRanking` |
| `my-certifications` | my certifications, my certificates | `CertificateSubmissionService.listOwn` / `listAll` |
| `available-learning-initiatives` | learning initiatives, available initiatives | `LearningInitiativeService.list` |

All tools are read-only.

---

## 6. Backend changes

**New packages**

- `assistant/intent/` — `IntentResolver`, `NavigationIntentResolver`, `ResolvedIntent`, `AssistantIntentType`, `NavigationTarget`
- `assistant/tool/` — `AssistantTool`, `AssistantToolRegistry`, `ToolResult`, four tool implementations
- `assistant/dto/` — `AssistantRequest`, `AssistantOrchestrationResponse`, `NavigationInstruction`, `AssistantOutcomeType`

**Updated**

- `AssistantOrchestrationService` — adds `processRequest` orchestration pipeline

**Unchanged**

- No Flyway migration (Phase 1 V21 sufficient)
- No chat endpoint
- No LLM HTTP integration
- `app.assistant.enabled=false` default preserved

---

## 7. Tests executed

```bash
mvn -f backend/pom.xml clean compile
mvn -f backend/pom.xml test -Dtest="com.company.learninghub.assistant.**"
cd frontend && npm run build
```

| Suite | Result |
|-------|--------|
| `IntentResolverTest` | 8 passed |
| `NavigationIntentResolverTest` | 5 passed |
| `AssistantToolRegistryTest` | 4 passed |
| `MyProfileToolTest` | 1 passed |
| `MyLeaderboardRankToolTest` | 2 passed |
| `MyCertificationsToolTest` | 2 passed |
| `AvailableLearningInitiativesToolTest` | 1 passed |
| `AssistantOrchestrationServiceTest` | 6 passed |
| Phase 1 assistant tests (regression) | 10 passed |
| **Total assistant tests** | **39 passed, 0 failed** |
| Frontend `npm run build` | **Success** |

Full backend suite (`mvn test`): 3 pre-existing failures in unrelated modules (`NotificationControllerTest`, `UserManagementServiceTest`) — not introduced by Phase 2.

---

## 8. Startup verification

```bash
mvn -f backend/pom.xml spring-boot:run --app.catalog.import.enabled=false
```

| Check | Result |
|-------|--------|
| Flyway V21 applied | **Success** — `now at version v21` |
| Application started | **Success** — `Started LearningHubApplication` |
| BeanCreationException | **None** |
| Health endpoint | `GET /api/v1/health` → `{"status":"UP"}` |
| Assistant status (authenticated) | `GET /api/v1/assistant/status` → `{"enabled":false,"llmProvider":"mock","llmHealthy":true}` |
| Frontend dev server | `npm run dev` → ready on port 5173 |

---

## 9. Regression verification

- All 39 assistant unit tests pass (Phase 1 + Phase 2)
- `GET /api/v1/assistant/status` unchanged
- No schema changes; Flyway remains at V21
- No frontend code changes
- Feature flag default unchanged (`false`)

---

## 10. Documentation updates

- `.cursor/architecture.md` — AI Assistant Phase 2 section
- `docs/releases/release-ai-assistant-phase2-intent-tools-report.md` (this file)

---

## 11. Files changed

### Backend (new)

- `assistant/intent/AssistantIntentType.java`
- `assistant/intent/NavigationTarget.java`
- `assistant/intent/ResolvedIntent.java`
- `assistant/intent/NavigationIntentResolver.java`
- `assistant/intent/IntentResolver.java`
- `assistant/tool/AssistantToolNames.java`
- `assistant/tool/AssistantTool.java`
- `assistant/tool/AssistantToolContext.java`
- `assistant/tool/ToolResult.java`
- `assistant/tool/AssistantToolRegistry.java`
- `assistant/tool/MyProfileTool.java`
- `assistant/tool/MyLeaderboardRankTool.java`
- `assistant/tool/MyCertificationsTool.java`
- `assistant/tool/AvailableLearningInitiativesTool.java`
- `assistant/dto/AssistantRequest.java`
- `assistant/dto/AssistantOutcomeType.java`
- `assistant/dto/NavigationInstruction.java`
- `assistant/dto/AssistantOrchestrationResponse.java`

### Backend (modified)

- `assistant/service/AssistantOrchestrationService.java`

### Tests (new)

- `assistant/intent/IntentResolverTest.java`
- `assistant/intent/NavigationIntentResolverTest.java`
- `assistant/tool/AssistantToolRegistryTest.java`
- `assistant/tool/MyProfileToolTest.java`
- `assistant/tool/MyLeaderboardRankToolTest.java`
- `assistant/tool/MyCertificationsToolTest.java`
- `assistant/tool/AvailableLearningInitiativesToolTest.java`

### Tests (modified)

- `assistant/service/AssistantOrchestrationServiceTest.java`

### Documentation

- `.cursor/architecture.md`
- `docs/releases/release-ai-assistant-phase2-intent-tools-report.md`

---

## 12. Risks

| Risk | Mitigation |
|------|------------|
| Keyword-based intent resolution is brittle | Acceptable for Phase 2; LLM intent classification deferred |
| `processRequest` not yet exposed via HTTP | Service-layer API ready for Phase 3 chat endpoint |
| Admin-only users without submissions see empty certifications | Expected; uses same service paths as REST APIs |
| Knowledge intent returns placeholder only | Explicit Phase 3 scope |

---

## 13. Explicit confirmation

**Implemented (Phase 2 only):**

- `IntentResolver` with `NAVIGATION`, `TOOL`, `KNOWLEDGE`, `UNKNOWN`
- `AssistantTool` interface and `AssistantToolRegistry`
- Four read-only tools reusing existing services
- `NavigationIntentResolver` for Projects, Learn, Leaderboards, Dashboard
- `ToolResult` abstraction with extension fields
- `AssistantOrchestrationService.processRequest` orchestration pipeline
- Unit tests for all new components

**NOT implemented (deferred):**

- Chat endpoint
- Frontend assistant UI
- OpenAI / LLM integration
- Prompt orchestration
- Source citations
- Phase 3 knowledge search
