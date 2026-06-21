# **Design Docs**

This directory holds the **design docs for Project Pulse** — how the code realizes the spec. Where `requirements/` says *what* the system does, design says *how*; design sits **below the SRS** in the spec→design→trace chain.

Design comes in **two levels**, and a coding agent reads them top-down:

1. **Architectural design** — [`architectural-design.md`](architectural-design.md). The product's architecture-of-record (arc42 + C4): the platform context/container views and the binding conventions, the shared-foundation and performance-tracking component views, plus the RAM module's component view and cross-cutting subsystems. **One doc.** Read it *first*, to orient before touching code.
2. **Detailed design** — one doc per use-case (UC) area (`doc.md`, `art.md`, …). How one area's code realizes its use cases: component/class design, sequence diagrams, the data-model delta. Read the relevant area doc *before extending that area*.

```
requirements/use-cases.md  +  software-requirements-specification.md   (what — the contract)
        │  realized by
        ▼
┌──────────────────────────────────────────────────────────┐
│  design/  (how)                                            │
│    Level 1  architectural-design.md   — product arch       │
│    Level 2  <area>.md (doc, art, …)   — per-area detail    │
└──────────────────────────────────────────────────────────┘
        │  recorded in
        ▼
traceability.md                                              (the spec→code map)
```

A design doc is the **design of record** for what it covers. The Level-2 docs are written or updated by `/design` (see [`../../.claude/commands/design.md`](../../.claude/commands/design.md)) when an area's use case is designed — before any code — and each is what `/implement` builds from and what the next person reads to understand the existing implementation before extending it.

---

# **Level 1 — Architectural design**

[`architectural-design.md`](architectural-design.md) is the product's **architecture-of-record** — the arc42/C4 design every area inherits or is bounded by, not architecture decided per feature. It holds:

- the **platform views** — the C4 system-context and container diagrams, and the binding conventions every area follows (the `Result` envelope, the DDD vertical-slice + `Converter` DTO pattern, JWT/`AuthorizationManager` auth, Flyway migrations), alongside runtime, deployment, cross-cutting concerns, decisions, and risks;
- the **shared-foundation and performance-tracking component views** — the `course`/`section`/`team`/`student`/`instructor` (foundation) and `activity`/`evaluation`/`rubric` (performance tracking) bounded contexts inside the REST API container;
- the **RAM component view** — the `ram/*` bounded-context components on the same shared base;
- the **cross-cutting subsystems** an area plugs into rather than reinvents (locking, templates, collaboration, glossary, authorship, notifications, security, autosave, validation, AI, export), each tied to its `FR-*` family and owner package. (Per-use-case build status is not tracked there — it lives in `../traceability.md`.)

