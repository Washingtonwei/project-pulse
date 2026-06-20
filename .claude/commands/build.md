---
description: Verify and resync anchors, cross-references, and cross-doc consistency across the requirements docs
---

# /build — Requirements docs build & verify

The five core Markdown files in this repo (`docs/requirements/project-glossary.md`, `docs/requirements/vision-and-scope.md`, `docs/requirements/use-cases.md`, `docs/requirements/business-rules.md`, `docs/requirements/software-requirements-specification.md`) are the single source of truth for the project's requirements. This command treats them as artifacts that need to "compile": **headings unnumbered, anchors resolving, cross-references resolving, terminology coherent.**

**The source carries no section numbers and no table of contents.** Headings are plain text (`# **Title**`); a heading's identity is its **name-based slug**, not a position — so inserting, removing, or reordering a section renumbers nothing and rots no anchor. Numbered headings and a TOC are a *delivery* concern, generated at export time (pandoc), not maintained in source. See `docs/CLAUDE.md` for the authoring conventions this command enforces.

**Doc shapes.** All five docs use unnumbered headings; two have catalog structure worth naming:
- **`docs/requirements/project-glossary.md`** — under `# **Definitions**`, a flat catalog of **unnumbered term headings** (`## **Template**`, `## **Validation (ReqLint)**`); slugs are name-based. Terms are a hand-ordered thematic catalog — never reorder them.
- **`docs/requirements/use-cases.md`** — a catalog **grouped by area**: the top-level heading (`Use Case List`) is an unnumbered H1; use cases are grouped under **unnumbered H2 area sub-headings** (`## **Glossary**`, `## **AI Assistants**`, …); and each use case is an **H3** carrying an **area-prefixed ID** assigned by hand, append-only within its area — `### **UC-GLO-1: …**` (format `UC-<AREA>-<n>`). **Never renumber the UC IDs** — identity is deliberately decoupled from document position.

**A sixth, derived artifact — `docs/traceability.md`.** This is the spec→code matrix: one row per use case (`UC-<AREA>-<n>`), in the same area order as `docs/requirements/use-cases.md`. It is NOT one of the five core docs and gets **no** mechanical treatment (and `/build` never edits its human-maintained Status / Frontend / Backend / Tests columns). `/build` only **verifies** it in Phase 2 (step 7): every use case has exactly one row and every FR ID it cites resolves. When a row is missing or stale, report it — don't silently add or rewrite it.

Process all five docs in order. Phase 1 auto-fixes; Phase 2 reports only; Phase 3 summarizes.

## Phase 1 — mechanical checks (auto-fix in place)

For each .md file, fix the following without asking. Track every change for the Phase 3 report.

1. **Strip section-number prefixes from headings.** Headings must NOT carry a leading `N.` / `N.M` / `N.M.K` number. If you find one — `## **2.1 Access and Ownership**`, `### *4.2.5 AI/LLM Integration Requirements*`, or a stray ordered-list marker (`1. # **...**`, Google Docs export residue) — strip the number prefix, leaving the text inside the bold/italic markers (`## **Access and Ownership**`, `### *AI/LLM Integration Requirements*`). The doc-title H1s, the product-name H1, and the "Revision History" heading carry no number and are left as-is. **Skip the use-case IDs in `docs/requirements/use-cases.md`** — `### **UC-GLO-1: …**` is an ID, not a section number; never strip or renumber it.

2. **Strip explicit anchor attributes.** Headings must NOT carry kramdown `{#slug}` attributes. Typora doesn't honor them as anchor targets, they show up as visual clutter in Typora's Outline panel, and they desync from the auto-generated slugs. If you find any `{#xxx}` tokens at the end of heading lines, strip them (along with any preceding whitespace).

3. **Remove any stale Table of Contents.** The source docs carry no TOC. If a `# **Table of Contents**` heading and its bullet list survive in a doc, delete the heading and the entire list up to the next H1. Do **not** regenerate one — navigation is handled by the editor's outline panel (Typora) and GitHub's auto-rendered heading outline, and a numbered TOC is produced at export time.

