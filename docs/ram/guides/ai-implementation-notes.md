# AI Implementation Notes (RAM)

> Reference for implementing the AI use cases (UC-AI-*). It captures the architecture reasoning behind the **Assistant** concept so we build the lean thing, not an over-engineered multi-agent system. Nothing here changes the requirements; it's build guidance.

## 1. "Assistant" is a configured role, not an autonomous agent

We deliberately renamed the domain concept from "agent" to **Assistant** to avoid importing the industry meaning of *agent* (an LLM that autonomously plans, calls tools, and loops). In RAM, an Assistant is **a configured pedagogical role**, not a piece of autonomous AI technology. Concretely, each Assistant is:

> **a system prompt (role + persona + rules + Teaching Context + Assistant Instructions) + a context-assembly recipe + one Claude API call** (occasionally multi-turn for conversation).

The elicitation assistant and the critique assistant are the **same engine, same proxy, same model** — they differ only in their system prompt, the context they're fed, and how their output is handled. "Just good prompting against the Claude API" is, for the most part, the correct mental model.

## 2. Anatomy of one Assistant

Build each Assistant as a triple, all server-side (Spring Boot), behind the existing **server-side AI proxy** (the Vue SPA must never call the LLM directly):

- **System prompt** — role, Socratic persona, hard rules, plus the course section's **Teaching Context** and that Assistant's **Assistant Instructions** (instructor-authored, per-assistant).
- **Context builder** — assembles the relevant artifacts for the call (the target document section/use case, or project-wide coverage; Project Source Material unless excluded; related glossary terms).
- **Output handler** — parses the response and routes it into the right UI/data path (findings list, candidate requirements gated by accept/reject, coaching text, etc.).

## 3. Keep the Assistants separate — but "separate" means *configuration*

Do **not** collapse the assistants into one mega-prompt. Keep them distinct because the differences are real and product-driven:

- Different **jobs** → different system prompts and different output handling.
- The instructor **configures each one separately** (Assistant Instructions are per-assistant) and **enables/disables each individually** (FR-AI-7, UC-CFG-2). You literally cannot offer that with one blended prompt.
- Focused prompts are far easier to tune and reason about than one prompt trying to be six roles.

"Separate" here means separate *configurations*, **not** separate services, frameworks, message buses, or running processes.

## 4. The project assistant is a router, not an orchestrator

The project assistant (UC-AI-9) is the single conversational front door. Implement it as **classify → dispatch**, not as an autonomous orchestrator. The spectrum, simplest → hardest:

1. **Explicit invocation** — context/UI picks the assistant (in-document panel = elicitation; a "Critique" button = critique). Deterministic, trivially testable.
2. **Router (target)** — one chatbot; one LLM call classifies intent + extracts args into a *structured* output (Claude **tool/function calling**); your server code deterministically dispatches to the right assistant. Expose `run_elicitation(scope, target)`, `run_critique(target)`, etc. as tools — the model picks the tool and fills the args; **your code does the work and holds the authority.**
3. **Autonomous orchestrator** — one LLM with the assistants as tools, looping and deciding on its own. Most flexible, least predictable, hardest to test, most expensive. **Avoid for now.**

FR-AI-19 says the system shall *route* the student into the corresponding use case — that's pattern #2, not #3. And keep the direct entry points (#1) even after the router exists: they're cheaper, more testable, and more accessible than round-tripping everything through chat.

## 5. Keep authority in code, never in the prompt

Three things must be enforced by application logic, not trusted to the model (prompts get jailbroken; gates don't):

- **Per-course-section enablement** (FR-AI-7) — check before you ever call the model.
- **Never author without explicit accept** (FR-AI-3, FR-AI-8) — the propose → inspect → accept loop is a server-side gate, with no "accept all."
- **Validate model-chosen targets** — before acting on "go to section X" or "critique artifact Y," confirm X/Y are real IDs.

Remember **Project Source Material and student content are untrusted input** flowing into prompts. Your guardrails must survive a malicious pitch PDF (prompt injection).

## 6. Applying a content-producing assistant's proposals to the document (Accept/Reject)

Three assistants *produce content* rather than just advise — **structuring (UC-AI-4)**, **drafting (UC-AI-7)**, and a critique **rewrite** (UC-AI-5 ext 4a). For all of them the rule is identical: **the AI panel proposes; Accept applies the change through the *same* authoring path a manual edit uses — never a direct write from the AI component into the editor.** That's both the spec (FR-AI-8 / UC-AI-8: the change becomes the Student's authored edit) and the right Vue architecture.

