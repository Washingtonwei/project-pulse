# Critique Assistant — Build & Operation Notes (shared)

> Build and operation guidance shared by the critique assistant's **two review modes**: the per-destination critique ([`requirement-quality-criteria.md`](requirement-quality-criteria.md)) and the whole-project review ([`cross-document-review-criteria.md`](cross-document-review-criteria.md)). Each of those docs carries its own review criteria (Part 1) and its own paste-ready system prompt (Part 2). This doc holds the construction, enforcement, and output rules **common to both**, so neither sibling restates them — each references this doc and adds only its mode-specific notes.

## Assistant anatomy

A critique assistant = a **system prompt + a context builder + one model call**, executed server-side behind the AI proxy. The two modes are the *same engine*; they differ only in their system prompt and the context they are fed.

## Build notes (common to both modes)

- **Deterministic first.** Run the mechanical checks in code before calling the model — ReqLint format/wordlist/placeholder checks, required-section presence, and ID resolution (does every cited `FR-*`/`BR-*`/`UC-*` exist? every glossary term used? one traceability row per use case?). Hand the model the *results* (e.g. "these FRs are orphaned, these terms undefined") so it spends its reasoning on judgment — ambiguity, conflicts, drift, scope — rather than rediscovering broken links. Faster, cheaper, more reliable.
- **Structured output.** Each mode returns its findings as JSON via tool/JSON mode, so the output is unit-testable. Each sibling's Part 2 defines its exact JSON shape.
- **Prompt caching.** The course section's teaching context and the large, stable document content (or its coverage map) are stable within a review session — cache them.
- **Read-only.** A critique neither locks nor modifies anything. Acting on a finding goes through the normal authoring path — a manual edit (UC-DOC-edit-document / UC-DOC-edit-use-case) or an accepted rewrite via UC-AI-review-proposal — never a direct write from the assistant.

## Spec-mandated guardrails (the spec is authoritative; the server enforces it)

In spec-driven development the **specification is the source of truth** — these guardrails are *required by the spec*, and the implementation conforms to it. Because the model's output, the browser client, and student content are all **untrusted** (a prompt-injection surface), the spec's guardrails must be enforced at a trusted point — the **server** — and never delegated to the system prompt or the client:

- **Enablement** — the critique assistant runs only where it is enabled for the course section (FR-AI-enablement, UC-CFG-toggle-assistants).
- **Never author without acceptance** — the assistant proposes; content changes only through the student's explicit, per-item acceptance (FR-AI-no-auto-edit, BR-explicit-acceptance; UC-AI-review-proposal). No silent writes, no "accept all".
- **Validate model-chosen targets** — confirm that any ID, document section, or use case the model names actually exists before acting on it.

## Findings output discipline (common to both modes)

Every finding =
- a **location** — the specific item / step it concerns,
- a **category** — the reviewing dimension (each mode names its own set),
- a **severity** — `blocker` / `major` / `minor`,
- the **problem** — one line, what is wrong,
- the **rationale** — *why* it weakens the requirements; this is the instructive lesson (BR-assistant-socratic), phrased for a student to learn from,
- and **a suggested fix _or_ a clarifying question** when the call is genuinely the team's.

Order findings by **severity**. **Never fabricate** an ID, citation, or value. **Never author** replacement content silently — propose, with rationale, for the student to accept (UC-AI-review-proposal). When a dimension is genuinely clean, **say so briefly** rather than inventing a nit. Each mode's Part 1 adds its mode-specific output notes (its category vocabulary and how it groups).
