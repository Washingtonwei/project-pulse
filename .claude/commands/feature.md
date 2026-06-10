---
description: Implement a feature from its use case — plan, design, code, and test from docs/ram/requirements/use-cases.md
argument-hint: <UC-ID>  (e.g. /feature UC-DOC-5)
---

# /feature — Implement a use case end to end

Take the use case named in `$ARGUMENTS` (e.g. `UC-DOC-5`) and drive it from spec to working, tested code. The use case is the contract; the linked FRs are the acceptance criteria; the glossary fixes the vocabulary.

**The spec is authoritative but not infallible — challenge it.** As you drive the use case down into design and code, you will hit steps that are ambiguous or underspecified, assumptions that don't survive contact with the existing code, requirements that contradict each other or reality, and — because the docs are written by humans and age — **unnecessary or incorrect constraints, out-of-date information, wrong assumptions, or an approach the spec suggests that simply isn't best practice or the most efficient one.** When that happens, **ask the developer a clarifying question or push back — do not silently comply, do not silently invent the missing detail, and do not inherit a constraint just because it's written down.** Name the gap or the better way, say what you'd do and why, and when it's a genuine spec defect propose the doc edit (per `docs/ram/CLAUDE.md`, then `/build`). A step with no backing FR, or a cited FR that doesn't exist, is a question to raise — not something to paper over. **When in doubt, or when you see a better way, shout** — this holds in every phase, not just at the end.

If no UC-ID is given, ask which use case to implement (or list candidate use cases that have no row, or an incomplete row, in `docs/ram/traceability.md`).

Work in phases. **Stop for approval after Phase 2 before writing any code.**

## Phase 1 — Understand the spec (read only)

Read widely — pull in as much relevant context as helps — but read **skeptically**. These docs are the contract, yet they are not infallible: they can carry unnecessary or incorrect constraints, out-of-date information, wrong assumptions, or a less-than-best-practice approach. Note anything that looks off and surface it in Phase 2 (or sooner if it blocks understanding) — see the "challenge it" principle above. Don't silently inherit a constraint just because it's written down.

1. Read the target use case in `docs/ram/requirements/use-cases.md` (locate it with Grep on `### **UC-` and the area-prefixed ID, e.g. `UC-DOC-5` — never read that file whole; use cases are H3 headings grouped under H2 area headings). Capture: primary/secondary actors, trigger, preconditions, postconditions, main success scenario, extensions, business rules, associated information, related use cases.
2. Resolve its **functional requirements**: the use case's own "The system …" steps + Associated Information **are** its detailed functional spec and primary acceptance criteria (a use case is a high-level FR). Then add the SRS §5.2 cross-cutting system-level FRs it depends on (`FR-<AREA>-<n>` format: `FR-SAVE-*` autosave, `FR-LOCK-*` locking, `FR-COL-*` collaboration, `FR-VAL-*` validation, `FR-AI-*` AI, `FR-TPL-*` templates, `FR-GLO-*` glossary, `FR-HIS-*` history & authorship metadata, `FR-SEC-*` security, `FR-EXP-*` export, `FR-IMP-*` import, `FR-PERF-*` performance, `FR-NOT-*` notifications) — the traceability matrix lists these per use case.
3. Resolve **vocabulary**: look up the domain terms the use case uses in `docs/ram/requirements/project-glossary.md`. Use the defined terms in code identifiers and UI strings; do not introduce synonyms.
4. Check **`docs/ram/traceability.md`** for an existing row: is any of this already built? Reuse it; don't duplicate. Note any existing design doc for the area under `docs/ram/design/`.
5. Identify **cross-cutting behavior already specified** and plan to reuse it rather than reinvent: locking (`FR-LOCK-*`, UC-DOC-2/UC-DOC-6), real-time collaboration (`FR-COL-*`, UC-COL-1), validation (`FR-VAL-*`, UC-VAL-1), notifications (Gmail SMTP), and the existing course/section/team/auth model.
6. Read the **SRS sections that constrain the design**, not just the use case: §4.2 Container Diagram and §4.4 Design & Implementation Constraints (fit the *unified* Project Pulse architecture — don't invent a parallel one), §4.3/§4.5 Operating Environment / Assumptions & Dependencies, §7.1 Business Domain Model (your entities/tables must align to it), §9 Quality Attributes (performance/security/availability are non-functional acceptance criteria even when no UC step names them), and §8.2 Software Interfaces / §8.3 API Document for AI/CFG/EXP areas (the LLM service). Treat §4.4 and §9 as binding — but skeptically (a constraint can be stale or wrong; if so, flag it, don't just obey it).
7. Locate the relevant **existing code** for the area touched (frontend views/store/api, backend controller/service/repository/entity, DB migrations). Note what already exists to extend.

