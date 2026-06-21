# Project Pulse — Documentation

This directory holds the **spec→design→trace chain** for Project Pulse — the durable, version-controlled documentation that drives the implementation. It is organized **doctype-first**: one set of docs for the whole product, covering both the Project Pulse core (weekly activity reports, peer evaluations, courses/sections/teams) and the RAM (Requirements Authoring & Management) module.

## Layout

```
docs/
├── requirements/   the spec (what the system does)
│   ├── project-glossary.md                       canonical domain vocabulary
│   ├── vision-and-scope.md                        business objectives, risks, assumptions, features
│   ├── use-cases.md                               behavioral specs, grouped by area (UC-<AREA>-<n>)
│   ├── business-rules.md                          cross-cutting policies & access rules (BR-*)
│   ├── software-requirements-specification.md     non-use-case FRs, domain model, quality attributes
│   └── OPEN-ISSUES.md                             implementation-readiness backlog (OI-n)
├── design/         the design (how the code realizes the spec)
│   ├── architectural-design.md                    the one arc42/C4 architecture-of-record
│   ├── README.md                                  design-doc conventions (two-level model)
│   └── <area>.md                                  per-UC-area detailed design (added as areas are designed)
├── traceability.md                                the spec→code map: one row per use case
├── guides/                                        supporting build guidance (not spec docs)
├── product/                                       shipped default content the product seeds at runtime
├── methodology.md                                 the spec-driven, agent-assisted method this repo follows
└── CLAUDE.md                                       authoring rules for everything under docs/
```

## Where to start

- **Reading the spec:** glossary → vision & scope → use cases → business rules → SRS (the order above).
- **Understanding the architecture:** [`design/architectural-design.md`](design/architectural-design.md) (arc42 + C4).
- **The method behind it all:** [`methodology.md`](methodology.md).
- **Authoring or editing these docs:** the conventions (anchors, ID schemes, terminology, cross-doc coherence) live in [`CLAUDE.md`](CLAUDE.md). Run `/spec-build` to verify and resync after structural edits.

The repo-root [`CLAUDE.md`](../CLAUDE.md) governs the codebase as a whole and explains how this spec drives the code (its **Spec-driven development** section).
