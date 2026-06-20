# /sync-check — Spec ↔ Code conformance audit

Detect **drift between the requirements docs and the actual codebase** — where built code diverges from the spec, where the spec is unimplemented, and where code exists that no spec describes. This is the periodic companion to `/build`.

**`/build` vs `/sync-check` — two different jobs, don't conflate them:**
- **`/build`** = *do the docs agree with each other?* Intra-doc consistency (anchors, slugs, FR/BR refs, UC↔traceability). Mechanical, deterministic, cheap, **auto-fixes**. Run it freely after doc edits.
- **`/sync-check`** (this) = *do the docs agree with the code?* Spec↔code conformance. Mostly **semantic** (read code, judge whether a rule is enforced), expensive, somewhat non-deterministic, and **report-mostly** — every finding needs a human fix-code-vs-amend-doc decision, so it never edits code and never "auto-fixes" a divergence. Its only write is appending **draft `OI-n`** items to `docs/requirements/OPEN-ISSUES.md`.

**Run `/build` first** (or assume it passes) — `/sync-check` relies on the docs being internally consistent and the traceability matrix being current. This command **reuses**, does not duplicate, `/build`'s UC↔traceability coverage check.

---

## Arguments / scope

`/sync-check [scope]` where `scope` is one of:
- *(none)* → **changed-since-last-run** (default). Read the **last-audit baseline** marker in `OPEN-ISSUES.md` (see Phase 0), `git diff <baseline>..HEAD --stat` to find changed `backend/`/`frontend/`/`docs/` paths, and scope the **semantic** Phase B to the BRs/UCs/areas those touch. Phase A (mechanical) always runs in full — it's cheap.
- `--full` → exhaustive: every BR, every UC main-flow + extensions, every endpoint and enum. Expensive; this is the deliberate 4-pass audit. Use after large changes or on a schedule.
- `<area>` (e.g. `EVA`, `DOC`, `ram/document`, `BR-12..BR-14`) → scope to one UC-area, package, or BR range.

Default to changed-since-last-run; drift only appears where code or specs changed, so this is both cheaper and higher-signal. State the chosen scope at the start of the run.

---

## Phase 0 — Baseline & reconciliation (always)

1. **Read the OI register.** Read `docs/requirements/OPEN-ISSUES.md` — specifically the `## Doc ↔ Code Gap Analysis` section and the related P2 items (OI-19, OI-23, OI-24). Build the set of **already-known** divergences. **You must reconcile against this** — never re-report a known gap as new. Note the next free `OI-n`.
2. **Read the baseline marker.** Find the `**Last sync-check:**` line in `OPEN-ISSUES.md` (under the gap-analysis heading). If absent, treat the whole repo as in-scope for this run and create the marker in Phase C. `git diff <baseline-commit>..HEAD --stat` gives the changed-files scope.
3. **Re-confirm resolved gaps.** For each known OI in scope, re-check the cited `file:method`: if the divergence is now fixed, flag it as **likely-resolved** (recommend checking its box) — don't silently leave it.

## Phase A — Mechanical checks (deterministic, cheap, always full)

Report each as PASS / FINDING with evidence.

- **A1 — UC ↔ traceability coverage.** Reuse `/build`'s check (one row per `UC-<AREA>-<n>`, no orphan rows). Don't duplicate the logic — if `/build` was just run, cite it; otherwise run the same comparison.
- **A2 — Endpoint ↔ spec coverage (catches *built-but-unspecified*).** Enumerate every controller mapping: `grep` for `@GetMapping|@PostMapping|@PutMapping|@PatchMapping|@DeleteMapping` under `backend/src/main/java`. For each endpoint, confirm it maps to a UC (via `traceability.md`) or a documented FR. Flag endpoints with **no** spec home that aren't already a known OI (this is how OI-34 rubric/criterion would surface). Likewise scan `frontend/src/apis/**` for client calls with no backing UC.
- **A3 — Enum ↔ domain-model sync.** Compare each backend enum to the SRS Business Domain Model lists and glossary: `DocumentStatus`, `RequirementArtifactType`, `ArtifactLinkType`, `Priority`, `SectionType`, `CommentThreadStatus`, `ExtensionKind`/`ExtensionExit`. Flag any value in the enum but not the spec (or vice-versa) — e.g. a missing `ACCEPTED` status (OI-37), or enum/taxonomy drift (OI-15/OI-22). Keep `frontend/src/apis/ram/types.ts` in lockstep with the backend enums.
- **A4 — Traceability honesty.** For every row marked ✅ Implemented, verify the cited backend class and test files actually exist (Glob). Flag ✅ rows whose cited artifacts are missing, and `—` cells where code clearly exists.
- **A5 — Pinned-value sync (best-effort).** Where a doc pins a concrete value, check the code constant: e.g. BR-10 lock timeout vs `ram.lock.default-lock-ttl` (OI-29); JWT/`2h` and reset-token `5min` vs the architecture doc; password rule vs the SRS. Opportunistic, not exhaustive.

## Phase B — Semantic enforcement review (judgment; scoped)

For each in-scope **business rule** and **use case**, locate the owning code via the **BR → enforcement map** below (verify it against the code — the map is a starting point, not gospel) and judge enforcement. **Cite `file:method` (and line) as evidence for every finding.**

