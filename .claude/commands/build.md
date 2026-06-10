---
description: Verify and resync numbering, TOCs, anchors, and cross-doc consistency across the five requirements docs
---

# /build — Requirements docs build & verify

The five core Markdown files in this repo (`docs/ram/requirements/project-glossary.md`, `docs/ram/requirements/vision-and-scope.md`, `docs/ram/requirements/use-cases.md`, `docs/ram/requirements/business-rules.md`, `docs/ram/requirements/software-requirements-specification.md`) are the single source of truth for the project's requirements. This command treats them as artifacts that need to "compile": numbering sequential, TOC in sync with body, anchors resolve, terminology coherent.

**Two doc shapes, handled differently:**
- **Section-numbered docs** — `docs/ram/requirements/project-glossary.md`, `docs/ram/requirements/vision-and-scope.md`, `docs/ram/requirements/business-rules.md`, `docs/ram/requirements/software-requirements-specification.md`. These use the `# **N. Title**` / `## **N.M Title**` / `### *N.M.K Title*` scheme and get the full mechanical treatment, including heading renumbering. **Exception within `docs/ram/requirements/project-glossary.md`:** the term headings under `# **2. Definitions**` are a flat, **unnumbered** catalog (`## **Template**`, not `## **2.13 Template**`). Number the Section 1/2 headings (`# **1. …**`, `## **1.x …**`, `# **2. Definitions**`) as usual, but never number or renumber the term headings; their TOC entries use name-based slugs. **Note on `docs/ram/requirements/business-rules.md`:** it IS fully section-numbered (number its `# **1. …**`, `# **2. Business Rules**`, and the `## **2.x …**` thematic group headings normally), but its `BR-*` identifiers are an **identifier space independent of section numbering** — like `FR-*` in the SRS, never renumber a `BR-*` when sections move.
- **UC catalog** — `docs/ram/requirements/use-cases.md`. A catalog **grouped by area**: the top-level heading (`Use Case List`) stays **unnumbered** H1; use cases are grouped under **unnumbered H2 area sub-headings** (`## **Glossary**`, `## **AI Assistants**`, …); and each use case is an **H3** heading carrying an **area-prefixed ID** assigned by hand, append-only within its area — `### **UC-GLO-1: …**` (format `UC-<AREA>-<n>`). **Never impose the `# **N. Title**` scheme on it, and never renumber its UC IDs** — identity is deliberately decoupled from document position. It still gets anchor-stripping (Phase 1 step 2), TOC regeneration + reconciliation (Phase 1 steps 3–4), and every Phase 2 semantic check, plus a per-area UC-ID sequencing check (Phase 2 step 6).

**A sixth, derived artifact — `docs/ram/traceability.md`.** This is the spec→code matrix: one row per use case (`UC-<AREA>-<n>`), in the same area order as `docs/ram/requirements/use-cases.md`. It is NOT one of the five core docs and gets **no** mechanical treatment (no renumbering, no TOC, and `/build` never edits its human-maintained Status / Frontend / Backend / Tests columns). `/build` only **verifies** it in Phase 2 (step 7): every use case has exactly one row and every FR ID it cites resolves. When a row is missing or stale, report it — don't silently add or rewrite it.

Process all five docs in order. Phase 1 auto-fixes; Phase 2 reports only; Phase 3 summarizes.

## Phase 1 — mechanical checks (auto-fix in place)

For each .md file, fix the following without asking. Track every change for the Phase 3 report.

