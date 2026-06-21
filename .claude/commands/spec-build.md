---
description: Verify and resync anchors, cross-references, and cross-doc consistency across the requirements docs
---

# /spec-build — Requirements docs build & verify

The five core Markdown files in this repo (`docs/requirements/project-glossary.md`, `docs/requirements/vision-and-scope.md`, `docs/requirements/use-cases.md`, `docs/requirements/business-rules.md`, `docs/requirements/software-requirements-specification.md`) are the single source of truth for the project's requirements. This command treats them as artifacts that need to "compile": **headings unnumbered, anchors resolving, cross-references resolving, terminology coherent.**

**The source carries no section numbers and no table of contents.** Headings are plain text (`# **Title**`); a heading's identity is its **name-based slug**, not a position — so inserting, removing, or reordering a section renumbers nothing and rots no anchor. Numbered headings and a TOC are a *delivery* concern, generated at export time (pandoc), not maintained in source. See `docs/CLAUDE.md` for the authoring conventions this command enforces.

**Doc shapes.** All five docs use unnumbered headings; two have catalog structure worth naming:
- **`docs/requirements/project-glossary.md`** — under `# **Definitions**`, a flat catalog of **unnumbered term headings** (`## **Template**`, `## **Validation (ReqLint)**`); slugs are name-based. Terms are a hand-ordered thematic catalog — never reorder them.
- **`docs/requirements/use-cases.md`** — a catalog **grouped by area**: the top-level heading (`Use Case List`) is an unnumbered H1; use cases are grouped under **unnumbered H2 area sub-headings** (`## **Glossary**`, `## **AI Assistants**`, …); and each use case is an **H3** carrying an **area-prefixed ID** assigned by hand, append-only within its area — `### **UC-GLO-1: …**` (format `UC-<AREA>-<n>`). **Never renumber the UC IDs** — identity is deliberately decoupled from document position.

**A sixth, derived artifact — `docs/traceability.md`.** This is the spec→code matrix: one row per use case (`UC-<AREA>-<n>`), in the same area order as `docs/requirements/use-cases.md`. It is NOT one of the five core docs and gets **no** mechanical treatment (and `/spec-build` never edits its human-maintained Status / Design / Frontend / Backend / Tests columns). `/spec-build` only **verifies** it in Phase 2a (the *traceability coverage* and *traceability completeness* checks): every use case has exactly one row and every FR ID it cites resolves. When a row is missing or stale, report it — don't silently add or rewrite it.

Process all five docs in order. Phase 1 auto-fixes; Phase 2 reports only — split into **2a** (deterministic, always run in full) and **2b** (judgment, scoped to what changed); Phase 3 summarizes.

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

## Phase 2 — semantic checks (report only, never edit)

Phase 2 never edits a file — it surfaces problems for the user to judge. It runs in **two passes** with very different cost and reliability profiles, and the Phase 3 report must say which coverage each got:

- **Phase 2a — deterministic checks.** Referential-integrity and structural-completeness checks that are mechanically decidable: extract a set, diff it, count it. **Run these in full, across all five docs, every time** — they're cheap and have a definite yes/no answer. Drive them with Grep, not by reading prose.
- **Phase 2b — judgment checks.** Requirements-quality, terminology, and conflict checks that need reading and reasoning, have no ground truth, and degrade against a ~275 KB corpus. **Scope them to what changed** (see *Scoping Phase 2b*) and report best-effort, naming exactly what was covered. **Never claim a full 2b sweep you didn't do** — an honest "checked the 3 changed sections" beats a false "all clean."

**The spec is authoritative but not infallible — don't limit yourself to the checklist below.** The enumerated checks are the floor, not the ceiling. If, while sweeping the docs, you notice a substantive spec defect — two docs that contradict each other, a use-case step that no FR backs (or that quietly restates a Non-Use Case Functional Requirement), an extension that can't be tested as written, a "shall" statement that isn't atomic or testable, an assumption that looks wrong — **flag it and challenge it**, even when no check below names it. Phrase it as a question or a recommended fix for the user to decide; never silently normalize over a meaning problem while fixing formatting. (Mechanical normalization in Phase 1 stays automatic — this applies to substance, not whitespace.)