**Structured candidates, not prose.** Have the assistant return machine-usable output (tool/JSON mode). In the real editor, sections are typed — the two seen so far are **`LIST`** and **`RICH_TEXT`**. A `LIST` section (e.g., `RISKS`) holds requirement artifacts as rows (Key / Type / Title / Priority / body) behind an **Add Requirement** button; a `RICH_TEXT` section (e.g., `BACKGROUND`) is a WYSIWYG rich-text editor (headings, bold/italic, lists, quote, link…). So a candidate targets a section, and its payload depends on the section type:
```
{ id, sectionId, sectionType:'LIST'|'RICH_TEXT',
  fields:   { type, title, priority, body },   // for a LIST row
  richText: '<h3>…</h3><p>…</p>',              // for a RICH_TEXT insert (editor HTML/fragment)
  sourceNoteSpan, rationale }
```
`sourceNoteSpan` gives you FR-AI-12's "traceable to its source note."

**Accept = a store action, not a component-to-component write.** The editor today is two columns (outline | section content) with **no AI panel** — so the AI panel (chat + accept/reject cards) is the main net-new surface. Use a single client store (Pinia) as the source of truth for the open document; the section editor and the AI panel both bind to it and never poke each other. Accept dispatches:
```
applyCandidate(c):                         // c.sectionId = the open, locked section
  requireLock(c.sectionId)                 // she already holds it (UC-DOC-2)
  if c.sectionType === 'LIST':
     store.addRequirement(c.sectionId, c.fields, provenance:'assistant')   // == the "Add Requirement" op
  else: // RICH_TEXT
     editor.applyFragment(c.sectionId, c.richText, provenance:'assistant')  // via the editor's transaction API, then Save
  markAccepted(c.id)
```
The editor re-renders reactively from the store; Reject just discards the candidate. This *is* UC-AI-8 — per-item, with diff + rationale, no "accept all."

**The two section types need two accept/reject UIs.** A `LIST` section is the easy case: candidates are **discrete rows**, so each card is just "add this requirement or not." A `RICH_TEXT` section is a WYSIWYG editor (the toolbar — H1–H3, bold/italic/strike/code, bullets/numbered/quote/rule, link — points to a ProseMirror/TipTap-style component), so its content is a **rich-text document model, not a plain string**: the assistant emits a rich-text fragment (HTML mapping to those marks/nodes), and acceptance applies it through the **editor's transaction/command API** (then Save), never a string splice. The natural accept/reject surface for rich text is **inline suggestion decorations** (track-changes style) rendered *in* the editor, not separate cards. That's the more involved path — another reason structuring's cleanest first target is `LIST` sections (discrete artifacts), with rich-text insert/rewrite second.

**Locking (corrected by the real UI).** Earlier I assumed new artifacts could be created lock-free — that's wrong here. Requirements live *inside* sections, and adding one is the lock-gated **Add Requirement** operation on that section. So *every* accept targets a section and needs that section's lock; there is no lock-free "new node" path. The clean consequence: **applying always happens inside a locked section** — there is no lock-free path. The simplest slice scopes a run to the one section she already has open and locked; for notes that span several sections (the common case), see the review basket next.

**Presenting the scattered candidates: a review basket + guided per-section apply.** One "structure my notes" call yields a *set* of candidates whose authoring destinations spread across sections and documents, so the review surface can't be the in-section editor alone. Collect them in a dedicated **review basket** (a drawer or full-width view), **grouped by document → section**, so the spread is legible:

```
Structured from your notes — 12 proposed (0 applied)        [pasted notes ▸]
▾ Vision and Scope
   ▾ Risks   (LIST · unlocked)
       ☐ RISK  "Vendor API may change"    Pri: High   ⓘ note ¶3   [Reject] [Accept]
       ☐ RISK  "Team new to Spring Boot"  Pri: Med    ⓘ note ¶7   [Reject] [Accept]
   ▾ Background   (RICH_TEXT)
       ☐ ¶  "The client currently tracks reviews in…"  ⓘ note ¶2   [Reject] [Accept]
▾ Use Cases
   ▾ (new use case)
       ☐ UC  "Student exports a document"              ⓘ note ¶9   [Reject] [Accept]
```

Each card shows the type, a one-line summary, priority, a link to its **source note** (`ⓘ note ¶3` — FR-AI-12 traceability), the rationale on expand, per-item **Accept/Reject**, and — since she stays the author — the option to **edit a candidate before accepting**. No "accept all" (FR-AI-8). Grouping by authoring destination also *teaches placement* (which requirement type belongs where) — UC-AI-4's actual learning goal.

