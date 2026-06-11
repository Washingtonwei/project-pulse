# Requirements Authoring & Management (RAM) — Spec

This directory holds the **specification for the Requirements Authoring & Management (RAM) module** of Project Pulse — a graph-first, model-driven requirements IDE for software engineering and senior design courses at TCU.

RAM is a **module inside the Project Pulse codebase**, not a separate project. This `docs/ram/` tree is its spec→design→trace chain; the code that implements it lives in `backend/` and `frontend/`. For how the spec drives the code (the `/feature` workflow), see the **Spec-driven RAM development** section of the repo-root [`CLAUDE.md`](../../CLAUDE.md).

## Layout

```
docs/ram/
├── requirements/                              # the spec (what to build)
│   ├── project-glossary.md                    # canonical vocabulary
│   ├── vision-and-scope.md                    # business objectives, risks, features
│   ├── use-cases.md                           # behavioral specs (UC-<AREA>-<n>)
│   ├── business-rules.md                      # cross-cutting policies (BR-*)
│   ├── software-requirements-specification.md # architecture, FRs, quality attrs
│   └── OPEN-ISSUES.md                          # working backlog (OI-n, P0–P3)
├── design/                                    # design docs, one per UC area (below the SRS)
│   └── README.md                              # design-doc conventions
├── traceability.md                            # spec→code matrix: one row per use case
├── guides/                                    # supporting build guidance (not spec docs)
│   └── ai-implementation-notes.md
├── product/                                   # RAM product material: shipped default content
│   └── cross-document-review-criteria.md      #   default review criteria + critique system prompt
├── CLAUDE.md                                  # authoring rules for everything under docs/ram/
└── README.md                                  # this file
```

The five files under `requirements/` are intentionally ordered and describe the same underlying model from different angles — a change in one usually requires updates in others (especially glossary terms). Read and edit them in that order.

## Editing these docs

**The authoring conventions are not repeated here** — they live in [`CLAUDE.md`](CLAUDE.md) (heading numbering, anchor slugs, TOC format, `UC-`/`FR-`/`BR-` ID schemes, glossary term formatting, cross-document coherence). Read that before editing.

The workflow:

```
edit a doc  →  run /build  →  review the report  →  fix flagged issues  →  commit
```

`/build` (defined in [`../../.claude/commands/build.md`](../../.claude/commands/build.md)) treats the docs as artifacts that must "compile": it auto-fixes mechanical issues (heading numbering, stray `{#slug}` anchors, TOC regeneration, straight quotes) and reports semantic issues for your review (undefined glossary terms, dangling FR/BR references, ReqLint writing issues, cross-doc terminology drift). It is run from [Claude Code](https://claude.com/claude-code).

To implement a use case end to end, run `/feature <UC-ID>` ([`../../.claude/commands/feature.md`](../../.claude/commands/feature.md)) — it drives the use case through plan → design → code → test and records the result back into `traceability.md`.
