# v0.8.0 — Future Enhancements & AI Opportunities

**Module:** Learn  
**Status:** Ideas only — not approved for implementation

---

## Purpose

This document captures enhancements deliberately excluded from v0.8.0 MVP. Items here require separate product approval before entering the implementation roadmap.

---

## 1. AI opportunities

AI is **optional and future-ready** in v0.8.0. No core Learn flow depends on AI. The architecture should allow integration via a pluggable recommendation or guidance service interface.

### 1.1 Personalized Career Path recommendation

**Concept:** Analyze employee profile attributes (role, team, existing certifications, completed Stages) and suggest the most relevant Career Path.

| Aspect | Detail |
|--------|--------|
| Input signals | Profile role, submission history, enrollment history, team (future) |
| Output | Ranked Career Path suggestions with rationale |
| UX | "Recommended for you" section on Learn Home |
| Guardrail | Suggestions are advisory; employee always chooses |
| Privacy | No external LLM on PII without enterprise approval |

### 1.2 "What should I learn next?" conversational guide

**Concept:** Chat-style interface that answers learning questions using the curated catalog as grounding context (RAG over Career Paths, Technologies, Resources).

| Aspect | Detail |
|--------|--------|
| Example queries | "I know Java basics, what next?", "How do I prepare for CKA?" |
| Grounding | Only recommend content from published Learn catalog |
| Disclaimer | "AI guidance — verify with your Roadmap" |
| Fallback | Link to relevant Roadmap when confidence is low |

### 1.3 Skill gap analysis

**Concept:** Compare an employee's completed Stages against a target role or Certification and highlight gaps.

| Aspect | Detail |
|--------|--------|
| Input | Target Career Path or Certification |
| Output | Gap report: missing Technologies, incomplete Stages |
| Visual | Heatmap or checklist on My Journey |
| Use case | Pre-performance-review self-assessment |

### 1.4 Resource summarization

**Concept:** AI-generated summaries of linked Learning Resources to help employees prioritize within a Stage.

| Aspect | Detail |
|--------|--------|
| Trigger | Admin opt-in per Resource or auto for official docs |
| Output | 2–3 sentence summary, key topics |
| Constraint | Summary links to original source; never replaces it |
| Risk | Stale summaries when source changes — regeneration cadence needed |

### 1.5 Smart certification readiness

**Concept:** Beyond Stage completion, estimate readiness using practice resource completion, resource visits, and time spent.

| Aspect | Detail |
|--------|--------|
| v0.8.0 baseline | All Stages complete = READY |
| AI enhancement | Weighted readiness score with confidence |
| Display | "Likely ready" vs "Fully ready" |
| Guardrail | Never block external exam registration |

### 1.6 Automated resource quality scoring

**Concept:** Background job evaluates linked resources for accessibility, recency, and relevance.

| Signal | Action |
|--------|--------|
| HTTP 404 | Flag admin: "Link may be broken" |
| Content drift | Flag for review |
| Duplicate resources | Suggest consolidation |
| Low engagement | Suggest replacement |

### 1.7 Initiative matching

**Concept:** When employee reaches Certification READY, suggest active Initiatives that reward that Certification.

| UX | Notification or Learn Home card: "Earn rewards — AWS Challenge active" |
| Rule | Still optional; never mandatory |

### 1.8 Rule-based learning recommendations (pre-AI)

Before any AI features, future "what to learn next" guidance should use **deterministic, rule-based** recommendations curated by admins:

```text
Completed Java → recommend Spring Boot → recommend Docker → recommend AWS
```

| Aspect | Guidance |
|--------|----------|
| Data model | Technology adjacency table or `recommendedNextTechnologyId` |
| Trigger | Roadmap complete or Technology marked complete |
| UX | "Suggested next" card on My Journey or Technology detail |
| AI | Only after rule-based approach is proven — see §1 AI opportunities |
| v0.8.0 | **Out of scope** — use static Next up within enrolled Roadmap |

This is intentionally simpler, auditable, and does not require AI infrastructure.

---

### 1.9 AI implementation prerequisites

| Prerequisite | Description |
|--------------|-------------|
| Catalog maturity | Sufficient published content for meaningful recommendations |
| Enterprise AI policy | Approved LLM provider, data handling, audit |
| Feature flag | `app.learn.ai.enabled` default false |
| Human oversight | Admin review of AI-curated suggestions before publish (optional mode) |

---

## 2. Learning experience enhancements

### 2.1 Multiple Roadmaps per Technology

Allow beginner and advanced Roadmaps for the same Technology (e.g., "AWS Fundamentals" vs "AWS Solutions Architect prep").

**Complexity:** Enrollment model, UI selection, Career Path references.

### 2.2 Sequential Stage locking

Hard-lock Stages until prior Stage is complete (configurable per Roadmap).

**Trade-off:** More structure vs self-paced flexibility. v0.8.0 uses soft recommendations.

### 2.3 Learning notes

Employee-private notes per Stage ("I struggled with IAM policies").

**Value:** Personal knowledge capture without social features.

### 2.4 Bookmarks

Save Resources and Certifications to a personal bookmark list independent of enrollment.

### 2.5 Peer progress (anonymous)

Show aggregate completion rates per Stage ("72% of learners found Stage 3 helpful") without identifying individuals.