4. **Normalize quotation marks to straight ASCII quotes.** Convert curly/smart quotes to their straight equivalents everywhere **except inside fenced code blocks** (` ``` ` / `~~~`, e.g. Mermaid diagrams — leave those verbatim):
   - `“` `”` `„` `‟` `″` → `"`
   - `‘` `’` `‚` `‛` `′` → `'`
   - **Quotes only.** Do NOT touch em-dash (`—`), en-dash (`–`), or ellipsis (`…`) — these maintained docs deliberately use `—` and `…` as prose typography. Likewise leave non-breaking spaces alone unless they are clearly stray.
   - Count the replacements per file for the Phase 3 report.

## Phase 2 — semantic checks (report only, do not edit)

These need user judgment. Surface them; don't fix.

**The spec is authoritative but not infallible — don't limit yourself to the checklist below.** The enumerated checks are the floor, not the ceiling. If, while sweeping the docs, you notice a substantive spec defect — two docs that contradict each other, a use-case step that no FR backs (or that quietly restates a Non-Use Case Functional Requirement), an extension that can't be tested as written, a "shall" statement that isn't atomic or testable, an assumption that looks wrong — **flag it and challenge it**, even when no Phase 2 rule names it. Phrase it as a question or a recommended fix for the user to decide; never silently normalize over a meaning problem while fixing formatting. (Mechanical normalization in Phase 1 stays automatic — this applies to substance, not whitespace.)

1. **Glossary coverage.** Any concept used as a domain term in docs 2–5 should be defined in `docs/requirements/project-glossary.md`. Flag terms that look domain-specific but are undefined there. Don't flag generic English words or common technical terms.

2. **FR ID references.** If text in any doc references an FR ID (e.g., `FR-VAL-3`, `FR-LOCK-2`), the ID must exist as a definition in `docs/requirements/software-requirements-specification.md`. Flag dangling references. **Scope includes `docs/traceability.md`** — its `FR IDs` column mixes specific IDs (`FR-AI-6`) and category wildcards (`FR-GLO*`); verify each specific ID resolves and leave wildcards alone.

2b. **BR ID references.** If text in any doc references a `BR-*` ID (e.g., in a use case's **Business Rules** field, or where the SRS cites a rule an FR enforces), the ID must exist as a definition in `docs/requirements/business-rules.md`. Flag dangling references. Also flag any `BR-*` definition that no use case or SRS requirement cites (an orphan rule — report only, since a rule may legitimately be system-wide).

3. **Cross-reference resolution.** Cross-references between sections are **name-based** — either a Markdown link `[Heading Text](file.md#slug)` (or `[Heading Text](#slug)` within a doc), or prose that names a section ("the Operating Environment section," "the SRS's Quality Attributes"). For every such link, verify its `#slug` resolves to an existing heading in the target file by the **slug rule** below. Flag dangling links (no matching heading) and prose that names a section or document that doesn't exist.
   - **Any leftover `§N.M` or "Section N" reference is stale** (section numbers were removed from source) — flag it; it should be rewritten as a name-based reference.
   - **Slug rule (must match what Typora/GitHub auto-generate):** take the rendered heading text → strip Markdown markers (`**`, `*`) → lowercase → replace whitespace with `-` → strip every character that isn't `[a-z0-9-]` (removes `.`, `()`, `,`, `/`, `<>`, etc.) → do NOT collapse consecutive hyphens (a `/` between spaces yields `--`, and that's correct). Examples:
     - `## **Operating Environment**` → `#operating-environment`
     - `## **Major Features / Scope**` → `#major-features--scope`
     - `## **Validation (ReqLint)**` → `#validation-reqlint`
     - `### **UC-GLO-1: The Student views the Project Glossary**` → `#uc-glo-1-the-student-views-the-project-glossary`
   - **Square brackets are a known trap.** Typora interprets `[...]` inside heading text as Markdown reference-link syntax and computes a non-obvious slug. Flag any heading whose visible text contains `[` or `]` (escaped or not) and recommend rewriting it without brackets rather than deriving a matching slug. Same caution for backticks, `!`, and `<>` HTML in heading text.

3b. **Duplicate heading text within a doc.** Name-based slugs collide when two headings in the same file share text (the renderer disambiguates the second as `slug-1`, which is fragile to link to). Flag any duplicate heading text in a doc — including a body section whose text equals the doc title — and recommend renaming one. (Identical headings across *different* docs are fine; slugs are per-file.)

4. **ReqLint-style writing issues** — apply only to `FR-*` "shall" statements in the SRS:
   - Vague verbs: `manage`, `support`, `handle`, `process`, `deal with`
   - Subjective adjectives: `fast`, `user-friendly`, `intuitive`, `easy`, `quick`, `appropriate`, `seamless`
   - Missing `shall` in FR statements
   - Non-atomic (multiple requirements jammed into one FR)

5. **Cross-doc terminology drift.** Same concept named differently in different docs. Note: describing "Project Pulse" (Vue.js + Spring Boot + DB + Gmail) as RAM's host platform is NOT drift — RAM is a module inside Project Pulse and the architecture is unified (the single architecture-of-record lives in `docs/design/architectural-design.md`). Flag the reverse: RAM-specific additions described as a separate system rather than extensions of the Project Pulse containers. Surface any other drift you find.

6. **Per-area UC-ID sequencing (`docs/requirements/use-cases.md`).** Use-case IDs follow `UC-<AREA>-<n>` and are **append-only within an area** — identity is decoupled from document position, so there is NO global sequence and document order need not be contiguous across areas. Verify that within each area the `<n>` run `1..max` with no gaps or duplicates, that every use case sits under the H2 area sub-heading matching its ID's `<AREA>`, that each H3 heading's ID matches the `UC ID and Name:` field in that use case's table (match on the ID, not the title text), and that inline `UC-<AREA>-<n>: <name>` cross-references resolve to an existing use case. Flag gaps, duplicates, misfiled use cases, and mismatches — do NOT renumber automatically; UC IDs are stable handles referenced across the repo.

7. **Traceability coverage (`docs/traceability.md`).** Cross-check the matrix against `docs/requirements/use-cases.md` in **both** directions: every `UC-<AREA>-<n>` has exactly one row, and every row corresponds to an existing use case (no orphan rows). Flag missing rows, orphan rows, and duplicates. Report only — do NOT add or edit rows yourself; the Status and code columns are human-maintained, and a new row needs a deliberate `❌ Not started` entry with the use case's specific `Implements` FR IDs.

## Phase 3 — report

Output a concise summary with two sections:

**Fixed (mechanical):**
- Bullet list by file, e.g., `` `docs/requirements/business-rules.md`: stripped 11 heading number prefixes, removed stale TOC, normalized 3 smart quotes ``.
- If a file was already clean, say so explicitly (`` `docs/requirements/project-glossary.md`: no changes ``).

**Flagged (semantic — needs your decision):**
- Bullet list with `file_path:line_number` references, one line per issue. Group by category from Phase 2.
- If no semantic issues found, say "No semantic issues found."

## Notes

- `docs/requirements/use-cases.md` is large (~250 KB — it now holds the platform-core areas plus the requirements-authoring areas). Use Grep to locate headings and references — only Read targeted sections when you must inspect content. Never read it whole.
- **IDs are independent of headings and never renumber.** SRS Non-Use Case Functional Requirements use the `FR-<AREA>-<n>` format (parallel to `UC-<AREA>-<n>`): FR-SAVE-* autosave, FR-LOCK-* locking, FR-COL-* collaboration, FR-VAL-* validation, FR-AI-* AI, FR-TPL-* templates, FR-GLO-* glossary, FR-HIS-* history & authorship metadata, FR-SEC-* security, FR-EXP-* export, FR-IMP-* import, FR-PERF-* performance, FR-NOT-* notifications. Business rules use `BR-<n>`. A use case is itself a high-level FR (its steps are its spec), so do not expect or add a separate FR that merely restates a use-case CRUD flow. None of these IDs are section numbers — never strip or renumber them.
- **Name-based slugs are stable on insert, fragile on rename.** Adding or reordering a section breaks nothing. **Renaming** a heading changes its slug, so any inbound `[…](#old-slug)` link must be updated — Phase 2 step 3 catches the dangling ones.
- **Doc titles are NOT body sections.** The first `# **...**` at the top of each file (the product name and the doc title), and any verbatim repetition of the doc name appearing as an H1 before the first real section, are title scaffolding — leave their text as-is. If a *real body section* duplicates the doc title, that's a collision to flag (Phase 2 step 3b), not a title.
- The author-guidance blocks in square-bracketed italics (e.g., `*[Note: ...]*`) are part of the Wiegers/Beatty template — leave them alone unless you're explicitly asked to finalize the doc.
- If you find a structural ambiguity the rules above don't resolve, stop and ask rather than guessing.