## Phase 2 — Plan (propose, then wait for approval)

Produce a concise implementation plan and present it for approval before coding. Include:

- **Acceptance criteria** — the main-flow steps + extensions restated as testable outcomes, each tied to an FR ID. **Flag any step with no backing FR** (a spec gap to confirm or fill) and any referenced FR that doesn't exist.
- **Design doc** — which `docs/ram/design/` doc (the touched UC area) you'll create or extend, and the key design decisions to record there: components/classes, sequence, API contracts, DB schema. The design doc sits below the SRS — it cites the UC/FRs it realizes, never restates them.
- **Design diagram(s) — design-first.** Include **at least one Mermaid diagram in the plan itself** (a sequence diagram of the main success scenario, or a class/ER sketch of the new structure) so the design's shape is reviewed *at the approval gate*, not after coding. Name the full set of diagrams the design doc will carry — sequence / class / ER / state as the area needs (e.g. a **state** diagram for a lock or review/submission lifecycle). See `docs/ram/design/README.md` for which diagram fits which concern.
- **Backend changes** — endpoints (method + path), service logic, repository/entity changes, DB schema/migration. Note auth/permission rules (who may do this — Student on own team, Instructor on assigned section, etc.).
- **Frontend changes** — views/components, store/state, API client calls, key UI states from the flow (including extension/error paths).
- **Tests** — backend unit/integration tests and frontend tests, mapped to the acceptance criteria. Traceability cites at the **use-case level** (one row per UC, no finer handle), so this UC's tests must cover the *entire* main success scenario **and every extension** — the error/extension flows are tests too, not just the happy path. An untested extension is an untested requirement with nowhere else to surface.
- **Reuse vs. new** — which existing modules are extended vs. what's genuinely new.
- **Open questions / risks.**

**Enter plan mode to present this plan and wait for explicit approval.** If the harness is not already in plan mode, enter it before presenting the plan. Edit nothing in this phase — not code, not docs, not the design doc (the Phase 1 reads are the only filesystem access allowed before approval). Proceed to Phase 3 only once the plan is approved.

## Phase 3 — Implement (after approval)

Implement back to front, matching existing conventions:

0. Write or update the area's design doc under `docs/ram/design/` to reflect the approved plan (components/classes, sequence, API contracts, DB schema) — the design of record the code realizes.
1. Backend: entity/migration → repository → service (incl. permission checks and the lock/collaboration/validation hooks the FRs require) → controller.
2. Frontend: API client → store → view/components, covering the main flow and the extension/error states.
3. Keep changes scoped to the use case; reuse existing utilities, services, and components.
4. If implementation surfaces a contradiction or a decision the spec doesn't settle, **pause and raise it** rather than guessing — see the "challenge it" principle at the top.

## Phase 4 — Test & verify

1. Add/extend automated tests for every acceptance criterion, including extension and exception flows.
2. Run the backend and frontend test suites (see run/test commands in the repo-root `CLAUDE.md`) and make them pass.
3. Where practical, exercise the feature in the running app to confirm the main success scenario behaves as the use case describes.
4. Report what passed, what was skipped and why — faithfully.

## Phase 5 — Trace it back

Add or update the use case's row in `docs/ram/traceability.md`: UC-ID, FR IDs, design doc, frontend modules, backend modules, DB changes, tests, status. This keeps the spec→code map navigable for the next feature.

If implementation revealed the use case or an FR is wrong, incomplete, or contradictory, say so and propose the doc edit (follow `docs/ram/CLAUDE.md`; run `/build` after). The spec and the code are meant to agree.