**Privacy:** Opt-in org setting; aggregate only.

### 2.6 Learn leaderboards

Leaderboards for Roadmap completion velocity — distinct from Initiative leaderboards.

**Risk:** May conflict with "learning not competition" principle. Requires product review.

---

## 3. Content and curation enhancements

### 3.1 Bulk resource import

CSV import of Learning Resources with Stage assignment.

**Columns:** title, url, type, provider, stageOrder, technologyName

### 3.2 Automated link health monitoring

Scheduled job checks all Resource URLs; admin dashboard of broken links.

### 3.3 Resource versioning

Track URL changes over time; notify employees who visited old URL.

### 3.4 Content templates

Reusable Roadmap templates (e.g., "Language fundamentals" pattern) for faster admin authoring.

### 3.5 Study Materials cross-link

Optional "Also in Study Materials" badge when a Learn Resource URL matches a Study Material link.

**Scope:** Read-only deduplication hint; no sync.

### 3.6 Projects module enhancements (independent release)

Full Projects module UI for organizational knowledge (overview, BRD, FRD, architecture, KT, deployment guides, team info) ships independently of Learn. Learn provides cross-navigation to Projects only — it does not own or duplicate project content.

---

## 4. Certification and submission enhancements

### 4.1 Certification FK on certificate submission

Link approved submissions to Certification catalog entries for RECORDED state automation.

### 4.2 Certification pathway suggestions

After RECORDED, suggest "next Certification" in the same provider track.

### 4.3 Exam date tracking

Employee records planned exam date; Dashboard reminder (not deadline enforcement).

### 4.4 Rejected submission resubmission

Existing backlog item — enables Learn → Certify → Resubmit loop.

---

## 5. Initiative integration enhancements

### 5.1 Initiative-required Technologies

Initiative specifies Technologies that count toward campaign progress (optional overlay on Learn).

**Rule:** Still must not block Learn usage without initiative.

### 5.2 Team-based initiative progress

Show team aggregate Roadmap progress on Initiative detail (manager view via Admin).

**Role tension:** Conflicts with two-role model — needs explicit approval.

### 5.3 Initiative templates

Create Initiative from Certification with pre-filled title, dates, reward text.

---

## 6. Platform enhancements

### 6.1 Global Search

Search across Learn, Initiatives, Users (admin), Certifications, Resources.

**Backlog:** Listed in `docs/project-roadmap.md` as v0.8+ candidate.

### 6.2 Email notifications for Learn

| Event | Channel |
|-------|---------|
| Roadmap 50% complete | In-app (future) / email (optional) |
| Certification READY | In-app + email |
| New Career Path published | Digest email |

Uses existing notification infrastructure pattern from v0.6.

### 6.3 Dashboard drilldowns (deferred from v0.6.2)

Active initiatives, top learners — extend to Learn adoption metrics.

### 6.4 Employee certificate download

Deferred from v0.6.2 — complements RECORDED state in Learn.

### 6.5 Manager analytics (explicitly low priority)

Conflicts with two-role simplicity unless managers are granted Admin. Not recommended.

---

## 7. Technical enhancements

### 7.1 Readiness API for third-party integrations

Expose employee readiness (with consent) to HR systems or LMS gateways.

### 7.2 xAPI / SCORM (not recommended)

Would move toward LMS territory — out of alignment with product vision unless strategy changes.

### 7.3 Offline Roadmap viewing

PWA cache of Roadmap structure (not Resources) for low-connectivity scenarios.

### 7.4 Content CDN for metadata

Cache published catalog JSON for fast Learn Home load.

---

## 8. Prioritization matrix (indicative)

| Enhancement | User value | Effort | Recommended release |
|-------------|------------|--------|---------------------|
| Automated link checker | High | Low | v0.8.1 |
| Certification FK on submission | High | Medium | v0.8.1 |
| Bulk resource import | Medium | Medium | v0.8.1 |
| Global Search | High | High | v0.9.0 |
| AI Path recommendation | High | High | v0.9.0+ |
| Learn leaderboards | Medium | Medium | v0.9.0 (if approved) |
| Conversational guide | Medium | Very high | v1.0+ |
| Multiple Roadmaps per Tech | Medium | High | v0.9.0 |
| Email Learn notifications | Medium | Medium | v0.9.0 |

---

## 9. Architecture hooks for future AI

v0.8.0 implementation should preserve extension points without building AI:

| Hook | Purpose |
|------|---------|
| `LearnRecommendationService` interface | Pluggable; default no-op implementation |
| `ReadinessCalculator` interface | v0.8.0: stage-based; future: ML-enhanced |
| Event emission | `StageCompleted`, `CertificationReady` events for downstream consumers |
| Feature flags | `app.learn.ai.*` configuration namespace |
| Catalog export API | Internal endpoint for RAG indexing (admin-only) |

---

## 10. Items explicitly not planned

| Item | Reason |
|------|--------|
| Hosted video lessons | Violates guidance-only principle |
| In-app quizzes | LMS territory |
| Proctored exams | Out of scope |
| Course certificates issued by ELH | Only industry certs from external providers |
| Manager role | Role simplicity |
| Social learning feed | Scope creep |
| Gamification points | Conflicts with initiative reward model |

---

**Return to:** [README.md](./README.md) | [00-product-design.md](./00-product-design.md)
