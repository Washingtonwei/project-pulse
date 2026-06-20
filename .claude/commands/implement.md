---
description: Implement a use case from its approved design — plan, code, and test from the area's docs/design doc. Run /design first.
argument-hint: <UC-ID>  (e.g. /implement UC-DOC-5)
---

# /implement — Implement a use case from its design

Take the use case named in `$ARGUMENTS` (e.g. `UC-DOC-5`) and drive it from its **design-of-record** to working, tested code. This is the **second** of the two build stages: `/design <UC-ID>` produces the Level-2 design doc under `docs/design/`; `/implement` implements it. The use case is the contract; the linked FRs are the acceptance criteria; the glossary fixes the vocabulary; the **design doc is the *how* you implement**.

**Precondition — the design must exist.** This command builds from an approved design; it does not invent one. Before planning, confirm the area's design doc (`docs/design/<area>.md`) exists and covers this use case (the `📐 Designed` status in `docs/traceability.md`, or a *Realizes:* header listing this `UC-`). **If there is no design for this use case, stop and tell the developer to run `/design <UC-ID>` first** (or offer to switch to designing it) — don't design-on-the-fly inside the code stage.

**The spec and the design are authoritative but not infallible — challenge them.** As you drive the design into code, you will hit ambiguous or underspecified steps, assumptions that don't survive contact with the existing code, requirements or design choices that contradict each other or reality, and — because the docs are written by humans and age — **unnecessary or incorrect constraints, out-of-date information, wrong assumptions, or an approach that simply isn't best practice or the most efficient one.** When that happens, **ask the developer a clarifying question or push back — do not silently comply, do not silently invent the missing detail, and do not inherit a constraint just because it's written down.** Name the gap or the better way, say what you'd do and why; when it's a genuine spec defect propose the doc edit (per `docs/CLAUDE.md`, then `/spec-build`), and when it's a *design* defect update `design/<area>.md` (per `docs/design/README.md`). A step with no backing FR, or a cited FR that doesn't exist, is a question to raise — not something to paper over. **When in doubt, or when you see a better way, shout** — this holds in every phase.

If no UC-ID is given, ask which use case to implement (or list candidate use cases that are `📐 Designed` in `docs/traceability.md` — designed and ready to build).

Work in phases. The design was already reviewed and approved in `/design`, so `/implement` **runs straight through to code — there is no second approval gate.** The one thing that still stops you is trouble: if the design contradicts the spec or the existing code, or a decision neither settles, **pause and raise it** (see the "challenge them" principle above) rather than guessing.

## Phase 1 — Understand the spec and the design (read only)

Read widely but **skeptically** — these docs are the contract, yet they can carry stale constraints, wrong assumptions, or a less-than-best-practice approach. Surface anything that looks off in Phase 2 (or sooner if it blocks understanding).

1. Read the **design-of-record** — `docs/design/<area>.md` for this UC's area. This is what you implement: its diagrams, components, key decisions, and data-model delta are the plan of how the code is shaped. Also skim its Level-1 parent, the architecture-of-record `docs/design/architectural-design.md` (the Container Diagram and conventions the design sits under, plus the RAM component view and cross-cutting subsystems), so you build inside the *unified* Project Pulse architecture, not a parallel one.
2. Read the target use case in `docs/requirements/use-cases.md` (locate it with Grep on `### **UC-` and the area-prefixed ID, e.g. `UC-DOC-5` — never read that file whole). Capture: primary/secondary actors, trigger, preconditions, postconditions, main success scenario, extensions, business rules, associated information, related use cases. These are the flows your tests must cover.
3. Resolve its **functional requirements**: the use case's own "The system …" steps + Associated Information **are** its detailed functional spec and primary acceptance criteria (a use case is a high-level FR). Add the SRS's cross-cutting non-use-case FRs it depends on (`FR-<AREA>-<n>`: `FR-SAVE-*`, `FR-LOCK-*`, `FR-COL-*`, `FR-VAL-*`, `FR-AI-*`, `FR-TPL-*`, `FR-GLO-*`, `FR-HIS-*`, `FR-SEC-*`, `FR-EXP-*`, `FR-IMP-*`, `FR-PERF-*`, `FR-NOT-*`) — the traceability matrix lists these per use case.
4. Resolve **vocabulary**: look up the domain terms in `docs/requirements/project-glossary.md`. Use the defined terms in code identifiers and UI strings; do not introduce synonyms.
5. Check **`docs/traceability.md`** for the existing row: is any of this already built? Reuse it; don't duplicate.
6. Identify **cross-cutting behavior already specified and built** and reuse it rather than reinvent: locking (`FR-LOCK-*`, UC-DOC-2/UC-DOC-6), real-time collaboration (`FR-COL-*`, UC-COL-1), validation (`FR-VAL-*`, UC-VAL-1), notifications (Gmail SMTP), and the existing course/section/team/auth model. The SRS's Quality Attributes (performance/security/availability) are non-functional acceptance criteria even when no UC step names them.
7. Locate the relevant **existing code** for the area touched (frontend views/store/api, backend controller/service/repository/entity, DB migrations). Note what already exists to extend.