1. **Heading numbering (section-numbered docs only — NOT `docs/ram/requirements/use-cases.md`).** For `docs/ram/requirements/project-glossary.md`, `docs/ram/requirements/vision-and-scope.md`, and `docs/ram/requirements/software-requirements-specification.md`, walk the body headings in order (skip the doc title and the "Table of Contents" heading) and enforce:
   - H1 → `# **N. Title**` — N starts at 1, increments per H1.
   - H2 → `## **N.M Title**` — M resets per H1, increments per H2.
   - H3 → `### *N.M.K Title*` — K resets per H2, increments per H3.
   - The number lives **inside** the bold/italic markers, not outside.
   - Strip leftover Markdown ordered-list markers from heading lines (`1. # **...**` → `# **N. ...**`). These are Google Docs export residue.
   - Headings must NOT carry `{#slug}` attributes — step 2 strips any that remain.
   - **Skip `docs/ram/requirements/use-cases.md` entirely in this step.** Its top-level headings stay unnumbered H1, its area sub-headings stay unnumbered H2, and its use cases keep their H3 area-prefixed `### **UC-<AREA>-<n>: …**` headings — do not convert any of these into the `# **N. …**` scheme and do not renumber the UC IDs. Per-area UC-ID sequencing is verified (not auto-fixed) in Phase 2 step 6.
   - **In `docs/ram/requirements/project-glossary.md`, do NOT number the term headings under `# **2. Definitions**`.** Number `# **1. …**`, `## **1.x …**`, and the `# **2. Definitions**` heading itself normally, but leave every `## **Term**` under Definitions unnumbered (`## **Template**`, not `## **2.13 Template**`). If a term heading carries a leftover `N.M` prefix, strip it. Terms are a hand-ordered thematic catalog — never reorder or renumber them.

2. **Strip explicit anchor attributes.** Headings must NOT carry kramdown `{#slug}` attributes. Typora doesn't honor them as anchor targets, they show up as visual clutter in Typora's Outline panel, and they desync from auto-generated slugs after renumbering. If you find any `{#xxx}` tokens at the end of heading lines, strip them (along with any preceding whitespace).

3. **TOC sync.** Regenerate the TOC *from the body*, treating the body headings as the single source of truth — do NOT audit or patch the existing TOC in place (a deleted section leaves a stale entry that an in-place review misses). Procedure:
   - First extract the **complete** ordered list of body headings (every H1/H2/H3, excluding the doc title and the "Table of Contents" heading). If your heading search is paginated or truncated, page through until you have all of them — never regenerate from a partial list.
   - Locate the existing "Table of Contents" block (between the `# **Table of Contents**` heading and the next H1) and replace its entire entry list with one built solely from that extracted heading list. Any old entry whose heading no longer exists in the body must therefore disappear.
   - Heading: `# **Table of Contents**` (consistent across all five docs).
   - Format: nested Markdown bullet list, two spaces of indent per nesting level.
   - Include all heading levels (H1 + H2 + H3).
   - Each entry: `- [N.M Title](#slug)` — no page numbers, no tab separators.
   - **Slug rule (must match what Typora/GitHub auto-generate):** take the rendered heading text *including* the number prefix → strip Markdown formatting markers (`**`, `*`) → lowercase → replace whitespace with `-` → strip every character that isn't `[a-z0-9-]` (this removes `.`, `()`, `,`, `/`, `<>`, etc.) → do NOT collapse consecutive hyphens (a `/` between spaces yields `--` and that's correct). Examples:
     - `## **1.1 Background**` → `#11-background`
     - `### *4.2.1 Graph-First Requirements Model*` → `#421-graph-first-requirements-model`
     - `## **4.2 Major Features / Scope**` → `#42-major-features--scope`
   - **For `docs/ram/requirements/use-cases.md`** the same procedure applies to its actual headings, producing a **grouped, three-level TOC**: the unnumbered H1 sections (`- [Use Case List](#use-case-list)`), the H2 area sub-headings nested one level under Use Case List (`  - [Glossary](#glossary)`), and the H3 use-case headings nested under their area (`    - [UC-GLO-1: The Student views the Project Glossary](#uc-glo-1-the-student-views-the-project-glossary)`). The slug rule is unchanged: the `UC-<AREA>-<n>:` prefix is part of the rendered text and therefore part of the slug.
   - **For `docs/ram/requirements/project-glossary.md`** the Section 1/2 headings produce numbered TOC entries as usual, but each term under `# **2. Definitions**` produces a **name-based** entry (`- [Template](#template)`, `- [Validation (ReqLint)](#validation-reqlint)`) — the slug rule applied to the term name with no number prefix.
   - **Square brackets are a known trap.** Typora interprets `[...]` inside heading text as Markdown reference-link syntax and computes a non-obvious slug. If you find a heading with `[` or `]` (escaped or not) in the visible text, flag it in Phase 3 and recommend rewriting the heading text without brackets rather than trying to derive a matching slug. The same caution applies to other Markdown-significant characters (backticks, `!`, `<>` HTML tags) appearing in heading text.

