---
description: Design a use case from its spec — understand it, work out the design (diagrams + decisions), and write the area's design doc. Stops before any code.
argument-hint: <UC-ID>  (e.g. /design UC-DOC-5)
---

# /design — Turn a use case into an approved design-of-record

Take the use case named in `$ARGUMENTS` (e.g. `UC-DOC-5`) and produce its **design-of-record**: the Level-2 design doc for its UC area under `docs/ram/design/` (see [`../../docs/ram/design/README.md`](../../docs/ram/design/README.md)). This is the **first** of the two build stages — design is a deliberate, separately-reviewed stage, just like requirements. **It stops before any code.** `/implement <UC-ID>` is the second stage: it implements from the design this command leaves behind.

The use case is the contract; the linked FRs are the acceptance criteria; the glossary fixes the vocabulary. The design says only *how* the code will realize them — it **cites** the UC/FRs, never restates them.

**The spec is authoritative but not infallible — challenge it.** As you work the use case into a design, you will hit steps that are ambiguous or underspecified, assumptions that don't survive contact with the existing code, requirements that contradict each other or reality, and — because the docs are written by humans and age — **unnecessary or incorrect constraints, out-of-date information, wrong assumptions, or an approach the spec suggests that simply isn't best practice.** When that happens, **ask the developer a clarifying question or push back — do not silently comply, do not silently invent the missing detail, and do not inherit a constraint just because it's written down.** Name the gap or the better way, say what you'd do and why, and when it's a genuine spec defect propose the doc edit (per `docs/ram/CLAUDE.md`, then `/build`). A step with no backing FR, or a cited FR that doesn't exist, is a question to raise — not something to paper over. **When in doubt, or when you see a better way, shout.**

If no UC-ID is given, ask which use case to design (or list candidate use cases whose `docs/ram/traceability.md` status is `❌ Not started` — those with no design yet).

Work in phases. **Stop for approval after Phase 2 before writing the design doc.**

## Phase 1 — Understand the spec (read only)

Read widely — pull in as much relevant context as helps — but read **skeptically**. These docs are the contract, yet they are not infallible; note anything that looks off and surface it in Phase 2 (or sooner if it blocks understanding).

1. Read the target use case in `docs/ram/requirements/use-cases.md` (locate it with Grep on `### **UC-` and the area-prefixed ID, e.g. `UC-DOC-5` — never read that file whole; use cases are H3 headings grouped under H2 area headings). Capture: primary/secondary actors, trigger, preconditions, postconditions, main success scenario, extensions, business rules, associated information, related use cases.
2. Resolve its **functional requirements**: the use case's own "The system …" steps + Associated Information **are** its detailed functional spec and primary acceptance criteria (a use case is a high-level FR). Then add the SRS's cross-cutting, system-level non-use-case FRs it depends on (`FR-<AREA>-<n>` format: `FR-SAVE-*` autosave, `FR-LOCK-*` locking, `FR-COL-*` collaboration, `FR-VAL-*` validation, `FR-AI-*` AI, `FR-TPL-*` templates, `FR-GLO-*` glossary, `FR-HIS-*` history & authorship metadata, `FR-SEC-*` security, `FR-EXP-*` export, `FR-IMP-*` import, `FR-PERF-*` performance, `FR-NOT-*` notifications) — the traceability matrix lists these per use case.
3. Resolve **vocabulary**: look up the domain terms the use case uses in `docs/ram/requirements/project-glossary.md`. Use the defined terms in the design's identifiers and prose; do not introduce synonyms.
4. Check **`docs/ram/traceability.md`** for an existing row, and **`docs/ram/design/`** for an existing area doc: is any of this already designed or built? Extend the existing design; don't duplicate it. A design doc is **one per UC area** — if `<area>.md` already exists, you are *revising it in place* for this use case, not starting a new doc.
5. Identify **cross-cutting behavior already specified** and plan to reuse it rather than reinvent: locking (`FR-LOCK-*`, UC-DOC-2/UC-DOC-6), real-time collaboration (`FR-COL-*`, UC-COL-1), validation (`FR-VAL-*`, UC-VAL-1), notifications (Gmail SMTP), and the existing course/section/team/auth model.
6. Read the **architectural design and the constraints it carries** — the Level-1 design your Level-2 doc sits under, which now spans two docs. Read the **platform architecture-of-record** `docs/pulse-core/design/architectural-design.md` (its Container Diagram and Architectural Conventions, which you *cite, not redraw*) and the **RAM module** architectural design `docs/ram/design/architectural-design.md` (the RAM component view and the cross-cutting subsystems your area plugs into); plus the Design & Implementation Constraints, Operating Environment, and Assumptions & Dependencies in the SRS, and the SRS's Business Domain Model (your entities/tables must align to it), the SRS's Quality Attributes (performance/security/availability are non-functional acceptance criteria even when no UC step names them), and the SRS's Software Interfaces / API Document for AI/CFG/EXP areas (the LLM service). Treat the constraints and quality attributes as binding — but skeptically (a constraint can be stale or wrong; if so, flag it, don't just obey it). Fit the *unified* Project Pulse architecture — don't invent a parallel one.
7. Locate the relevant **existing code** for the area touched (frontend views/store/api, backend controller/service/repository/entity, DB migrations). The design must extend what exists, not shadow it.

