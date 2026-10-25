---
description: Implement a use case from its approved design — plan, code, and test from the area's docs/design doc. Run /design first.
argument-hint: <UC-ID>  (e.g. /implement UC-DOC-create-use-case)
---

# /implement — Implement a use case from its design

Take the use case named in `$ARGUMENTS` (e.g. `UC-DOC-create-use-case`) and drive it from its **design-of-record** to working, tested code. This is the **second** of the two build stages: `/design <UC-ID>` produces the Level-2 design doc under `docs/design/`; `/implement` implements it. The use case is the contract; the linked FRs are the acceptance criteria; the glossary fixes the vocabulary; the **design doc is the *how* you implement**.

**Precondition: the design must exist.** This command builds from an approved design; it does not invent one. Before planning, confirm the area's design doc (`docs/design/<area>.md`) exists and covers this use case (the `📐 Designed` Build state in `docs/traceability.md`, or a *Realizes:* header listing this `UC-`). **If there is no design for this use case, stop and tell the developer to run `/design <UC-ID>` first** (or offer to switch to designing it) — don't design-on-the-fly inside the code stage.

**Precondition: a fresh session.** The build reads the merged design, not the conversation that produced it. If this session already carries the `/design` conversation (or any unrelated work), ask the developer to run `/clear` and start again. Anything the documents do not answer, but that conversation did, is a gap in the design: ask, then fix `docs/design/<area>.md` in this branch (Phase 3, step 4) and name the change under Reviewer focus. The session continues across the trunk pause in Phase 3; clearing happens between gates, not within one.

**The spec and the design are authoritative but not infallible — challenge them.** As you drive the design into code, you will hit ambiguous or underspecified steps, assumptions that don't survive contact with the existing code, requirements or design choices that contradict each other or reality, and — because the docs are written by humans and age — **unnecessary or incorrect constraints, out-of-date information, wrong assumptions, or an approach that simply isn't best practice or the most efficient one.** When that happens, **ask the developer a clarifying question or push back — do not silently comply, do not silently invent the missing detail, and do not inherit a constraint just because it's written down.** Name the gap or the better way, say what you'd do and why; when it's a genuine spec defect propose the doc edit (per `docs/CLAUDE.md`, then `/spec-build`), and when it's a *design* defect update `design/<area>.md` (per `docs/design/README.md`). A step with no backing FR, or a cited FR that doesn't exist, is a question to raise — not something to paper over. **When in doubt, or when you see a better way, shout** — this holds in every phase. Shouting means saying it, not doing it: a better way the use case does not need is proposed, never built unasked (Phase 3).

If no UC-ID is given, ask which use case to implement (or list candidate use cases whose **Build** state is `📐 Designed` in `docs/traceability.md`, designed and ready to build, in **Release** order, earliest release first).

Work in phases. The design was already reviewed and approved in `/design`, so `/implement` **has no second approval gate on the design. It pauses once, after the trunk commit** (Phase 3), so the developer reads the shared code before the leaves are built on it. The other thing that stops you is trouble: if the design contradicts the spec or the existing code, or a decision neither settles, **pause and raise it** (see the "challenge them" principle above) rather than guessing.

## Phase 1 — Understand the spec and the design (read only)

Read widely but **skeptically** — these docs are the contract, yet they can carry stale constraints, wrong assumptions, or a less-than-best-practice approach. Surface anything that looks off in Phase 2 (or sooner if it blocks understanding).