**If the design doc and the spec or the existing code disagree, flag it now** — don't quietly implement around a stale design. Update `design/<area>.md` (or raise a spec defect) before building on a contradiction.

## Phase 2 — Plan the implementation (then proceed, no gate)

Work out a concise implementation plan from the approved design and **proceed directly into Phase 3** — do not stop for approval. This plan is your own working map (the *design's shape* was already approved in `/design`); state it briefly so the build and its tests are deliberate, not improvised. Cover:

- **Acceptance criteria** — the main-flow steps + extensions restated as testable outcomes. These steps *are* the use case's own functional requirement, so they tie to the use case itself, not a separate FR ID. Tie a step to a specific non-use-case FR ID *only* where it invokes cross-cutting behavior (locking `FR-LOCK-*`, autosave `FR-SAVE-*`, validation `FR-VAL-*`, collaboration `FR-COL-*`, …). **Flag any step that relies on cross-cutting behavior with no backing non-use-case FR**, and any cited FR that doesn't exist.
- **Backend changes** — endpoints (method + path), service logic, repository/entity changes, DB schema/migration — realizing the design's components and data-model delta. Note auth/permission rules (who may do this — Student on own team, Instructor on assigned course section, etc.).
- **Frontend changes** — views/components, store/state, API client calls, key UI states from the flow (including extension/error paths).
- **Tests** — backend unit/integration tests and frontend tests, mapped to the acceptance criteria. Traceability cites at the **use-case level** (one row per UC, no finer handle), so this UC's tests must cover the *entire* main success scenario **and every extension** — the error/extension flows are tests too. An untested extension is an untested requirement with nowhere else to surface.
- **Conformance to the design** — note where the plan follows `design/<area>.md` and call out **any deviation** the implementation will require (which then updates the design doc in Phase 3/5). The design's diagrams are the reference — don't redraw them here.
- **Reuse vs. new** — which existing modules are extended vs. what's genuinely new.
- **Open questions / risks** — and if any is a true blocker (a design/spec contradiction, a missing decision), **pause and raise it** before coding rather than guessing.

State the plan briefly, then move on to Phase 3. There is no approval gate here and no scratch file — the gate was `/design`.

## Phase 3 — Implement

Implement back to front, matching existing conventions and the approved design:

1. Backend: entity/migration → repository → service (incl. permission checks and the lock/collaboration/validation hooks the FRs require) → controller.
2. Frontend: API client → store → view/components, covering the main flow and the extension/error states.
3. Keep changes scoped to the use case; reuse existing utilities, services, and components.
4. **Keep the design-of-record honest.** If implementation forced a change from the approved design (a different class boundary, an extra table, a revised flow), update `docs/design/<area>.md` to match — revise the area-wide class/ER diagram in place, adjust the affected sequence. The design doc must describe what was actually built.
5. If implementation surfaces a contradiction or a decision neither the spec nor the design settles, **pause and raise it** rather than guessing — see the "challenge them" principle at the top.

## Phase 4 — Test & verify

1. Add/extend automated tests for every acceptance criterion, including extension and exception flows.
2. Run the backend and frontend test suites (see run/test commands in the repo-root `CLAUDE.md`) and make them pass.
3. Where practical, exercise the feature in the running app to confirm the main success scenario behaves as the use case describes.
4. Report what passed, what was skipped and why — faithfully.

## Phase 5 — Trace it back

Update the use case's row in `docs/traceability.md`: flip **Status** from `📐 Designed` to the built state (`🟡 In progress`, `🔎 Needs verification`, or `✅ Implemented`), and fill FR IDs, frontend modules, backend modules, DB changes, and tests. This keeps the spec→code map navigable for the next feature. The design doc stays at `design/<area>.md`; make sure it reflects any change Phase 3 made.

If implementation revealed the use case or an FR is wrong, incomplete, or contradictory, say so and propose the doc edit (follow `docs/CLAUDE.md`; run `/spec-build` after). The spec, the design, and the code are meant to agree.
