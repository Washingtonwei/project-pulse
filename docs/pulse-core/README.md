# Pulse Core — Spec

This directory holds the **specification of the core Project Pulse functionality** — the non-RAM features: weekly activity reports, peer evaluations, and instructor dashboards.

Status:
- **`design/architectural-design.md` is authored** — the platform architecture-of-record (C4 views, conventions, cross-cutting subsystems, deployment) that *both* the core and the RAM module inherit. RAM's design docs cite it.
- **The requirements specs are not yet in Markdown** — they exist in Google Docs and are pending conversion into `requirements/` (Vision and Scope, Project Glossary, Business Rules, Use Cases, SRS).

The full set follows the **same shape as [`../ram/`](../ram/)** — area-first, spec→design→trace:

```
docs/pulse-core/
├── requirements/        # project-glossary, vision-and-scope, use-cases,
│                        #   business-rules, software-requirements-specification
├── design/              # architectural-design.md (authored) + one doc per UC area (below the SRS)
├── traceability.md      # spec→code matrix: one row per use case
├── guides/              # supporting authoring/build guidance
├── CLAUDE.md            # authoring rules for everything under docs/pulse-core/
└── README.md            # this file
```

Notes:

- Until the requirements specs land, the **authoritative requirements example lives under [`../ram/`](../ram/)** (the RAM module). Use it as the template for structure, ID schemes, and authoring conventions.
- This module will carry its **own nested `CLAUDE.md`** governing edits under `docs/pulse-core/`, mirroring [`../ram/CLAUDE.md`](../ram/CLAUDE.md). Do not assume `docs/ram/CLAUDE.md` governs files here.
- For how a spec drives implementation (the `/design` → `/implement` workflow) and the overall docs philosophy, see the repo-root [`CLAUDE.md`](../../CLAUDE.md).