1. Read the **design-of-record** — `docs/design/<area>.md` for this UC's area. This is what you implement: its diagrams, components, key decisions, and data-model delta are the plan of how the code is shaped. Its Level-1 parent is the architecture-of-record, `docs/design/architectural-design.md`: skim its Container Diagram and RAM component view so you build inside the *unified* Project Pulse architecture, not a parallel one, and read its Crosscutting Concepts in full (step 6).
2. Read the target use case in `docs/requirements/use-cases.md` (locate it with Grep on `### **UC-` and the area-prefixed ID, e.g. `UC-DOC-create-use-case` — never read that file whole). Capture: primary/secondary actors, trigger, preconditions, postconditions, main success scenario, extensions, business rules, associated information, related use cases. These are the flows your tests must cover.
3. Resolve its **functional requirements**: the use case's own "The system …" steps + Associated Information **are** its detailed functional spec and primary acceptance criteria (a use case is a high-level FR). Add the SRS's cross-cutting non-use-case FRs it depends on (`FR-<AREA>-<slug>`: `FR-SAVE-*`, `FR-VAL-*`, `FR-AI-*`, `FR-TPL-*`, `FR-GLO-*`, `FR-HIS-*`, `FR-SEC-*`, `FR-NOT-*`) — the traceability matrix lists these per use case. (Section locking, real-time collaboration, export, and import have no FR — they live in their use cases' steps, plus `BR-edit-lock-required`/`BR-lock-expiry` for locking, UC-COL-collaborative-edit for real-time collaboration, and the `SI-*` interface requirements for export/import.)
4. Resolve **vocabulary**: look up the domain terms in `docs/requirements/project-glossary.md`. Use the defined terms in code identifiers and UI strings; do not introduce synonyms.
5. Check **`docs/traceability.md`** for the existing row: is any of this already built? Reuse it; don't duplicate.
6. Read the architecture-of-record's **Crosscutting Concepts** section in full: Architectural conventions, the two cross-cutting subsystem tables, Security & Compliance › Authorization, and Data architecture. It is the one normative home of the conventions every feature follows (the `Result` envelope and error mapping, caller identity through `UserUtils`, calendar vs elapsed time, the two authorization enforcement points, transactions and schema migrations, the client ↔ API contract), and it names the machinery already specified and built that you plug into rather than reinvent (locking, templates, comment threads, validation, notifications, and the rest). Where existing code breaks a convention, follow the convention, not the code, and note the drift for the report. The SRS's Quality Attributes (performance/security/availability) are non-functional acceptance criteria even when no UC step names them.
7. Locate the relevant **existing code** for the area touched (frontend views/store/api, backend controller/service/repository/entity, DB migrations). Note what already exists to extend.

**If the design doc and the spec or the existing code disagree, flag it now** — don't quietly implement around a stale design. Update `design/<area>.md` (or raise a spec defect) before building on a contradiction.

## Phase 2 — Plan the implementation (then proceed, no approval gate)

Work out a concise implementation plan from the approved design and **proceed directly into Phase 3** — do not stop for approval. This plan is your own working map (the *design's shape* was already approved in `/design`); state it briefly so the build and its tests are deliberate, not improvised. Cover:

- **Acceptance criteria** — the main-flow steps + extensions restated as testable outcomes. These steps *are* the use case's own functional requirement, so they tie to the use case itself, not a separate FR ID. Tie a step to a specific non-use-case FR ID *only* where it invokes cross-cutting behavior (autosave `FR-SAVE-*`, validation `FR-VAL-*`, …). (A lock step ties to the editing use case itself + `BR-edit-lock-required`/`BR-lock-expiry`, not to an FR.) **Flag any step that relies on cross-cutting behavior with no backing non-use-case FR**, and any cited FR that doesn't exist.
- **Backend changes** — endpoints (method + path), service logic, repository/entity changes, DB schema/migration — realizing the design's components and data-model delta. Note auth/permission rules (who may do this — Student on own team, Instructor on assigned course section, etc.).
- **Frontend changes** — views/components, store/state, API client calls, key UI states from the flow (including extension/error paths).
- **Tests** — backend unit/integration tests and frontend tests, mapped to the acceptance criteria. Traceability cites at the **use-case level** (one row per UC, no finer handle), so this UC's tests must cover the *entire* main success scenario **and every extension** — the error/extension flows are tests too. An untested extension is an untested requirement with nowhere else to surface.
- **Conformance to the design** — note where the plan follows `design/<area>.md` and call out **any deviation** the implementation will require (which then updates the design doc in Phase 3/5). The design's diagrams are the reference — don't redraw them here.
- **Reuse vs. new** — which existing modules are extended vs. what's genuinely new.
- **Open questions / risks** — and if any is a true blocker (a design/spec contradiction, a missing decision), **pause and raise it** before coding rather than guessing.

State the plan briefly, then move on to Phase 3. There is no approval gate here and no scratch file — the gate was `/design`.

## Phase 3 — Implement

Work on a branch, never on `main`, and **commit in steps a reviewer can read, cut along the design**. Code the developer accepted without understanding is cognitive debt, and the next session builds on it.

- **Trunk first, in a commit of its own:** the trunk as `docs/methodology.md` Phase C step 2 defines it (code many paths depend on, code that changes behavior existing users rely on, or code that is hard to roll back). In practice that is the new or changed services, queries, and entities the design's class diagram puts under more than one flow, plus any change to existing behavior. Then **stop**, tell the developer which commit to read, and wait. Ask them to say what changed and why; continue when they can.
- **Then one commit per leaf:** each flow's own controller, view, or job.
- **A security rule (`SecurityConfiguration`) or a schema migration goes in a commit of its own**, so the reviewer sees it alone.
- **A refactor the use case needs goes in a commit of its own**, before the code that uses it: no change in behavior, the existing tests green before and after, and finished everywhere the old way appears, so no second way of doing the same thing is left behind.
- **Each commit carries the tests for what it adds** and passes them, run as targeted test classes (`-Dtest=...`), not the full suite. Phase 4 runs the full suites once, before the pull request opens.
- **Small use case, no trunk?** One commit and no pause, except that a security rule or schema migration still goes in a commit of its own.

Within each commit, implement back to front, matching existing conventions and the approved design:

1. Backend: entity/migration → repository → service (incl. permission checks and the lock/collaboration/validation hooks the FRs require) → controller.
2. Frontend: API client → store → view/components, covering the main flow and the extension/error states.
3. Keep changes scoped to the use case; reuse existing utilities, services, and components. An improvement the use case does not need (a rename, a tidier method, a convention fixed elsewhere, shared behavior changed for every caller) is proposed, not made: list it in the pull request description under Reviewer focus as a follow-up.
4. **Keep the design-of-record honest.** If implementation forced a change from the approved design (a different class boundary, an extra table, a revised flow), update `docs/design/<area>.md` to match — revise the area-wide class/ER diagram in place, adjust the affected sequence. The design doc must describe what was actually built.
5. If implementation surfaces a contradiction or a decision neither the spec nor the design settles, **pause and raise it** rather than guessing — see the "challenge them" principle at the top.

## Phase 4 — Test & verify

1. Add/extend automated tests for every acceptance criterion, including extension and exception flows.
2. **Check the diff against Crosscutting Concepts** before running anything. At minimum: every new endpoint has a route rule **and** a team-scoped lookup, with a test for each refusal (`403` at the route, `404` for another team's id through your own team's URL); ids in request bodies are re-resolved through a scoped finder; DTO request bodies carry `@Valid`; the caller's identity comes from `UserUtils`; calendar time reads the injected `Clock` and elapsed time the real clock; a schema change has a Flyway migration and matching `DataInitializer` seed data; frontend calls go through an `src/apis/<feature>/` module on the shared `request` instance. Fix a miss, or record it as a deliberate deviation in the report.
3. Once every commit is in, run the full backend and frontend test suites (see run/test commands in the repo-root `CLAUDE.md`) and make them pass. The backend suite takes several minutes, so ask the developer to run it if they prefer, and report its result either way.
4. Where practical, exercise the feature in the running app to confirm the main success scenario behaves as the use case describes.
5. Report what passed, what was skipped and why — faithfully.

## Phase 5 — Trace it back

Update the use case's row in `docs/traceability.md`: set **Build** from `📐 Designed` to the built state (`🟡 In progress` or `✅ Built`), and set **Verify** independently from the tests you actually wrote (`✅ Verified` only if tests exercise the main flow *and* every extension and pass; `🟡 Partial` if some flows/extensions are untested; `🔎 None` if you built it without a passing test). Then fill FR IDs, frontend modules, backend modules, DB changes, and tests — and in the **Tests** cell, per the extension-coverage convention, note which extensions each test covers (e.g. `…(E1,E3); E2 untested`). Don't mark `✅ Built` / `✅ Verified` to mean "done" — a built-but-untested use case is `✅ Built` / `🔎 None`, and that honesty is the point. This keeps the spec→code map navigable for the next feature. The design doc stays at `design/<area>.md`; make sure it reflects any change Phase 3 made.

If implementation revealed the use case or an FR is wrong, incomplete, or contradictory, say so and propose the doc edit (follow `docs/CLAUDE.md`; run `/spec-build` after). The spec, the design, and the code are meant to agree.

## Phase 6: Open the pull request

The commits from Phase 3 are already on the branch; keep them separate so the reviewer can read commit by commit. Do not squash them: the repository merges pull requests with a merge commit, so the trunk and leaf commits stay readable on `main` too. Draft the pull request description in the shape of `.github/PULL_REQUEST_TEMPLATE.md`, at most three lines per section:

- **Traces to:** the use case ID and `docs/design/<area>.md`.
- **How it was verified:** under Verified, the tests mapped to the design's test-list rows and what Phase 4 ran. Under Not verified, every extension the traceability row marks untested and anything Phase 4 skipped. Never state a confidence level.
- **Reviewer focus:** which commit is the trunk, any refactor commit and why the use case needed it, then every change to shared code (the shared foundation or a cross-cutting subsystem), first among them any new route rule in `SecurityConfiguration`, and any change this pull request makes to `docs/design/<area>.md`, named as a design change.

Recommend the developer run a fresh-context review (a new session, or `/code-review`) before requesting a human reviewer, and add its findings under How it was verified. Then tell the developer the pull request is ready for a reviewer who did not write it, and that once it merges they close the use case (`docs/methodology.md`, Phase C step 4) before starting the next.