Checks are referred to **by name** (not number) everywhere else in the repo, so reordering them rots no cross-reference — the same identity-decoupled-from-position rule the docs themselves follow.

### Phase 2a — deterministic checks (always run in full; report only)

- **ID reference resolution — every ID space, not just FR/BR.** Each ID an artifact defines is a handle other artifacts point at: a reference resolving to no definition is a dangling pointer; two definitions of one ID is a collision. For **every** ID space, verify references resolve, definitions are unique, and — where the space is a contiguous sequence — the numbers run `1..max` with no gaps:
  - **`FR-<AREA>-<n>`** — defined in `software-requirements-specification.md`; referenced from use cases' *Realized by* / *honors* lines, vision, and `traceability.md`'s `FR IDs` column. Flag dangling refs. **`traceability.md` carries specific IDs only** — the old `FR-<area>*` wildcards were removed (OI-2), so a wildcard token there is itself a defect, not something to skip.
  - **`BR-<n>`** — defined in `business-rules.md`; referenced in use cases' **Business Rules** field and where the SRS cites a rule. Flag dangling refs and **orphan rules** (a `BR-*` no UC or SRS cites — report only; a rule may legitimately be system-wide).
  - **The other key spaces** — `BO-*` / `RI-*` / `AS-*` / `DE-*` (vision + SRS), `CO-*` / `OE-*` / `UI-*` / `SI-*` / `CI-*` and the quality codes (`USE-*` / `PER-*` / `SEC-*` / `SAF-*` / `AVL-*` / `ROB-*` / `SCA-*` / `INT-*`) in the SRS, plus the per-type artifact keys. Verify each referenced ID resolves, no ID is defined twice, and team-wide sequences (e.g. `AS-*`, `DE-*`) don't fork into clashing runs (the OI-15 failure). Flag dangling refs, duplicate definitions, and gaps.

- **Cross-reference resolution.** Cross-references between sections are **name-based** — either a Markdown link `[Heading Text](file.md#slug)` (or `[Heading Text](#slug)` within a doc), or prose that names a section ("the Operating Environment section," "the SRS's Quality Attributes"). For every such link, verify its `#slug` resolves to an existing heading in the target file by the **slug rule** below. Flag dangling links (no matching heading) and prose that names a section or document that doesn't exist.
  - **Any leftover `§N.M` or "Section N" reference is stale** (section numbers were removed from source) — flag it; it should be rewritten as a name-based reference.
  - **Slug rule (must match what Typora/GitHub auto-generate):** take the rendered heading text → strip Markdown markers (`**`, `*`) → lowercase → replace whitespace with `-` → strip every character that isn't `[a-z0-9-]` (removes `.`, `()`, `,`, `/`, `<>`, etc.) → do NOT collapse consecutive hyphens (a `/` between spaces yields `--`, and that's correct). Examples:
    - `## **Operating Environment**` → `#operating-environment`
    - `## **Major Features / Scope**` → `#major-features--scope`
    - `## **Validation (ReqLint)**` → `#validation-reqlint`
    - `### **UC-GLO-1: The Student views the Project Glossary**` → `#uc-glo-1-the-student-views-the-project-glossary`
  - **Square brackets are a known trap.** Typora interprets `[...]` inside heading text as Markdown reference-link syntax and computes a non-obvious slug. Flag any heading whose visible text contains `[` or `]` (escaped or not) and recommend rewriting it without brackets rather than deriving a matching slug. Same caution for backticks, `!`, and `<>` HTML in heading text.

- **Duplicate heading text within a doc.** Name-based slugs collide when two headings in the same file share text (the renderer disambiguates the second as `slug-1`, which is fragile to link to). Flag any duplicate heading text in a doc — including a body section whose text equals the doc title — and recommend renaming one. (Identical headings across *different* docs are fine; slugs are per-file.)