The requirements-level frame around this architecture — operating environment (`OE-*`), design/implementation constraints (`CO-*`), and architecture-level assumptions/dependencies (`AS-*` / `DE-*`) — is **owned by the SRS** ([Overall Description](../requirements/software-requirements-specification.md#overall-description)). `architectural-design.md` cites those IDs and realizes them; it does not redefine them.

It is **not** named after a UC area and is **not** produced by a single `/design` run — it changes when the architecture does, not when one area gains a use case. The Level-2 docs **cite the Container and Component diagrams here rather than redrawing them**.

The **cross-area shared model** is **owned by the SRS's Business Domain Model** (the requirement-artifact graph underlying ART/LNK/VAL/REV; documents + sections + locking underlying DOC/TPL/COL). It is *referenced* from here and from the per-area docs, *above* the Level-2 docs, so a Level-2 doc never redraws it — it cites it (see the cardinal rule below).

---

# **Level 2 — Detailed design (one doc per UC area)**

Each Level-2 doc covers exactly one UC area and is named after that area's lowercase code, so it maps unambiguously to the `UC-<AREA>-<n>` / `FR-<AREA>-<n>` IDs it realizes. The areas (in document order, matching `requirements/use-cases.md`):

| File | Area | Scope |
|------|------|-------|
| `tpl.md` | TPL | templates / team-document provisioning |
| `glo.md` | GLO | glossary |
| `doc.md` | DOC | requirement documents (sections, locking) |
| `art.md` | ART | requirement artifacts |
| `lnk.md` | LNK | artifact links & tracing |
| `val.md` | VAL | validation (ReqLint) |
| `col.md` | COL | collaboration |
| `rev.md` | REV | review & submission |
| `exp.md` | EXP | export |
| `cfg.md` | CFG | AI configuration |
| `ai.md`  | AI  | AI assistants |

Add a doc only when its area is first designed; this directory grows as `/design` runs. If a new UC area is introduced in `requirements/use-cases.md`, add the matching row above and a same-named design doc when it's implemented.

## **The cardinal rule: cite, don't restate**

A design doc **cites** the use cases and functional requirements it realizes (`UC-<AREA>-<n>`, `FR-<AREA>-<n>`) and links back to them — it never copies their text. The requirement lives in `requirements/`; duplicating it here just creates a second copy to drift. If you find yourself re-describing *what* the system shall do, stop and link to the UC/FR instead, then describe only the *how*.

The same rule applies upward to Level 1: **cite the SRS for the shared model; design only the delta.** Don't redraw the shared entity graph in a Level-2 doc — link to the SRS's Business Domain Model and the Container Diagram in [`architectural-design.md`](architectural-design.md), then design only the implementation-level delta this area adds (JPA mapping, columns, migration names, the bits below SRS granularity). Two area docs each re-drawing the artifact ER means three copies (SRS + both) to keep in sync.

## **What a design doc contains**

A design doc is **structured by concern, not by use case.** An area accumulates several use cases over time (`UC-DOC-2`, then `-5`, then `-6`), each designed by its own `/design` run — but the doc must not become a per-UC changelog (`## UC-DOC-2 design`, `## UC-DOC-5 design`, … stacked up). Keep the skeleton below and let the two axes grow differently:

- **Area-wide, revised in place** — Overview, Components & classes (one class diagram), Data model (one ER diagram). A new use case edits these (a new service method, a new column) rather than appending a parallel copy. There is **one** class diagram and **one** ER diagram per area, not one per use case.
- **Per-flow, appended** — Sequence diagrams (one per main success scenario + each non-trivial extension) and API-contract rows. These accumulate as use cases are added.
- **Realizes / Depends header** — append the new `UC-`/`FR-` ID each time the area gains a use case.

**Keep it lean — design is close to the code, so don't duplicate the code.** Thoroughness belongs in `requirements/`; a design doc earns its place only by holding what code *can't* show: the **diagram** (the shape of a flow, the lifecycle of a state, how classes relate) and the **non-obvious decisions** (an invariant, an auth rule, a reuse choice, a *why*). Everything a reader could recover by opening the files — full request/response bodies, every column, every getter — does **not** go here; `traceability.md` already maps the UC to its actual frontend/backend/test files, so **link to them, don't transcribe them.** If a section would just paraphrase the code or the SRS, drop it. A good area doc is mostly diagrams plus a few lines of rationale, not prose.

Use this skeleton as a *menu*, not a checklist. Drop any section that would only restate code or requirements; keep the order of what remains so the docs are scannable.

```markdown
# <Area> Design

> Realizes: UC-<AREA>-1, UC-<AREA>-2, …
> Depends on FRs: FR-<AREA>-n, FR-LOCK-*, … (the cross-cutting subsystems it builds on)
> See: ../requirements/use-cases.md, ../requirements/software-requirements-specification.md, architectural-design.md (Container & Component diagrams + cross-cutting subsystems)

## Overview
One paragraph: what this area does and how it fits the architecture
(link to the Container Diagram in architectural-design.md rather than redrawing it).

## Components & classes
The class diagram and a one-line-per-component pointer to the actual files
(controller / service / repository / entity; view / store / api). Mark reused vs. new.
Link the files — don't transcribe their contents.

## Sequence
Mermaid sequence diagram(s) for the main success scenario and any non-trivial
extension/error flow. This is the core value — the flow a reader can't see at a glance.
Reference the use-case step each interaction implements.

## Key decisions
Only the non-obvious: invariants, the auth/permission rule (who may do this), a chosen
trade-off, a reuse choice. Skip anything self-evident from the code.

## Data model
The *delta* this area adds (new tables/columns/migrations) — as an ER diagram where it
helps. Don't redraw the shared graph; link to the SRS's Business Domain Model instead.

## Reuse & cross-cutting
Which existing subsystems this leans on — locking (FR-LOCK-*), collaboration
(FR-COL-*), validation (FR-VAL-*), auth, email — rather than reinventing.

## Open questions / risks
```

## **Which diagram for what**

Good design before implementation is the point of these docs — pick the UML view that fits the concern, and use more than one when the area warrants it. All diagrams are Mermaid (see Conventions).

- **Sequence** — request/response flows: SPA → controller → service → repository, plus any LLM-service round-trip. One for the main success scenario; add one for each non-trivial extension/error flow. Tie every interaction to the UC step it implements.
- **Class** — component/structure design: backend controllers/services/repositories/entities and frontend views/stores/api clients, and their relationships. Mark what is reused vs. new.
- **ER** — DB schema: tables, key fields, relationships, migrations. Tie back to the SRS's Business Domain Model.
- **State** — lifecycle-heavy areas, where the subtle behavior (and the bugs) live: document-section **lock** states (DOC), **review & submission** states (REV), AI candidate accept/reject (AI). If an area has a state machine, diagram it.
- **Flowchart / C4** — how the area fits the Project Pulse containers (the Container Diagram in [`architectural-design.md`](architectural-design.md)), when the architectural placement isn't obvious.

---

# **Conventions (both levels)**

- **Diagrams are Mermaid** (` ```mermaid ` fenced blocks), never base64-embedded images — diffable and renders in Typora/GitHub. (Same rule as the core docs; see [`../CLAUDE.md`](../CLAUDE.md).)
- **Vocabulary follows the glossary.** Use the defined terms (`document section`, `course section`, `requirement artifact`, `artifact link`, …) in prose, identifiers, and UI strings — never a synonym. Never write a bare "section"; qualify it as *course section* or *document section*. See [`../CLAUDE.md`](../CLAUDE.md) for the full vocabulary rules.
- **`/spec-build` does not process these docs.** It only handles the five `requirements/` docs and verifies `../traceability.md`; design docs get no mechanical anchor/heading treatment. Keep them plainly structured with the skeletons above. (Like the core docs, they carry no section numbers and no TOC — but that's just the house style here, not something `/spec-build` enforces.)

# **After writing a design doc**

Record the work in [`../traceability.md`](../traceability.md): `/design` sets the use case's row to `📐 Designed` (the design-of-record now exists at `design/<area>.md`), and `/implement` later fills the frontend/backend/test artifacts and flips the status (its Phase 5). That row, not this README, is the live index of which areas are designed and built.