## Phase 2 — Work out the design (propose, then wait for approval)

Produce the design and present it for approval before writing the doc. This is the design review — get the *shape* right here, while it's cheap to change. Include:

- **What this realizes** — the `UC-<AREA>-<n>` and the cross-cutting `FR-<AREA>-<n>` IDs it builds on. **Flag any use-case step that relies on cross-cutting behavior with no backing non-use-case FR** (a spec gap to confirm or fill) and any cited FR that doesn't exist.
- **Design diagram(s) — this is the core deliverable.** Include the Mermaid diagrams the design doc will carry, **in the proposal itself**, so the design is reviewed *now*, not reverse-engineered from code later. At minimum a **sequence diagram of the main success scenario**; add a **class** diagram for new structure, an **ER** diagram for the data-model delta, and a **state** diagram for any lock / review-submission / accept-reject lifecycle. Tie each interaction to the UC step it implements. See `docs/ram/design/README.md` ("Which diagram for what") for which view fits which concern.
- **Components & classes** — the backend controller/service/repository/entity and frontend view/store/api the area needs, **marked reused vs. new**, each a pointer to the actual file (don't transcribe its contents).
- **Key decisions** — only the non-obvious: invariants, the auth/permission rule (who may do this — Student on own team, Instructor on assigned course section, etc.), a chosen trade-off, a reuse choice.
- **Data-model delta** — the new tables/columns/migrations this area adds, as an ER diagram where it helps. **Don't redraw the shared graph** — cite the SRS's Business Domain Model and design only the delta.
- **Reuse vs. new** — which existing subsystems this leans on (locking, collaboration, validation, auth, email) vs. what is genuinely new.
- **Open questions / risks.**

**Keep the design lean** (see `docs/ram/design/README.md`): a design doc holds what code can't — the diagram(s) and the decisions that aren't self-evident — and *links* to files and the SRS's Business Domain Model rather than transcribing endpoints, schemas, or the shared domain model. Thoroughness lives in `requirements/`, not here. The doc sits **below the SRS** and is structured **by concern**, never as a per-UC changelog.

**Present the design and wait for explicit approval.** Default: present it in plan mode, rendered in the terminal, and iterate via replies. If the harness is not already in plan mode, enter it before presenting.

**Optional scratch-file editing.** Some developers find editing a file faster than terminal back-and-forth. So *offer* — or honor a request — to write the proposed design to a temporary scratch `.md` and let them edit it directly:

- Write it **outside the working tree** — the OS temp dir (e.g. `$env:TEMP` on Windows), **never inside the repo**, so an interrupted run can't leave a stray file behind.
- Print its **absolute path** (the harness renders it as a clickable link) and **pause** while they edit it in place.
- When they say to continue, **read the edited file**, treat their edits as the revised design, and then **delete the scratch file** — it is not a persisted artifact.

Either path ends at the **same approval gate**. Apart from that one optional scratch file — created *and* deleted within this phase — **edit nothing in Phase 2**: not code, not docs, not the design doc. The Phase 1 reads plus that single scratch file are the only filesystem access allowed before approval.

## Phase 3 — Write the design doc (after approval)

Write or update the area's Level-2 design doc under `docs/ram/design/<area>.md` (the lowercase area code — `doc.md`, `art.md`, …), following the skeleton and rules in [`../../docs/ram/design/README.md`](../../docs/ram/design/README.md):

- **One doc per UC area.** If the file exists, revise it **in place** — append this use case's `UC-`/`FR-` IDs to the *Realizes / Depends* header, append a sequence diagram for each new flow, and edit the area-wide Overview / class diagram / ER diagram rather than stacking a parallel `## UC-…` section. There is one class diagram and one ER diagram per area.
- **Cite, don't restate.** Link to the UC/FRs in `requirements/`, the SRS's Business Domain Model, and the platform Container Diagram in `docs/pulse-core/design/architectural-design.md`; never copy their text or redraw the shared graph.
- **Lean.** Diagrams + non-obvious decisions + file pointers. Drop any section that would only paraphrase code or requirements.
- **Level 1 stays separate.** If the design surfaced a change to the *host architecture or a cross-area model* (not just this area), that belongs in a Level-1 doc, not here — a platform-wide change (containers, conventions, deployment) in `docs/pulse-core/design/architectural-design.md`, a RAM-module-wide change (the component view, a cross-cutting subsystem) in `docs/ram/design/architectural-design.md`. **Flag it and confirm** before touching either Level-1 doc; neither is produced by a single `/design` run.

## Phase 4 — Record the design

Update the use case's row in `docs/ram/traceability.md`: set **Status** to `📐 Designed` (the design-of-record exists at `design/<area>.md`; not yet coded), and make sure the `FR IDs` column lists the cross-cutting FRs the design depends on. Leave the Frontend / Backend / Tests columns as `—` — `/implement` fills those when it builds.

If designing revealed the use case or an FR is wrong, incomplete, or contradictory, say so and propose the doc edit (follow `docs/ram/CLAUDE.md`; run `/build` after) before handing off. The spec and the design are meant to agree.

**Hand off.** End by telling the developer the design-of-record is written and they can run **`/implement <UC-ID>`** to implement it.