- **Per-area UC-ID sequencing (`use-cases.md`).** Use-case IDs follow `UC-<AREA>-<n>` and are **append-only within an area** — identity is decoupled from document position, so there is NO global sequence and document order need not be contiguous across areas. Verify that within each area the `<n>` run `1..max` with no gaps or duplicates, that every use case sits under the H2 area sub-heading matching its ID's `<AREA>`, that each H3 heading's ID matches the `UC ID and Name:` field in that use case's table (match on the ID, not the title text), and that inline `UC-<AREA>-<n>: <name>` cross-references resolve to an existing use case. Flag gaps, duplicates, misfiled use cases, and mismatches — do NOT renumber automatically; UC IDs are stable handles referenced across the repo.

- **Traceability coverage — UC ↔ row (`traceability.md`).** Cross-check the matrix against `use-cases.md` in **both** directions: every `UC-<AREA>-<n>` has exactly one row, and every row corresponds to an existing use case (no orphan rows). Flag missing rows, orphan rows, and duplicates. Report only — do NOT add or edit rows yourself; the Status and code columns are human-maintained, and a new row needs a deliberate `❌ Not started` entry with the use case's specific `Implements` FR IDs.

- **Traceability completeness — forward and backward, not just row existence.** Beyond every UC having a row, verify the requirement chain is closed both ways:
  - **Forward (no orphan scope):** every `vision-and-scope.md` feature and every `BO-*` is realized by at least one use case or FR. A feature nothing implements is unspecified promised scope — flag it.
  - **Backward (no unjustified requirement):** every use case and every `FR-*` traces up to a feature or business objective, and every `FR-*` is cited by at least one use case **or** is a standalone non-use-case FR by design. An `FR-*` that nothing references and isn't a recognized cross-cutting/non-UC FR is dead weight — flag it.
  - Report as coverage gaps; the user decides whether to add the missing link or drop the artifact.

- **Use-case structural completeness.** Each use case in `use-cases.md` must carry its required template fields: `UC ID and Name`, trigger, actor(s), preconditions, postconditions, a main success scenario, and **at least one extension**. The methodology makes a UC's extensions the *sole* record of its negative/edge behavior and tags tests to the whole UC — so a use case with **no extensions** (no exception flow specified) or **missing postconditions** (nothing to verify against) is a defect, not a style nit. Flag any UC missing a required field or with an empty extensions list. **Exclude deferred/tabled UCs** — a use case carrying a non-Active `**Status:**` line (`Deferred — post-MVP` or `Tabled — future release`; see the *use-case lifecycle status* convention in `docs/CLAUDE.md`) is a deliberate out-of-scope stub, not a half-written UC. **Detect deferral by that `**Status:**` field only** — an active UC carries no Status line; never infer deferral from prose phrasing or an italic note, which is unreliable. Report such UCs separately as "deferred/tabled (no full spec, as intended)" and don't count them as completeness defects; this mirrors the deferred-FR exclusion in *MVP / deferred-scope consistency*. (Currently `UC-ART-4` and `UC-COL-1`.)

- **MVP / deferred-scope consistency.** Some requirements are labelled deferred / post-MVP (e.g. `FR-HIS-1/2/3`, the OI-4/5/6 cluster). Flag any **live (non-deferred) use case that cites a deferred FR**, or a deferred feature whose use cases are presented as buildable — a UC can't be MVP-ready while depending on something explicitly out of scope.

- **Quality attributes are measurable (heuristic).** Every quality-attribute requirement in the SRS Quality Attributes section should state a verifiable threshold — a number with a unit and a condition (e.g. "95% of ReqLint runs complete within 3 s"). Flag any quality requirement that asserts a target with no number / unit / condition (it reads as prose, not a testable bound). Heuristic — a borderline case is a judgment call to surface, not a hard fail.

### Phase 2b — judgment checks (scope to the change set; report coverage honestly)