- **Per `BR-*`:** read the owning service method; does it actually enforce the rule (guards, uniqueness, ref-checks, status gates, visibility filters)? A rule with no enforcing code is a finding.
- **Per UC in scope:** does the cited code cover the **main success scenario AND every extension**? Extensions carry the edge-case requirements — an unhandled extension is a finding.
- **Classify each finding** by *nature*: **Code<Doc** (code under-implements the spec) · **Code≠Doc** (diverges) · **Doc<Code** (built but unspecified) · **Doc-wrong** (doc is inaccurate).
- **Reconcile:** drop anything already in the OI register; surface only **new** or **changed** drift.

### BR → enforcement-point map (verify each run; status lives in OPEN-ISSUES, not here)

| BR | Rule (short) | Owning code to read |
|----|--------------|---------------------|
| BR-1 | Team-scoped access to requirements | `security/authorizationmanagers/Team*AuthorizationManager`; `ram/*Specs` team filters |
| BR-2 | RBAC roles | `security/SecurityConfiguration`, `RoleHierarchyImpl` |
| BR-3 | Only admin creates/regenerates docs; regenerate destructive + confirm | `ram/document/DocumentService.createRequirementDocument`; SecurityConfiguration POST documents |
| BR-4 | Instructor-only AI config | CFG area (not built) |
| BR-5 | Artifact key unique + stable per team | `ram/requirement/RequirementArtifactService.generateNextArtifactKey`, `ArtifactKeySequence` |
| BR-6 | Glossary term name unique per team | `ram/glossary/GlossaryService` |
| BR-7 | Use case name unique per team | `ram/usecase/UseCaseService.saveUseCase` |
| BR-8 | Link self-link ban / uniqueness / tier matrix | `ram/requirement/ArtifactLinkService.createArtifactLink` |
| BR-9 | Lock required to edit | `ram/document/DocumentSectionService`, `ram/usecase/UseCaseService` update methods |
| BR-10 | Lock auto-release timeout | same; `ram.lock.default-lock-ttl` |
| BR-11 | Real-time no-overwrite | post-MVP / deferred |
| BR-12 | No delete if referenced; soft delete | `ram/requirement/RequirementArtifactService.deleteRequirementArtifact` |
| BR-13/14 | Review read-only; instructor accept/return | `ram/document/DocumentService.updateRequirementDocument` + section/UC edit paths; `DocumentStatus` |
| BR-15..18 | AI assistant behavior | AI area (not built) |
| BR-19 | Only team member imports source material | import (not built) |
| BR-20 | Comment access; allowed under review lock | `ram/collaboration/CommentService` + comment URL rules (teamMembership) |
| BR-21 | Team ≥1 instructor, commonly two | `team/TeamService`, `team/Team` entity; cf. `section/SectionService.removeInstructor` (section-level ≥1) |
| BR-22 | Active weeks; eval only active, WAR anytime | `evaluation/EvaluationService.addPeerEvaluation`; `activity/ActivityService.saveActivity` |
| BR-23 | Eval editable post-submit | `evaluation/EvaluationService.updatePeerEvaluation` |
| BR-24 | Eval previous-week / one-week window | `evaluation/EvaluationService` add vs update |
| BR-25 | Student sees only own scores/public/overall | `evaluation/EvaluationService.generate*` (private-comment nulling) |

Also spot-check cross-cutting: authorship/audit metadata (FR-HIS-4) on RAM entities; the `Clock` convention (`backend/CLAUDE.md` — no raw `LocalDateTime.now()`); frontend role-hierarchy expansion in `router/guards.ts`.

## Phase C — Report & record

1. **Chat summary** — a table: *finding · area · severity (🔴/🟠/🟡) · nature · evidence `file:method` · recommendation (fix-code / amend-doc / decide)*. Lead with the highest severity.
2. **Append draft OIs** for genuinely new gaps to `OPEN-ISSUES.md`, under the `## Doc ↔ Code Gap Analysis` section, using the **next free `OI-n`** (append-only, never renumber). Match the existing item format exactly: a checkbox, the title, an **owner tag** (✍️ Claude can draft · 🧑 needs user decision · 🔢 needs a value), the rule, what the code does with `file:method`, and the fix-code-vs-amend-doc decision. Tag the surfacing: *"Surfaced <date> by /sync-check."* Add a Change-log line.
3. **Resolved gaps** — list any known OI that now appears fixed; recommend the user check its box (don't silently close it).
4. **Update the baseline** — write/refresh the `**Last sync-check:** <commit-sha> (<date>), scope: <...>` line under the gap-analysis heading so the next run can diff from here.

## Guardrails

- **Report-mostly.** Never modify code. Never "fix" a divergence. The *only* permitted writes are: appending draft OIs and updating the baseline marker in `OPEN-ISSUES.md` (and listing — not auto-checking — resolved items).
- **Reconcile, don't duplicate.** Always read the existing OI register first; the 16+ known gaps (OI-19, OI-23, OI-24, OI-25..OI-39) are not "new findings."
- **Evidence required.** Every finding cites `file:method` (line where possible). No speculation — if you can't confirm from the code, say "unverified" and say what to check.
- **Don't re-report compliant rules.** BR-5, BR-22, BR-25, section-level ≥1-instructor, password-reset 5-min, frontend guards were verified compliant on 2026-06-20 — only flag them if they regressed.
- **`use-cases.md` is large (~250 KB).** Use Grep/targeted reads; never read it whole.
- **Honor the spec-is-fallible principle** (`CLAUDE.md`): a divergence may mean the *doc* is wrong, not the code. Present both directions; let the user decide.
- This run is **expensive** at `--full`. Prefer the default scoped mode for routine checks.
