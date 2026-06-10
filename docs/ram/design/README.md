# RAM Design Docs

This directory holds the **design docs for the RAM module** — one per use-case area. They sit **below the SRS** in the spec→design→trace chain: where `requirements/` says *what* the system does, a design doc says *how* the code realizes it (component/class design, sequence, API contracts, DB schema).

A design doc is the **design of record** for its area. It is written or updated by `/feature` (Phase 3, step 0; see [`../../../.claude/commands/feature.md`](../../../.claude/commands/feature.md)) when an area's use case is implemented, and it is what the next person reads to understand the existing implementation before extending it.

## The cardinal rule: cite, don't restate

A design doc **cites** the use cases and functional requirements it realizes (`UC-<AREA>-<n>`, `FR-<AREA>-<n>`) and links back to them — it never copies their text. The requirement lives in `requirements/`; duplicating it here just creates a second copy to drift. If you find yourself re-describing *what* the system shall do, stop and link to the UC/FR instead, then describe only the *how*.

The chain to keep straight:

```
requirements/use-cases.md  +  software-requirements-specification.md   (what — the contract)
        │  realized by
        ▼
docs/ram/design/<area>.md                                             (how — this directory)
        │  recorded in
        ▼
traceability.md                                                       (the spec→code map)
```

## One doc per use-case area

Each design doc covers exactly one UC area and is named after that area's lowercase code, so it maps unambiguously to the `UC-<AREA>-<n>` / `FR-<AREA>-<n>` IDs it realizes. The areas (in document order, matching `requirements/use-cases.md`):

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

Add a doc only when its area is first designed; this directory grows as `/feature` runs. If a new UC area is introduced in `requirements/use-cases.md`, add the matching row above and a same-named design doc when it's implemented.

## What a design doc contains

Use this skeleton. Drop sections that don't apply to the area, but keep the order so the docs are scannable.

```markdown
# <Area> Design

> Realizes: UC-<AREA>-1, UC-<AREA>-2, …
> Depends on FRs: FR-<AREA>-n, FR-LOCK-*, … (the cross-cutting subsystems it builds on)
> See: ../requirements/use-cases.md, ../requirements/software-requirements-specification.md

## Overview
One paragraph: what this area does and how it fits the Project Pulse architecture.

## Components & classes
Backend (controller / service / repository / entity) and frontend (view / store / api)
that realize the area. Link to the actual files. Note what is reused vs. new.

## Sequence
Mermaid sequence diagram(s) for the main success scenario and any non-trivial
extension/error flow. Reference the use-case step each interaction implements.

## API contracts
Endpoints touched: method + path, request/response shape, auth/permission rule
(who may do this — student on own team, instructor on assigned section, …).

## Data model
Entities/tables, key fields, relationships, and migrations. Mermaid class or ER
diagram where it helps. Tie back to SRS §7 (Business Domain Model).

## Reuse & cross-cutting
Which existing subsystems this leans on — locking (FR-LOCK-*), collaboration
(FR-COL-*), validation (FR-VAL-*), auth, email — rather than reinventing.

## Open questions / risks
```

## Which diagram for what

Good design before implementation is the point of these docs — pick the UML view that fits the concern, and use more than one when the area warrants it. All diagrams are Mermaid (see Conventions).

- **Sequence** — request/response flows: SPA → controller → service → repository, plus any LLM-service round-trip. One for the main success scenario; add one for each non-trivial extension/error flow. Tie every interaction to the UC step it implements.
- **Class** — component/structure design: backend controllers/services/repositories/entities and frontend views/stores/api clients, and their relationships. Mark what is reused vs. new.
- **ER** — DB schema: tables, key fields, relationships, migrations. Tie back to SRS §7.1 (Business Domain Model).
- **State** — lifecycle-heavy areas, where the subtle behavior (and the bugs) live: document-section **lock** states (DOC), **review & submission** states (REV), AI candidate accept/reject (AI). If an area has a state machine, diagram it.
- **Flowchart / C4** — how the area fits the Project Pulse containers (SRS §4.2), when the architectural placement isn't obvious.

## Conventions

- **Diagrams are Mermaid** (` ```mermaid ` fenced blocks), never base64-embedded images — diffable and renders in Typora/GitHub. (Same rule as the core docs; see [`../CLAUDE.md`](../CLAUDE.md).)
- **Vocabulary follows the glossary.** Use the defined terms (`document section`, `course section`, `requirement artifact`, `artifact link`, …) in prose, identifiers, and UI strings — never a synonym. Never write a bare "section"; qualify it as *course section* or *document section*. See [`../CLAUDE.md`](../CLAUDE.md) for the full vocabulary rules.
- **These docs are not section-numbered specs.** They don't follow the `# **N. Title**` numbering / TOC scheme of the five core docs, and **`/build` does not process them** — `/build` only handles the five `requirements/` docs and verifies `../traceability.md`. Keep design docs plainly structured with the skeleton above.

## After writing a design doc

Record the work in [`../traceability.md`](../traceability.md): the use case's row should point at this design doc alongside its frontend/backend/test artifacts (`/feature` Phase 5 does this). That row, not this README, is the live index of which areas are designed and built.