**Scoping Phase 2b.** Establish the change set *before* reading: run `git diff --stat HEAD -- docs/requirements docs/traceability.md` for the working-tree edits since the last commit — or, if the user asked for a fuller pass or just committed, widen to `git diff --stat HEAD~1` (or a range they give). The checks below apply to the **changed hunks and the requirements/sections they touch** (a reworded BR, a new UC, an edited FR) plus their immediate cross-references. If the working tree is clean and no range was given, say so and either **skip 2b** or run a small **declared** sample (e.g. one area) — never let silence imply a full sweep. Always name the scope you used in the Phase 3 report.

- **Glossary coverage — both directions.** Any concept used as a domain term in docs 2–5 should be defined in `project-glossary.md` — flag domain-specific terms used but undefined (don't flag generic English or common technical terms). Also flag the reverse where visible: a glossary term **defined but never used** anywhere (dead vocabulary — report only).

- **Terminology & convention enforcement.** The docs' own conventions (in `docs/CLAUDE.md`) are consistency rules with no other automated guard — enforce them on the changed prose:
  - **Never a bare "section"** for a domain concept — it must be qualified *course section* or *document section* (only a doc referring to its own parts, or template author-guidance, may say "section" unqualified).
  - **No synonym for a defined glossary term** — flag a coined alternative where the glossary already fixes the word (e.g. a new word for *authoring destination*, *requirement artifact*, *course admin*).
  - **Two-register capitalization** — glossary terms are lowercase in ordinary prose; Title Case is reserved for headings, UC titles, UI/table labels, doc titles, code identifiers/enums, and controlled artifact-type labels. Flag Title-Cased or bolded glossary terms in running prose.

- **ReqLint writing quality.** The atomic functional requirements live in **two registers**, and both get linted: **(a)** the non-use-case `FR-*` "shall" statements in the SRS, and **(b)** the **system-subject steps** ("The system …") and their **Associated Information** in `use-cases.md` — a use case is itself a high-level FR, so its system steps are the finer-grained FRs and carry the same quality bar (the Phase 2a *use-case structural completeness* check only verifies the UC's fields exist, never the writing quality of its steps). Apply the **register-neutral** checks to **both** registers; only the **register conformance** check differs between them. These map to the requirement-quality characteristics of ISO/IEC/IEEE 29148:2018 §5.2 (named in parentheses); most other §5.2 characteristics are already enforced elsewhere under different names (singular ≈ *non-atomic*; complete/consistent ≈ the traceability and conflict checks), so this bullet only adds what those don't cover:
  - Vague verbs (*unambiguous / verifiable*) — both registers: `manage`, `support`, `handle`, `process`, `deal with`.
  - Subjective adjectives (*verifiable*) — both registers: `fast`, `user-friendly`, `intuitive`, `easy`, `quick`, `appropriate`, `seamless`.
  - **Open-ended qualifiers (*unambiguous / complete*) — both registers:** `e.g.`, `etc.`, `and/or`, `as appropriate`, `as needed`, `where possible`, `TBD`, `TODO`. An example or an open list inside a requirement makes it unbounded — move the example into surrounding guidance prose and keep the requirement closed. (Known live instances: `FR-TPL-1`'s "(e.g., Wiegers, IEEE, RUP, custom)", `AS-7`'s "(e.g., OpenAI)".)
  - Non-atomic — multiple requirements jammed into one statement (*singular*) — both registers.
  - **Register conformance (*conforming*) — register-specific, do NOT apply blindly:** a non-use-case `FR-*` (register a) **must** use "shall" — flag a missing "shall" there. A use-case system step (register b) is deliberately **present-tense declarative** ("The system validates …"), **not** a "shall" statement — so **never flag a use-case step for lacking "shall"** (every one would false-positive), and conversely flag a "shall" that has crept into a use-case step as a register violation. **Actor-subject steps** ("The Student clicks …") describe user action, not system behavior — exempt them from all of the above.

- **Cross-doc terminology drift.** Same concept named differently across docs. Note: Project Pulse is the product and RAM is a module within it (the architecture-of-record is `docs/design/architectural-design.md`) — describe RAM as a module/extension of Project Pulse, never as a separate system, and **don't call Project Pulse RAM's "host platform"** (it's the product, not RAM's host). Flag a RAM addition described as a separate system rather than an extension of the Project Pulse containers, any "host platform" framing of Project Pulse, and any other drift you find.

- **Conflict / contradiction detection.** The highest-value judgment check, and historically the source of most open issues (BR-23 ↔ UC-EVA-1 on editability, UC-COL-2 ↔ UC-REV-2 on the review lock, BR-14's "accept" ↔ a `DocumentStatus` with no `ACCEPTED`). For each changed business rule or use case, check whether any artifact that cites it — or that it cites — asserts the opposite: a rule that permits X against a UC that forbids X, a postcondition that contradicts a precondition, a state/transition named in one doc but absent from the data model in another. Flag conflicts as questions for the user; don't pick a side silently.

- **"Link, don't restate" duplication.** The methodology forbids the SRS restating a use-case CRUD flow as a separate FR. Flag a non-use-case `FR-*` that merely re-describes a use case's create / view / edit / delete / search steps (it belongs in the UC, not as a parallel FR), and any requirement text near-duplicated across two docs instead of stated once and linked.

## Phase 3 — report

Output a concise summary with three sections, so the report never overstates coverage:

**Fixed (Phase 1 — mechanical):**
- Bullet list by file, e.g., `` `docs/requirements/business-rules.md`: stripped 11 heading number prefixes, removed stale TOC, normalized 3 smart quotes ``.
- If a file was already clean, say so explicitly (`` `docs/requirements/project-glossary.md`: no changes ``).

**Verified (Phase 2a — deterministic, full pass):**
- One line per check with its result, e.g. `` ID resolution: all FR/BR/AS/DE/quality IDs resolve ``, `` traceability: 96 UC ↔ 96 rows, no orphans ``, `` UC structural completeness: 2 UCs missing extensions (UC-…, UC-…) ``. State plainly that this pass was full.
- List any 2a defect with a `file_path:line_number` reference.

**Flagged (Phase 2b — judgment, scoped):**
- **First state the scope you ran 2b over** — the changed sections / area you covered, or "working tree clean — 2b skipped" / "sampled area X." Don't let silence imply more coverage than you did.
- Then one line per issue with `file_path:line_number`, grouped by check.
- If nothing was flagged within the covered scope, say "No issues in the covered scope" — not "no issues."

## Notes

- `docs/requirements/use-cases.md` is large (~275 KB — it now holds the foundation and performance-tracking areas plus the requirements-authoring areas). Use Grep to locate headings and references — only Read targeted sections when you must inspect content. Never read it whole.
- **IDs are independent of headings and never renumber.** SRS Non-Use Case Functional Requirements use the `FR-<AREA>-<n>` format (parallel to `UC-<AREA>-<n>`): FR-SAVE-* autosave, FR-LOCK-* locking, FR-COL-* collaboration, FR-VAL-* validation, FR-AI-* AI, FR-TPL-* templates, FR-GLO-* glossary, FR-HIS-* history & authorship metadata, FR-SEC-* security, FR-EXP-* export, FR-IMP-* import, FR-NOT-* notifications. Business rules use `BR-<n>`. A use case is itself a high-level FR (its steps are its spec), so do not expect or add a separate FR that merely restates a use-case CRUD flow. None of these IDs are section numbers — never strip or renumber them.
- **Name-based slugs are stable on insert, fragile on rename.** Adding or reordering a section breaks nothing. **Renaming** a heading changes its slug, so any inbound `[…](#old-slug)` link must be updated — Phase 2a's *cross-reference resolution* check catches the dangling ones.
- **Doc titles are NOT body sections.** The first `# **...**` at the top of each file (the product name and the doc title), and any verbatim repetition of the doc name appearing as an H1 before the first real section, are title scaffolding — leave their text as-is. If a *real body section* duplicates the doc title, that's a collision to flag (Phase 2a's *duplicate heading text* check), not a title.
- The author-guidance blocks in square-bracketed italics (e.g., `*[Note: ...]*`) are part of the Wiegers/Beatty template — leave them alone unless you're explicitly asked to finalize the doc.
- If you find a structural ambiguity the rules above don't resolve, stop and ask rather than guessing.
