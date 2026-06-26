# Spec-Driven, Agent-Assisted Development — A Methodology

> **Status:** working methodology document. It captures the development process Project Pulse (and its RAM module in particular) is built with, written so it can serve three audiences at once: practitioners applying it, students learning it (senior design, Fall 2026), and a research write-up (target venues: ICSE, RE, ASE, or CSEET's educational track). Names and framing here are deliberately open — see [Open questions](#open-questions-and-where-this-is-still-provisional).
>
> **Working name:** *Spec-Driven, Agent-Assisted Development* (SDAAD). A shorter handle for the central loop is **breadth-first, slice-proven, fan-out** (below). The name is a placeholder; the method is the point.

## The one-sentence version

> **Breadth-complete architecture + requirements → prove with one vertical slice → fan out per use case, looping fixes back into both the requirements and the architecture.**

That is "big design up front" with its two classic failure modes filed off: the architecture is complete in **breadth** but shallow in **depth** (so you don't over-commit), and it is **validated by a working slice** before you build on it (so you don't build on a guess). An AI coding agent does the per-use-case design and implementation against a human-authored, version-controlled contract.

## Motivation

LLM coding agents are fast, but unguided they **drift**: ad-hoc prompting produces code with no durable contract, no shared vocabulary, and no record of *why*. "Vibe coding" scales poorly past a toy — there is nothing to review the code *against*, nothing to keep two features consistent, and nothing to hand to the next contributor (human or agent).

The two traditional answers each fail differently for agent-driven work:

- **Big Design Up Front (waterfall-ish).** Specifying everything to full depth before building is wasteful and brittle — most of the detail is invalidated on contact with code, and the agent will happily implement a design that reality already contradicts.
- **Pure agile / emergent design.** Under-specifies. An agent has no stable contract to build toward, so each prompt re-derives intent and the result diverges run to run.

The methodology here is the middle path tuned specifically for a **human + coding-agent** team: give the agent a **stable, reviewable contract** (the spec) and a **complete-but-shallow map** (the architecture), validate the map early with a real slice, then let the agent realize one use case at a time — with an explicit duty to **challenge the contract** when it's wrong, and a traceability ledger that keeps spec and code honest with each other.

## Core principles

1. **The spec is the source of truth — and a durable artifact.** Requirements live as version-controlled Markdown in the repo (glossary, vision & scope, use cases, business rules, SRS), not as throwaway prompts. The code implements the spec; when they disagree, one of them is a defect.
2. **Breadth-complete, depth-shallow architecture.** The architecture-of-record names *every* component and cross-cutting subsystem (so the map is whole) but stops at responsibilities and relationships (so nothing is over-committed). Internals are designed per area, against real code, just-in-time.
3. **The use case is the unit of work, citation, and test.** Each use case is a high-level functional requirement; its steps + associated information are its acceptance criteria; traceability and tests are tagged to it. Use cases are kept small enough that "UC-X passes" is a meaningful statement.
4. **The spec is authoritative but not infallible — the challenge loop.** The agent is not a stenographer. When a step is ambiguous, an assumption breaks against the code, requirements contradict, or the spec suggests something that isn't best practice, the agent **surfaces it** — asks or pushes back — rather than silently complying or silently inventing. Fixes go back into the spec, then the design is re-derived.
5. **Bidirectional traceability and co-evolution.** Traceability is a two-direction graph, not a one-way chain (see [The traceability model](#the-traceability-model)): a matrix maps requirements → design → code → tests on a functional and a non-functional axis, checked *forward* (is every objective built?) and *backward* (does every artifact justify itself?). It is the connective tissue: changes loop *back* into the requirements and architecture, not just forward into code.
6. **Stable identifiers decoupled from position.** Requirements carry position-independent IDs (`UC-<AREA>-<slug>`, `FR-<AREA>-<slug>`, `BR-<slug>`) so the spec can be reorganized without breaking citations — essential when many documents and the code all reference the same handles.
7. **Human-owned levels, agent-built levels.** Humans own requirements quality and the high-level architectural decisions; the agent owns turning an approved use case into a design-of-record and then into code + tests, behind explicit approval gates.

## The artifacts — a spec→design→trace chain

The methodology is realized as a layered set of documents. The shape (realized in this repo under `docs/`, doctype-first — one spec set for the whole product):

- **Requirements (`requirements/`)** — the *what*, authored by humans:
  - `project-glossary.md` — canonical domain vocabulary. Fixes the words used everywhere else (and in code identifiers and UI text). No synonyms.
  - `vision-and-scope.md` — business objectives, risks, assumptions, major features.
  - `use-cases.md` — behavioral specs as use cases, grouped by area, each a high-level FR.
  - `business-rules.md` — cross-cutting policies/constraints (`BR-*`), cited by use cases and the SRS.
  - `software-requirements-specification.md` — non-use-case functional requirements (`FR-*`), domain model, quality attributes, constraints, operating environment.
- **Design (`design/`)** — the *how*, generated from the spec, in **two levels**:
  - **Level 1 — architecture-of-record.** The breadth-complete, depth-shallow map: a single arc42/C4 architecture-of-record for the whole product (`docs/design/architectural-design.md`) — the platform context/container views and conventions every module inherits, plus each module's component view. Component boundaries for not-yet-designed areas are explicitly **provisional**.
  - **Level 2 — design-of-record, one per use-case area.** Component/class design, sequence diagrams, API contracts, schema deltas. Cites the use cases/FRs it realizes; never restates them. Lean: diagrams + non-obvious decisions + pointers to real files.
- **Traceability (`traceability.md`)** — the spec→code map on two axes (see [The traceability model](#the-traceability-model)): a *functional* matrix (one row per use case, carrying FR IDs, design doc, frontend/backend modules, tests, status) and a *non-functional* matrix (one row per quality attribute → quality scenario → verifying test).

A companion **product/guides** split keeps shipped default content and build-guidance distinct from the spec itself, and an **OPEN-ISSUES** backlog (`OI-n`) tracks gaps still needed to make the spec implementation-ready.

### Documentation standards

The artifacts above follow **recognized industry templates** rather than bespoke structures — a deliberate choice, since the method targets teaching and publication: a known template is what students should learn, and it lowers the cost of peer review (reviewers recognize the structure instead of decoding a custom one).

- **Requirements** follow **Wiegers & Beatty** (*Software Requirements*, 3rd ed.) — the SRS, use-case, vision-and-scope, and glossary shapes.
- The **architecture-of-record** follows **arc42** (Starke & Hruschka), using **C4** (Brown) for the context and building-block views.

Adopt the canonical sections and ordering; *drop or merge* sections that genuinely don't apply (arc42 explicitly allows this) rather than padding with filler. Fidelity to the standard beats local optimization here.

### Features and use case areas — different views, not different fragments

Two listings sit close to each other in the requirements docs and look like they should be the same list — and aren't. Conflating them flattens both, so it's worth naming the distinction once.

- In `vision-and-scope.md`, the **Major Features** are stakeholder-visible *capabilities* — "what does the product do for the user?" Typically 8–15 per product, named and written in value terms, no behavioral detail.
- In `use-cases.md`, **use case areas** (`UC-<AREA>-*`) are *behavioral groupings* — "which actor-task interactions belong together?" Usually aligned with a bounded context in the code, so they double as the spine of the design and the traceability matrix.

The relationship between them is **many-to-many**, not one-to-one. A single feature is usually realized by use cases across several areas (Administration & Course Management spans course-section, team, student, instructor, account); a single area can serve more than one feature (the glossary area feeds *Glossary & Terminology Consistency*, but its terms also show up under *Smart Editing and Validation* and *Full Requirements Traceability*). Forcing a 1:1 list either flattens features into a CRUD enumeration or inflates UC areas into marketing buckets — losing what each view is for.

Two health checks keep the two lists honest:

- **Coverage.** Every UC area should be reachable from at least one feature; otherwise there are use cases without a stakeholder-visible reason, and the spec has work the vision doesn't justify. (When we ran this check on Project Pulse, the glossary and document-authoring areas had no feature pointing at them — both real gaps, fixed by naming features for them.)
- **Right altitude.** A feature should describe a capability, not enumerate use cases; a UC area should reflect the domain, not the marketing pitch. If a feature reads like "create / edit / delete X", it's a UC list mis-cast as a feature. If an area name reads like a tagline, it's a feature mis-cast as an area.

A deliberate consequence: **the Major Features keep no inline UC IDs.** The feature → use-case-area map lives in `traceability.md` and is verified there. That separation keeps the features at value-altitude and frees the use-case catalog to organize by domain rather than by the marketing brochure.

## The traceability model

Principle 5 calls traceability "bidirectional" and points at "a single matrix." This section makes the model *behind* that matrix explicit, because the intuitive picture most people carry — a linear chain `business objective → feature → use case → design → code` — is incomplete in three ways that matter for keeping a fallible spec and generated code honest with each other. The chain is the right **spine**; it just isn't the whole shape.

**1. It is a graph, not a chain.** Every edge is many-to-many: one objective spawns several features, one feature decomposes into use cases across several areas (the many-to-many feature ↔ area relation above is one slice of this), one use case touches several code modules, and one cross-cutting module serves many use cases. So an edge reads "realized by **one or more**," and the structure is a directed acyclic graph, not a line.

**2. It runs in two directions, and both are load-bearing.** *Forward* (objective → code) answers **coverage**: is every objective actually built? *Backward* (code → objective) answers **justification**: why does this artifact exist? A requirement nothing traces up to is gold-plating; a feature nothing traces down from is unimplemented promised scope. Bidirectionality is not decoration — ISO/IEC/IEEE 29148 requires it — and `/spec-build` enforces both: its forward check flags orphan scope, its backward check flags unjustified requirements.

**3. It has a vertical spine and orthogonal layers.** Not every requirement type sits on the objective→code line. Business rules and quality attributes cross-cut it, and glossary terms underpin all of it:

```mermaid
flowchart TD
    RIAS["Risks / assumptions (RI-*, AS-*)"] -. motivate .-> BO["Business objective (BO-*)"]
    BO -- realized by --> FEAT["Feature"]
    FEAT --> UC["Use case = high-level FR (UC-*)"]
    FEAT --> FR["Non-use-case FR (FR-*)"]
    UC -- allocated to --> DSGN["Design-of-record"]
    FR -- allocated to --> DSGN
    DSGN --> CODE["Code"]
    CODE --> TEST["Tests"]
    TEST -. verifies .-> UC

    BR["Business rules (BR-*)"] -. constrains .-> UC
    BR -. constrains .-> FR

    QA["Quality attributes (PER-*, SEC-*, ROB-*, ...)"] -- operationalized by --> QS["Quality scenario (QS-n)"]
    QS -- allocated to --> DSGN
    QS -. verified by .-> TEST

    GLOSS["Glossary terms"] -. fix vocabulary of .-> UC
```

The nodes, grounded in this repo's identifier spaces:

- **Risks / assumptions** (`RI-*`, `AS-*`) — *motivate* objectives; they are not "realized by" code, so they sit off the spine.
- **Business objective** (`BO-<AREA>-*`) — the *why*.
- **Feature** — the stakeholder-visible *capability*.
- **Use case = high-level FR** (`UC-<AREA>-*`) — observable behavior. Collapsing the textbook user-requirement / system-requirement split into one node (a use case *is* its detailed functional requirement — its steps + associated information) deliberately **removes a whole traceability hop and its drift**; this is the single biggest simplification the method buys.
- **Non-use-case FR** (`FR-<AREA>-*`) — the cross-cutting subsystems (autosave, validation, AI orchestration, notifications, security) that no single use case owns. They sit *beside* the use-case layer, not below it.
- **Design-of-record → code → tests** — the realization tail. Requirements are *allocated to* design, design is built as code, code is *verified by* tests. The verification edge is what closes the loop: a requirement with no test verifying it is not actually traced.
- **Business rules** (`BR-*`) — an orthogonal **constraint** layer cited by use cases and FRs; the question "what enforces a given `BR-*`?" is a different relation than "what realizes a given `BO-*`?".
- **Quality attributes** (`USE-*`, `PER-*`, `SEC-*`, `AVL-*`, `ROB-*`, …) — the **NFR spine runs in parallel**, never through a use case: a quality attribute is *operationalized by* a quality scenario (`QS-n`) and *verified by* a test. This is the spine most projects let float; naming it as its own axis is what keeps it honest.
- **Glossary terms** — the **consistency axis** underneath everything: the same word in objective, use case, code identifier, and UI label.

Three relation kinds, then, not one: requirements **derive from** higher needs (the spine), are **allocated to** design, and are **verified by** tests — plus the orthogonal **constrains** (rules) and **operationalizes** (attribute → scenario) edges. Keeping them distinct is what lets the matrix answer "is X built?", "why does X exist?", and "is X verified?" as separate questions.

This model is instantiated, not just described: `traceability.md` carries it on **two axes** — a functional matrix (use case → FR/design/code/tests), with a companion register giving each non-use-case `FR-*` its own realization row, and a non-functional matrix (quality attribute → `QS-n` → test) — the `QS-n` scenario definitions live in the architecture-of-record, the `BR-*` constraints in the business rules, and `/spec-build` mechanically checks that every edge resolves and every node is covered both ways. The model is the picture; those documents and checks are its enforcement.

## The lifecycle

### Phase A — Author requirements and a breadth-complete architecture (human-led, often parallel)

Draft good-quality requirements first; develop the architecture-of-record alongside them — much of "architecture" (host constraints, operating environment, external integrations) is itself requirements-level, so the two co-evolve. The exit criterion is **breadth, not depth**: every use-case area, component, cross-cutting subsystem, convention, and external integration is named and placed; internals are deliberately deferred.

### Phase B — Prove the architecture with one vertical slice

Before fanning out, pick one **representative** use case and take it all the way through: design-of-record → implementation → tests. This validates the architecture while corrections are still cheap — boundaries that looked clean on the diagram meet real code, and the architecture-of-record is corrected from what was learned. **Architecture proven by a working slice beats architecture proven by inspection.**

### Phase C — Fan out per use case

With the architecture validated, realize the remaining use cases one at a time:

1. **`/design <UC>`** — turn the use case into an approved Level-2 design-of-record (diagrams + decisions). A separately-reviewed stage that **stops before code**. If the area contradicts the provisional Level-1 map, the design revises Level 1 (module architecture) and records it; platform-level changes are confirmed separately.
2. **`/implement <UC>`** — build from that approved design: plan → code → tests, extending the existing packages rather than forking the architecture.
3. **Record** — update traceability; loop any spec/architecture fixes the work surfaced back into the docs.

### Phase D — Continuous co-evolution

During design or implementation, the team can always return to adjust **both** the requirements and the architecture. The challenge loop (Principle 4) plus traceability (Principle 5) make this the normal case, not an exception: the spec, the architecture, and the code are kept in agreement as a standing invariant.

## The human–agent division of labor

| Concern | Owner |
|---|---|
| Requirements quality, scope, vocabulary | Human |
| High-level architectural decisions (platform-wide) | Human (agent proposes, human confirms) |
| Turning an approved use case into a design-of-record | Agent, behind a design-review approval gate |
| Implementing an approved design into code + tests | Agent |
| Module-level architecture revision when an area contradicts the map | Agent, surfaced in the design review and recorded |
| Cross-document consistency checks | Agent (tooling) |
| Judgment calls, ambiguity resolution, "is this a good idea?" | Human — prompted by the agent's challenge loop |

The **approval gate** between design and code is load-bearing: design is reviewed as a deliberate stage (like requirements), so the *shape* is corrected while it is cheap, not reverse-engineered from code later.

## The tooling instance (this repository)

The methodology is tool-agnostic, but Project Pulse instantiates it with **[Claude Code](https://claude.com/claude-code)** and a small set of repository-local slash commands plus a machine-and-human conventions file:

- **`CLAUDE.md`** (root and per-subtree) — the conventions, the binding architecture-of-record pointer, and the authoring rules, read by both humans and the agent on every session. This is how the contract stays enforced rather than aspirational.
- **`/design`** — Phase C step 1 (use case → design-of-record; may revise the module architecture).
- **`/implement`** — Phase C step 2 (design → code + tests).
- **`/spec-build`** — mechanically verifies and resyncs cross-document consistency (anchors, ID resolution, UC↔traceability coupling, terminology).

The running case study is the **RAM (Requirements Authoring & Management) module** — itself a tool for *authoring* requirements — developed spec-first inside the larger Project Pulse platform. (A pleasing reflexivity for a paper: a spec-driven methodology, applied to build a requirements-authoring tool, documented by its own specs.)

## Educational deployment (senior design, Fall 2026)

The intended classroom use: student teams **author the spec** (glossary, vision & scope, use cases, business rules, SRS) for their capstone project, then use an AI coding agent under this methodology to produce good-quality software quickly — *without* the agent running away from them.

Learning outcomes it targets:

- **Requirements-engineering discipline** — students must write atomic, testable, vocabulary-consistent requirements, because the agent builds *exactly* what the spec says (a fast, unforgiving feedback loop on spec quality).
- **Architectural thinking** — breadth-complete-but-shallow forces students to decompose a system without drowning in premature detail.
- **Reviewing AI output** — the design-review gate and traceability make "read and judge what the agent produced" a first-class, gradeable activity.
- **Engineering judgment** — the challenge loop teaches students to treat the agent as a skeptical collaborator, not an oracle.

Guardrails to design into the assignment: insist on the approval gates (no `/implement` without a reviewed design), require the traceability matrix to be kept current, and grade the *spec* and the *review*, not only the running code.

## Research framing (toward ICSE / RE / ASE / CSEET)

> This section is a **starting scaffold**, not claims of results. It is meant to help shape a submission; fill in the empirical parts before asserting them.

**Candidate contributions to argue:**

1. A concrete, tool-supported methodology for **human-in-the-loop, spec-driven development with LLM coding agents**, with the use case as the unit of design, implementation, traceability, and test.
2. The **breadth-complete / depth-shallow + thin-slice-validation** discipline as a specific answer to "how much design up front" for agentic development.
3. The **challenge loop** — an explicit obligation on the agent to contest the spec — as a mechanism for keeping a fallible human spec and generated code in co-evolving agreement, mediated by traceability.
4. (CSEET angle) An **educational instantiation** and its effect on student requirements-engineering and AI-review competencies.

**Candidate research questions:**

- RQ1. Does anchoring an LLM coding agent to a version-controlled spec (vs. ad-hoc prompting) measurably reduce drift / inconsistency and improve traceability coverage?
- RQ2. Does breadth-complete/depth-shallow architecture + thin-slice validation reduce rework compared to either full up-front design or emergent design, in agent-driven projects?
- RQ3. Does the challenge loop catch spec defects earlier, and what classes of defect does it surface?
- RQ4. (Education) How does the methodology affect students' requirements quality, architectural decomposition, and ability to critically review AI-generated code?

**Evaluation strategies to consider:** a longitudinal **case study** on Project Pulse/RAM (defect-escape, rework, traceability completeness, spec/code divergence over time); a **controlled or quasi-experimental student study** in the Fall 2026 cohort (methodology vs. control, with rubric-scored specs and reviews); **artifact-quality metrics** (atomicity/testability of requirements, traceability matrix completeness, consistency-check pass rates from `/spec-build`).

**Positioning vs. prior art (to be written honestly):** relate to model-driven development, behavior-driven development and executable specifications, requirements traceability research, and the recent wave of agentic / "spec-driven" AI development tooling — and state precisely what is novel here (the specific *discipline* and the human-agent *division of labor with a challenge loop*, not merely "use a spec").

**Threats to validity to pre-empt:** single-project / single-instructor case study (external validity); author-as-evaluator bias; rapidly changing agent capabilities (construct/temporal validity); Hawthorne effects in the student study.

## Open questions and where this is still provisional

- **Naming.** "Spec-Driven, Agent-Assisted Development" is a working title; the loop ("breadth-first, slice-proven, fan-out") may be the more memorable handle. A sharper name will help the paper.
- **Slice selection.** What makes a use case a *good* Phase-B proving slice (most cross-cutting? highest risk? most representative?) deserves a crisp, teachable rule.
- **Where Level-1 revision is allowed.** Currently the module architecture is agent-revisable (with review); the platform architecture is confirm-first. Whether that boundary is the right one is itself worth studying.
- **Metrics.** The evaluation metrics above need operational definitions before they can be reported.

---

*This document is itself maintained spec-first: it describes the process the repository is built with. When the process changes, change it here.*
