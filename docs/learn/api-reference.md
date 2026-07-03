# Learn Module — API Reference

**Base path:** `/api/v1`  
**Authentication:** JWT Bearer token on all endpoints  
**Roles:** `ADMIN`, `EMPLOYEE`

Swagger UI: `http://localhost:8080/swagger-ui.html` (tag: Learn)

---

## Employee APIs — Technologies

### `GET /learn/technologies`

**Purpose:** Paginated list of published catalog technologies with search and filters.

**Permissions:** ADMIN, EMPLOYEE (employees see `PUBLISHED` + `catalog_present` only)

**Query parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `search` | string | — | Whole-term search across name, slug, tags, description |
| `category` | enum | — | Technology category filter |
| `difficulty` | enum | — | BEGINNER, INTERMEDIATE, ADVANCED |
| `page` | int | 0 | Page index |
| `size` | int | 20 | Page size |
| `sort` | string | `name,asc` | Sort field and direction |

**Response:** `PageResponse<TechnologyResponse>`

**Example:**

```bash
curl -s "http://localhost:8080/api/v1/learn/technologies?search=java&size=5" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### `GET /learn/technologies/{technologyId}`

**Purpose:** Single technology detail by UUID.

**Permissions:** ADMIN (all statuses), EMPLOYEE (published only → 404 if hidden)

**Response:** `TechnologyResponse`

---

## Employee APIs — Roadmaps

### `GET /learn/technologies/{slug}/roadmap`

**Purpose:** Roadmap by technology slug.

**Permissions:** Authenticated; employees require published technology.

**Response:** `RoadmapResponse` with ordered `stages[]`, each with `learningResources[]` and `practiceResources[]`.

---

### `GET /learn/technologies/id/{technologyId}/roadmap`

**Purpose:** Roadmap by technology UUID (used by frontend roadmap page).

**Permissions:** Same as slug endpoint.

**Response:** `RoadmapResponse`

**Example:**

```bash
curl -s "http://localhost:8080/api/v1/learn/technologies/id/${TECH_ID}/roadmap" \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## Employee APIs — Progress

### `POST /learn/enrollments`

**Purpose:** Enroll in a technology roadmap. Creates enrollment and auto-starts at stage 1.

**Permissions:** ADMIN, EMPLOYEE (owner-scoped)

**Request:**

```json
{ "technologyId": "eb1559fa-b253-46f4-b7b3-9e7c1fa48ada" }
```

**Response:** `201` `EnrollmentResponse`

**Errors:** `409` if active enrollment already exists for the technology.

---

### `POST /learn/enrollments/{enrollmentId}/start`

**Purpose:** Move a `NOT_STARTED` enrollment to `IN_PROGRESS`.

**Permissions:** Owner only

**Response:** `EnrollmentResponse`

---

### `POST /learn/enrollments/{enrollmentId}/complete-stage`

**Purpose:** Complete the next sequential stage.

**Permissions:** Owner only

**Request:**

```json
{ "stageId": "55089351-b3f3-4a37-9afa-67b2daf55a4f" }
```

**Response:** `EnrollmentResponse` with updated progress

**Errors:** `400` if stage is not the next incomplete stage (sequential validation).

---

### `DELETE /learn/enrollments/{enrollmentId}`

**Purpose:** Leave enrollment (status → `LEFT`). History preserved.

**Permissions:** Owner only

**Response:** `204 No Content`

---

### `GET /learn/journey`

**Purpose:** User's learning journey — Continue Learning card data plus active, completed, and left enrollments.

**Permissions:** Authenticated user (own journey only)

**Response:** `JourneyResponse`

```json
{
  "continueLearning": {
    "enrollmentId": "...",
    "technologyId": "...",
    "technologyName": "Spring Boot",
    "currentStageTitle": "REST APIs",
    "progressPercent": 33
  },
  "active": [],
  "completed": [],
  "left": []
}
```

---

### `GET /learn/progress/technologies/{technologyId}`

**Purpose:** Full progress overlay for roadmap page (enrollment + completed stage IDs).

**Permissions:** Owner only → `404` if no enrollment

**Response:** `TechnologyProgressResponse`

---

### `GET /learn/enrollments/technologies/{technologyId}`

**Purpose:** Active enrollment for a technology.

**Permissions:** Owner only → `404` if none