4. **TOC ↔ body reconciliation.** After regenerating, verify the TOC and body agree in **both** directions — this is the check that catches deleted/renamed sections, not just slug typos:
   - **Every TOC entry has a matching body heading.** No entry may point at an `#anchor` whose heading was removed or renumbered (the dangling-entry case). Confirm existence in the body, not just that the slug is well-formed.
   - **Every body heading appears exactly once in the TOC**, at the right nesting level and in document order.
   - Each entry's `#anchor` matches the slug Typora would compute for its heading (slug rule above).
   - Any discrepancy is a bug in your regeneration — fix before moving on.

5. **Normalize quotation marks to straight ASCII quotes.** Convert curly/smart quotes to their straight equivalents everywhere **except inside fenced code blocks** (` ``` ` / `~~~`, e.g. Mermaid diagrams — leave those verbatim):
   - `“` `”` `„` `‟` `″` → `"`
   - `‘` `’` `‚` `‛` `′` → `'`
   - **Quotes only.** Do NOT touch em-dash (`—`), en-dash (`–`), or ellipsis (`…`) — unlike `gdoc-cleanup` (which ASCII-folds those for fresh exports), these maintained docs deliberately use `—` and `…` as prose typography ("The system shall…", em-dash asides), and folding them would be wrong here. Likewise leave non-breaking spaces alone unless they are clearly stray.
   - Smart quotes accumulate in these docs via editor autocorrect and pasted content; this step keeps them from creeping back. Count the replacements per file for the Phase 3 report.

## Phase 2 — semantic checks (report only, do not edit)

These need user judgment. Surface them; don't fix.

**The spec is authoritative but not infallible — don't limit yourself to the checklist below.** The enumerated checks are the floor, not the ceiling. If, while sweeping the docs, you notice a substantive spec defect — two docs that contradict each other, a use-case step that no FR backs (or that quietly restates a §5.2 FR), an extension that can't be tested as written, a "shall" statement that isn't atomic or testable, an assumption that looks wrong — **flag it and challenge it**, even when no Phase 2 rule names it. Phrase it as a question or a recommended fix for the user to decide; never silently normalize over a meaning problem while fixing formatting. (Mechanical normalization in Phase 1 stays automatic — this applies to substance, not whitespace.)

1. **Glossary coverage.** Any concept used as a domain term in docs 2–5 should be defined in `docs/ram/requirements/project-glossary.md`. Flag terms that look domain-specific but are undefined there. Don't flag generic English words or common technical terms.

2. **FR ID references.** If text in any doc references an FR ID (e.g., `FR-VAL-3`, `FR-LOCK-2`), the ID must exist as a definition in `docs/ram/requirements/software-requirements-specification.md`. Flag dangling references. **Scope includes `docs/ram/traceability.md`** — its `FR IDs` column mixes specific IDs (`FR-AI-6`) and category wildcards (`FR-GLO*`); verify each specific ID resolves and leave wildcards alone.

2b. **BR ID references.** If text in any doc references a `BR-*` ID (e.g., in a use case's **Business Rules** field, or where the SRS cites a rule an FR enforces), the ID must exist as a definition in `docs/ram/requirements/business-rules.md`. Flag dangling references. Also flag any `BR-*` definition in `docs/ram/requirements/business-rules.md` that no use case or SRS requirement cites (an orphan rule — report only, since a rule may legitimately be system-wide).

3. **Cross-section references.** If prose says "see Section 4.2" or similar, the referenced section must exist with that number. Flag stale references.

4. **ReqLint-style writing issues** — apply only to `FR-*` "shall" statements in the SRS:
   - Vague verbs: `manage`, `support`, `handle`, `process`, `deal with`
   - Subjective adjectives: `fast`, `user-friendly`, `intuitive`, `easy`, `quick`, `appropriate`, `seamless`
   - Missing `shall` in FR statements
   - Non-atomic (multiple requirements jammed into one FR)

5. **Cross-doc terminology drift.** Same concept named differently in different docs. Note: SRS Section 4 describing "Project Pulse" (Vue.js + Spring Boot + DB + Gmail) as the host platform is NOT drift — RAM is a module inside Project Pulse and the architecture is unified. Flag the reverse: RAM-specific additions described as a separate system rather than extensions of the Project Pulse containers. Surface any other drift you find.

6. **Per-area UC-ID sequencing (`docs/ram/requirements/use-cases.md`).** Use-case IDs follow `UC-<AREA>-<n>` and are **append-only within an area** — identity is decoupled from document position, so there is NO global sequence and document order need not be contiguous across areas. Verify that within each area the `<n>` run `1..max` with no gaps or duplicates, that every use case sits under the H2 area sub-heading matching its ID's `<AREA>`, that each H3 heading's ID matches the `UC ID and Name:` field in that use case's table (match on the ID, not the title text — heading and field wording legitimately differ), and that inline `UC-<AREA>-<n>: <name>` cross-references resolve to an existing use case. Flag gaps, duplicates, misfiled use cases, and mismatches — do NOT renumber automatically; UC IDs are stable handles referenced across the repo and renumbering has cross-reference fallout that needs your judgment.

7. **Traceability coverage (`docs/ram/traceability.md`).** Cross-check the matrix against `docs/ram/requirements/use-cases.md` in **both** directions: every `UC-<AREA>-<n>` in `docs/ram/requirements/use-cases.md` has exactly one row in the matrix, and every row in the matrix corresponds to an existing use case (no orphan rows for deleted use cases). Flag missing rows (a use case with no matrix row — the gap that motivated this check), orphan rows, and duplicates. Report only — do NOT add or edit rows yourself; the Status and code columns are human-maintained, and a new row needs a deliberate `❌ Not started` entry with the use case's specific `Implements` FR IDs.

## Phase 3 — report

Output a concise summary with two sections:

**Fixed (mechanical):**
- Bullet list by file, e.g., `` `docs/ram/requirements/vision-and-scope.md`: renumbered 3 headings, stripped 5 leftover `{#slug}` attributes, regenerated TOC ``.
- If a file was already clean, say so explicitly (`` `docs/ram/requirements/project-glossary.md`: no changes ``).

**Flagged (semantic — needs your decision):**
- Bullet list with `file_path:line_number` references, one line per issue. Group by category from Phase 2.
- If no semantic issues found, say "No semantic issues found."

## Notes

- `docs/ram/requirements/use-cases.md` is large (~88 KB). Use Grep to locate headings and references — only Read targeted sections when you must inspect content. Never read it whole.
- **FR IDs are independent of section numbering.** SRS §5.2 FRs (`FR-SAVE-1`, `FR-LOCK-3`, etc.) use the `FR-<AREA>-<n>` format (parallel to `UC-<AREA>-<n>`): FR-SAVE-* autosave, FR-LOCK-* locking, FR-COL-* collaboration, FR-VAL-* validation, FR-AI-* AI, FR-TPL-* templates, FR-GLO-* glossary, FR-HIS-* history & authorship metadata, FR-SEC-* security, FR-EXP-* export, FR-IMP-* import, FR-PERF-* performance, FR-NOT-* notifications. §5.2 is for non-use-case behaviors only — a use case is itself a high-level FR (its steps are its spec), so do not expect or add a separate FR that merely restates a use-case CRUD flow. Do NOT renumber FR IDs as part of section renumbering — they're a separate identifier space, append-only within each area.
- **Doc titles are NOT body sections.** The first `# **...**` at the top of each file (and any verbatim repetition of the doc name appearing as H1 between the TOC and the first numbered section) is the doc title and must NOT be numbered.
- The author-guidance blocks in square-bracketed italics (e.g., `*[Note: ...]*`) are part of the Wiegers/Beatty template — leave them alone unless you're explicitly asked to finalize the doc.
- If you find a structural ambiguity the rules above don't resolve, stop and ask rather than guessing.
