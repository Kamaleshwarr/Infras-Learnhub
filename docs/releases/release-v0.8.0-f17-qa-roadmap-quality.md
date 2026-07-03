# F17 QA Fix — Roadmap Catalog Quality

**Branch:** `cursor/f17-roadmap-viewer-59d6`  
**Scope:** F17 catalog quality and validation only — **F18 not started**

---

## 1. Root Cause

Initial F17 roadmap JSON was authored with **optional** `practiceResources`. The JSON schema and import validator only required learning resources (`minItems: 1`), so stages could import with learning links only. Manual QA found **13 of 30 stages** across 5 roadmaps missing practice resources.

---

## 2. Files Changed

| File | Change |
|------|--------|
| `catalog/schemas/roadmap.schema.json` | `practiceResources` required, `minItems: 1` |
| `catalog/manifest.json` | Bumped to **1.1.1** |
| `catalog/roadmaps/*.json` (5 files) | Added practice resources to all incomplete stages |
| `CatalogSchemaValidator.java` | Stricter stage validation |
| `CatalogRoadmapCatalogQualityTest.java` | New regression suite |
| `CatalogRoadmapSchemaValidatorTest.java` | Updated helpers |
| `CatalogImportServiceTest.java` | Catalog version 1.1.1 |

---

## 3. Catalog Changes

- **Catalog version:** 1.1.0 → **1.1.1**
- **Roadmaps package version:** 1.0.0 → **1.0.1**
- **Stages fixed:** 13 stages received practice resources

| Roadmap | Stages fixed |
|---------|--------------|
| Java | collections, streams, jvm |
| Spring Boot | data-access, testing |
| React | state-and-hooks, routing, testing-and-performance |
| Docker | introduction, networking |
| AWS | cloud-fundamentals, storage, iam-and-security |

---

## 4. Validation Improvements

Import now **fail-fast** when any stage violates:

- Missing title, description, or estimated effort
- Empty roadmap or fewer than 3 stages
- Missing learning resources (≥1)
- Missing practice resources (≥1)
- Non-HTTPS URLs
- Duplicate resource slugs within a stage
- Duplicate resource URLs within a stage
- Non-contiguous stage order

---

## 5. Tests Added

`CatalogRoadmapCatalogQualityTest`:
- All 5 production roadmaps pass validation
- Every stage has learning + practice resources
- Rejects missing practice, empty learning, invalid URL, duplicate URL, duplicate order, empty roadmap

---

## 6. Automated Test Results

```
com.company.learninghub.learn.catalog.** — all passed
```

---

## 7. Build Results

Backend compile and catalog tests: **SUCCESS**

---

## 8. Self QA

| Check | Status |
|-------|--------|
| Java roadmap complete | ✓ |
| Spring Boot roadmap complete | ✓ |
| React roadmap complete | ✓ |
| Docker roadmap complete | ✓ |
| AWS roadmap complete | ✓ |
| Every stage has both resource types | ✓ (30/30) |
| Roadmap Viewer unchanged | ✓ |
| APIs unchanged | ✓ |
| UI unchanged | ✓ |

---

## 9. Manual QA Verification

- [ ] Restart backend; catalog 1.1.1 reimports roadmaps
- [ ] Java → Collections, Streams, JVM show Practice Resources
- [ ] All 5 roadmaps: every stage has Learning + Practice sections
- [ ] Attempt import with incomplete JSON → startup fails with clear message

---

## 10. Risks

| Risk | Mitigation |
|------|------------|
| Catalog bump requires reimport on existing envs | Manifest 1.1.1 triggers roadmaps package reimport |
| External URL availability | Only established provider URLs used |

---

## 11. F18 Confirmation

**F18 has NOT been started.** No progress tracking, persistence, or new features introduced.