**Response:** `EnrollmentResponse`

---

## Admin APIs — Technology curation

**Base:** `/learn/manage/technologies`  
**Permissions:** `ADMIN` only

### `GET /learn/manage/technologies`

**Purpose:** Admin curation list (all statuses).

**Query:** Same as employee list plus `status` filter (HIDDEN, PUBLISHED, ARCHIVED).

**Response:** `PageResponse<TechnologyResponse>`

---

### `PATCH /learn/manage/technologies/{technologyId}/curation`

**Purpose:** Update org curation fields (featured override, status, org notes).

**Request:**

```json
{
  "featured": true,
  "status": "PUBLISHED",
  "orgNotes": "Required for Q3 platform team."
}
```

**Response:** `TechnologyResponse`

---

### `POST /learn/manage/technologies/{technologyId}/publish`

**Purpose:** `HIDDEN` → `PUBLISHED`

---

### `POST /learn/manage/technologies/{technologyId}/hide`

**Purpose:** `PUBLISHED` → `HIDDEN`

---

### `POST /learn/manage/technologies/{technologyId}/archive`

**Purpose:** `PUBLISHED` → `ARCHIVED` (terminal)

---

### `POST /learn/manage/technologies/{technologyId}/project-links`

**Purpose:** Link organizational project to technology.

**Request:** `{ "projectId": "uuid" }`

---

### `DELETE /learn/manage/technologies/{technologyId}/project-links/{projectId}`

**Purpose:** Remove project link.

---

## Admin APIs — Catalog

### `GET /learn/manage/catalog/status`

**Purpose:** Catalog import status for technologies and roadmaps.

**Permissions:** `ADMIN`

**Response:**

```json
{
  "catalogVersion": "1.1.1",
  "importedAt": "2026-07-03T09:46:00Z",
  "packageType": "technologies",
  "recordsUpserted": 30,
  "status": "SUCCESS",
  "technologyCount": 30,
  "roadmapImportStatus": "SUCCESS",
  "roadmapRecordsUpserted": 5,
  "roadmapCount": 5
}
```

---

## Response DTOs (summary)

| DTO | Key fields |
|-----|------------|
| `TechnologyResponse` | `id`, `slug`, `name`, `category`, `difficulty`, `status`, `featured`, `tags`, `relatedProjects`, `catalogVersion` |
| `RoadmapResponse` | `technologyName`, `version`, `stageCount`, `stages[]` |
| `RoadmapStageResponse` | `id`, `order`, `title`, `estimatedEffort`, `learningResources`, `practiceResources` |
| `EnrollmentResponse` | `id`, `status`, `progressPercent`, `currentStageTitle`, `nextStageTitle` |
| `TechnologyProgressResponse` | Enrollment fields + `completedStageIds`, `completedStageCount`, `estimatedRemainingEffort` |
| `JourneyResponse` | `continueLearning`, `active`, `completed`, `left` |

Full DTO definitions: `backend/src/main/java/com/company/learninghub/learn/dto/`

---

## Business rules (API layer)

| Rule | Enforcement |
|------|-------------|
| Sequential stage completion | `LearningProgressService` — only next stage allowed |
| One active enrollment per technology | DB partial unique index + `409` on duplicate |
| Employee progress ownership | All progress APIs use `@AuthenticationPrincipal` |
| No admin progress editing | No admin write endpoints for enrollments (BR-PR10) |
| Roadmap read-only | No PUT/POST on roadmap content; catalog import only |
| Employee technology visibility | Unpublished/hidden → `404` (not `403`) |

---

## Frontend API client

All Learn HTTP calls: `frontend/src/api/learnApi.ts`

| Frontend method | Backend endpoint |
|-----------------|------------------|
| `listTechnologies` | `GET /learn/technologies` |
| `getTechnology` | `GET /learn/technologies/{id}` |
| `getRoadmapByTechnologyId` | `GET /learn/technologies/id/{id}/roadmap` |
| `enrollInTechnology` | `POST /learn/enrollments` |
| `completeStage` | `POST /learn/enrollments/{id}/complete-stage` |
| `getJourney` | `GET /learn/journey` |
| `getTechnologyProgress` | `GET /learn/progress/technologies/{id}` |
| `getActiveEnrollment` | `GET /learn/enrollments/technologies/{id}` |
