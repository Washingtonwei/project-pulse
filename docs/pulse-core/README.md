# Pulse Core — Spec (placeholder)

This directory is reserved for the **specification of the core Project Pulse functionality** — the non-RAM features: weekly activity reports, peer evaluations, and instructor dashboards. **It is a placeholder; the specs have not been authored yet.**

When this set is written, it will follow the **same shape as [`../ram/`](../ram/)** — area-first, spec→design→trace:

```
docs/pulse-core/
├── requirements/        # project-glossary, vision-and-scope, use-cases,
│                        #   business-rules, software-requirements-specification
├── design/              # design docs, one per UC area (below the SRS)
├── traceability.md      # spec→code matrix: one row per use case
├── guides/              # supporting authoring/build guidance
├── CLAUDE.md            # authoring rules for everything under docs/pulse-core/
└── README.md            # this file
```

Until then:

- The **authoritative requirements work lives under [`../ram/`](../ram/)** (the RAM module). Use it as the template for structure, ID schemes, and authoring conventions.
- This module will carry its **own nested `CLAUDE.md`** governing edits under `docs/pulse-core/`, mirroring [`../ram/CLAUDE.md`](../ram/CLAUDE.md). Do not assume `docs/ram/CLAUDE.md` governs files here.
- For how a spec drives implementation (the `/feature` workflow) and the overall docs philosophy, see the repo-root [`CLAUDE.md`](../../CLAUDE.md).
