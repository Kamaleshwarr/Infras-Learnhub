# F18 Roadmap Hero Redesign — UI Polish Report

**Phase:** F18 (UI-only refinement)  
**Scope:** Learning Roadmap page — modern learning-platform hero experience  
**No backend, database, API, routing, or business logic changes**

## Summary

Redesigned the Roadmap page top section into a **progress-first hero** that immediately answers: what am I learning, how far have I progressed, and what should I do next. Replaced bulky stat cards with a compact metadata row and human-friendly effort summaries computed from stage-level estimates.

## Before / After Screenshots

| Viewport | Before | After |
|----------|--------|-------|
| Desktop (1280px) | `docs/screenshots/f18-roadmap-hero-redesign/before/before-desktop.png` | `docs/screenshots/f18-roadmap-hero-redesign/after/after-desktop.png` |
| Tablet (834px) | `docs/screenshots/f18-roadmap-hero-redesign/before/before-tablet.png` | `docs/screenshots/f18-roadmap-hero-redesign/after/after-tablet.png` |
| Mobile (390px) | `docs/screenshots/f18-roadmap-hero-redesign/before/before-mobile.png` | `docs/screenshots/f18-roadmap-hero-redesign/after/after-mobile.png` |

## Key Changes

### 1. Hero section (`RoadmapHero.tsx`)
- Progress-first layout: **33% Complete**, prominent bar, **2 of 6 stages completed**
- **Current Stage** / **Next Stage** callouts with **Continue Learning** CTA
- Gradient hero panel replaces PageHeader + metadata cards
- **Start Learning** CTA for unenrolled visitors

### 2. Compact metadata row
- Single horizontal row: `6 Stages · ≈ 8 weeks · 4 stages left · Remaining ≈ 5 weeks · Version 1.0`
- Version de-emphasized (muted color)
- Catalog source URL removed from hero (reduced admin-like clutter)

### 3. Human-friendly effort (`roadmapEffort.ts`)
- Parses stage `estimatedEffort` strings (e.g. `1 week`, `1-2 weeks`, `3-5 days`)
- Summarizes totals as `≈ 3 weeks`, `≈ 6–8 weeks`, or `≈ 2 months`
- Remaining effort computed from incomplete stages only — no concatenated strings

### 4. Learning journey timeline (`RoadmapJourneyTimeline.tsx`)
- ✓ completed (green), ● current (primary), ○ upcoming (grey)
- Keyboard-accessible step labels (Enter/Space scroll to stage)
- Compact container between hero and stage cards

### 5. Stage cards
- Tighter padding, stronger current-stage emphasis
- Grouped Learning / Practice resource sections
- Icons supplement color for completed/current/upcoming states

## Files Changed

| File | Change |
|------|--------|
| `frontend/src/components/learn/RoadmapHero.tsx` | New hero component |
| `frontend/src/components/learn/RoadmapJourneyTimeline.tsx` | New timeline component |
| `frontend/src/utils/roadmapEffort.ts` | Display-only effort summarization |
| `frontend/src/utils/roadmapEffort.test.ts` | Unit tests for effort formatting |
| `frontend/src/pages/learn/RoadmapPage.tsx` | Simplified layout using new components |
| `frontend/src/components/learn/RoadmapStageCard.tsx` | Tighter spacing, resource groups |
| `frontend/src/components/learn/learnMessages.ts` | Hero copy strings |
| `frontend/src/pages/learn/RoadmapPage.test.tsx` | Updated assertions for new hero |
| `frontend/src/routes/AppRoutes.test.tsx` | Updated heading assertion |

## Test Summary

| Check | Result |
|-------|--------|
| `roadmapEffort.test.ts` | PASS (4) |
| `RoadmapPage.test.tsx` | PASS (5) |
| `AppRoutes.test.tsx` | PASS |
| Learn regression tests | PASS |
| `npm run build` | PASS |
| UI smoke (Continue Learning, Complete Stage) | PASS |
| Backend health | UP |

## E2E Smoke Verification

- Active enrollment loads hero with progress %, stages completed, current/next stage
- **Continue Learning** scrolls to current stage card
- **Complete Stage** advances progress and updates hero
- Responsive layouts verified at desktop, tablet, mobile

## Business Logic Confirmation

- **No changes** to enrollment, stage completion, progress calculation APIs, or backend services
- Effort summaries are **display-only** transformations of existing stage `estimatedEffort` catalog fields
- Progress % and stage completion still sourced entirely from existing `/learn/progress` and `/learn/enrollments` APIs

## F19 Status

**F19 has NOT been started.**
