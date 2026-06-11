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

A design doc is **structured by concern, not by use case.** An area accumulates several use cases over time (`UC-DOC-2`, then `-5`, then `-6`), each landed by its own `/feature` run — but the doc must not become a per-UC changelog (`## UC-DOC-2 design`, `## UC-DOC-5 design`, … stacked up). Keep the skeleton below and let the two axes grow differently:

- **Area-wide, revised in place** — Overview, Components & classes (one class diagram), Data model (one ER diagram). A new use case edits these (a new service method, a new column) rather than appending a parallel copy. There is **one** class diagram and **one** ER diagram per area, not one per use case.
- **Per-flow, appended** — Sequence diagrams (one per main success scenario + each non-trivial extension) and API-contract rows. These accumulate as use cases are added.
- **Realizes / Depends header** — append the new `UC-`/`FR-` ID each time the area gains a use case.

**Cite the SRS for the shared model; design only the delta.** Areas sit on a shared substrate — the requirement artifact graph underlies ART/LNK/VAL/REV, documents+sections+locking underlie DOC/TPL/COL. That cross-area model lives **above** these docs, in SRS §7.1 (Business Domain Model) and §4.2 (Container Diagram). **Do not redraw the shared entity graph here** — link to SRS §7.1 and design only the implementation-level delta this area adds (JPA mapping, columns, migration names, the bits below SRS granularity). Two area docs each re-drawing the artifact ER means three copies (SRS + both) to keep in sync.

**Keep it lean — design is close to the code, so don't duplicate the code.** Thoroughness belongs in `requirements/`; a design doc earns its place only by holding what code *can't* show: the **diagram** (the shape of a flow, the lifecycle of a state, how classes relate) and the **non-obvious decisions** (an invariant, an auth rule, a reuse choice, a *why*). Everything a reader could recover by opening the files — full request/response bodies, every column, every getter — does **not** go here; `traceability.md` already maps the UC to its actual frontend/backend/test files, so **link to them, don't transcribe them.** If a section would just paraphrase the code or the SRS, drop it. A good area doc is mostly diagrams plus a few lines of rationale, not prose.

Use this skeleton as a *menu*, not a checklist. Drop any section that would only restate code or requirements; keep the order of what remains so the docs are scannable.

```markdown
# <Area> Design

> Realizes: UC-<AREA>-1, UC-<AREA>-2, …
> Depends on FRs: FR-<AREA>-n, FR-LOCK-*, … (the cross-cutting subsystems it builds on)
> See: ../requirements/use-cases.md, ../requirements/software-requirements-specification.md

## Overview
One paragraph: what this area does and how it fits the Project Pulse architecture.

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
helps. Don't redraw the shared graph; link to SRS §7.1 (Business Domain Model) instead.

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