**Decouple review from application.** The basket is the one place she triages everything the notes produced; applying still happens in the relevant editor — the section editor for a Document Section, the use-case editor for a use case — one authoring destination at a time, with the lock visible. Clicking a group navigates to that authoring destination and locks it: for a Document Section it drops the accepted candidates in as real `Add Requirement` rows (or rich-text suggestions for a `RICH_TEXT` section); for a use case it opens the use-case editor on the new (UC-DOC-5) or selected (UC-DOC-6) use case and drops the candidate in as its draft fields. She confirms, Saves, the lock releases, and the basket marks those "✓ applied." If a teammate holds an authoring destination's lock, that candidate is **deferred** ("locked by Alice — apply later") rather than blocking the batch. The fully-automated alternative — Accept applies at once while the server transactionally acquires → applies → saves → releases the lock — is a later enhancement.

**Backend.** Accept ultimately calls the *same* endpoints as manual authoring — the **Add Requirement** call for a `LIST` row, or the rich-text **Save** for a `RICH_TEXT` section — on the locked section; the server enforces lock ownership, versions the change (history), and broadcasts it to teammates (real-time collaboration). The AI has no privileged write path — going through the normal path is what gives you locking, autosave, history, and collaboration for free; a direct AI→editor write would bypass all of it.

**Sequencing.** These content-producing assistants (and the shared UC-AI-8 accept loop) are more involved than the advisory ones — build them after elicitation/critique/tutor, once the accept-loop infrastructure exists.

## 7. The hard part is context assembly, not routing

Most of the engineering value is in the **per-assistant context builder** — pulling the right Teaching Context + artifacts + (optionally) Project Source Material into each call. The router is the easy 10%. Don't dump the whole requirements graph into every call; assemble the relevant slice (e.g., a coverage summary, not full text).

## 8. Claude specifics to build in from day one

- **Prompt caching.** Teaching Context, Assistant Instructions, and templates are large and *stable per course section* — ideal cache candidates. Caching them across calls is a big cost/latency win and painful to retrofit.
- **Structured outputs.** Use tool calling / JSON mode for the router and for any "list of findings/proposals" output, so it's unit-testable.
- **Observability.** Log prompts, responses, and token usage from day one (debugging, cost control, and the audit trail POST-1 records).
- **Graceful degradation** (FR-AI-13). When the LLM service is down, AI features go unavailable and the rest of RAM keeps working.

## 9. Suggested build sequence (de-risks a first-time integration)

1. **One assistant end-to-end first — critique.** It's self-contained (input a section → findings), so you nail the proxy, context builder, accept loop, enablement check, logging, and caching without multi-turn complexity.
2. **Add elicitation** — now you meet the multi-turn loop and the two scopes (focused vs. project-wide), reusing the shared infra.
3. **Add the project assistant (router) last** — by now each assistant has a clear contract, so the classify→dispatch router is straightforward, and you're not learning LLM plumbing and routing simultaneously.

The spec supports any order — each assistant is an independently-invocable use case, with UC-AI-9 sitting on top — so you can ship value (critique) before the router exists.

## 10. What would be over-engineering (avoid)

- An agent framework / autonomous tool-loop for tasks that are single-shot (critique, explain, structure).
- Assistants talking to each other / a multi-agent message bus.
- A vector DB / RAG pipeline before you've proven you need retrieval (pre-assembled context covers most cases).
- The autonomous orchestrator (pattern #3) instead of a simple router (pattern #2).

## Mapping to the spec

| Concept here | In the docs |
|---|---|
| Assistant (configured role) | `Assistant` glossary term; the assistants list in `vision-and-scope.md` |
| The specialized assistants | UC-AI-1..UC-AI-8 |
| Project assistant (router) | UC-AI-9; FR-AI-18, FR-AI-19 |
| Per-assistant behavior config | `Assistant Instructions`; UC-CFG-3 |
| Enable/disable per course section | FR-AI-7; UC-CFG-2 |
| Shared standards | `Teaching Context`; UC-CFG-1 |
| Never-author-without-accept | FR-AI-3, FR-AI-8; UC-AI-8 |
| LLM-down behavior | FR-AI-13 |
| Pitch materials as context (+ staleness) | `Project Source Material`; UC-AI-1, FR-AI-14, FR-AI-15, FR-AI-17 |
