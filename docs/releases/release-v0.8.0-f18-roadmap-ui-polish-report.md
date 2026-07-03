# F18 Roadmap Page — UI Polish Report

**Phase:** F18 (UI-only polish)  
**Scope:** Learning Roadmap page visual hierarchy and progress clarity  
**No API, database, routing, or business logic changes**

## Summary

Polished the Learning Roadmap page to feel more like a modern learning platform: compact overview, prominent progress, clearer timeline, and stronger stage card states. All F18 functionality remains unchanged.

## Before / After Screenshots

| Viewport | Before | After |
|----------|--------|-------|
| Desktop (1280px) | `docs/screenshots/f18-roadmap-ui-polish/before/before-desktop.png` | `docs/screenshots/f18-roadmap-ui-polish/after/after-desktop.png` |
| Tablet (834px) | `docs/screenshots/f18-roadmap-ui-polish/before/before-tablet.png` | `docs/screenshots/f18-roadmap-ui-polish/after/after-tablet.png` |
| Mobile (390px) | `docs/screenshots/f18-roadmap-ui-polish/before/before-mobile.png` | `docs/screenshots/f18-roadmap-ui-polish/after/after-mobile.png` |

## Changes by Focus Area

### 1. Roadmap Overview
- Replaced inline chip row with a responsive **stat grid** (Stages, Estimated duration, Remaining effort, Version).
- Reduced card padding and clamped description to two lines.
- Merged overview and continue-learning timeline into **one card** when enrolled (less vertical clutter).

### 2. Progress
- Progress is now the **primary visual element**: large percentage, 10px rounded bar, dedicated panel.
- Added **"X of Y stages completed"** text below the bar alongside the percentage.

### 3. Learning Timeline
- Custom step connector colors (success for completed, primary for active).
- Distinct step icons: check (completed), filled dot (current), outline (upcoming).
- Vertical stepper on mobile, horizontal on desktop.
- Current/next stage hints as clean text lines (preserved existing copy for tests).

### 4. Stage Cards
- **Current stage:** primary tint background, elevation, stronger border.
- **Completed stages:** success left accent bar, muted typography, outlined completed badge.
- **Upcoming stages:** slightly reduced opacity.
- Learning and practice resources grouped in **nested card panels** instead of dense lists.

### 5. Typography & Spacing
- Clear heading hierarchy: page → overview → timeline → stage titles.
- Consistent `2.5` spacing units between major sections.
- Compact back navigation link.

### 6. Responsive Design
- Stat grid: 2 columns on mobile, 4 on desktop when enrolled.
- Timeline switches orientation at `md` breakpoint.
- Resource metadata wraps cleanly; effort labels use `nowrap` where appropriate.

## Files Changed

| File | Change |
|------|--------|
| `frontend/src/pages/learn/RoadmapPage.tsx` | Overview grid, progress panel, timeline styling |
| `frontend/src/components/learn/RoadmapStageCard.tsx` | Stage states, resource group cards |
| `frontend/scripts/capture-roadmap-screenshots.mjs` | Screenshot helper (dev QA) |
| `frontend/scripts/roadmap-ui-smoke.mjs` | Browser smoke helper (dev QA) |
| `docs/screenshots/f18-roadmap-ui-polish/**` | Before/after screenshots |

## Test Results

| Check | Result |
|-------|--------|
| `RoadmapPage.test.tsx` | PASS (5 tests) |
| Learn regression tests (15 total) | PASS |
| `npm run build` | PASS |
| UI smoke: progress text, Complete Stage, snackbar | PASS |
| API smoke: enroll (409 existing), complete stage, journey | PASS |

## E2E Smoke Test (Roadmap Page)

1. Loaded Spring Boot roadmap with active enrollment — progress panel shows **50%** and **2 of 6 stages completed**.
2. Clicked **Complete Stage** — snackbar "Stage marked complete.", progress advanced to **50% → next stage Security**.
3. Continue learning hints and timeline updated without page errors.
4. Responsive layouts verified via Playwright at desktop, tablet, and mobile widths.

## Regression

- No changes to APIs, enrollment logic, or sequential completion rules.
- Existing test expectations preserved (`Start here: …`, `Next recommended stage: …`, `Complete Stage`).

## F19 Status

**F19 has NOT been started.**
