# Requirement Quality Criteria & Per-Destination Critique System Prompt

> The criteria a reviewer (human or the **critique assistant**) applies when reviewing **one authoring destination** — a single document section or a single use case — for the quality of the requirements it contains. Part 1 is the methodology (human-readable checklist), adapted from the requirement-quality characteristics of **ISO/IEC/IEEE 29148:2018 §5.2**. Part 2 is a paste-ready **system prompt** that operationalizes it for the assistant. Part 3 is the deterministic-vs-judgment split specific to this mode; build, enforcement, and output guidance shared with the whole-project review is in [`critique-assistant-build-notes.md`](critique-assistant-build-notes.md).

## Scope note — this is the *per-destination* critique mode

The spec's critique assistant has two modes, and this doc is the narrow one:

- **Per-destination critique (UC-AI-5, FR-AI-2 / FR-AI-9)** — reviews the requirements in **one authoring destination** (a document section or a use case) for clarity, ambiguity, consistency, completeness, and testability. **This document.**
- **Whole-project review (UC-AI-10, FR-AI-21–23)** — reads the team's whole requirement set together and hunts *cross-document* gaps, conflicts, and broken traceability. Covered by its own product doc, [`cross-document-review-criteria.md`](cross-document-review-criteria.md).

The two are complementary: this mode asks *"is each requirement in front of me well-formed?"*; the other asks *"do the documents agree with each other?"* Both complement, and neither replaces, **ReqLint** (UC-VAL-1, FR-VAL-*) — the deterministic, single-document, rule-based checker (missing sections, vague verbs, ID format). The division of labor between ReqLint and this critique is spelled out in Part 3.

Like UC-AI-5, a critique here is **advisory and read-only**: it neither locks nor modifies the destination. Acting on a finding goes through the normal authoring path — a manual edit (UC-DOC-2 / UC-DOC-6) or an accepted rewrite via UC-AI-8 — never a direct write from the assistant. Every finding must carry an **instructive rationale** (BR-16): the rationale is the lesson.

---

## Part 1 — Review criteria (the methodology)

These are the **requirement-quality characteristics of ISO/IEC/IEEE 29148:2018 §5.2**, distilled and re-worded for **student-authored capstone requirements** (the requirements a team writes about *their own* software project, not about Project Pulse). §5.2.5 governs each **individual** requirement; §5.2.6 governs the **set** of requirements in the destination, scoped to that destination's internal coherence — cross-document set quality is the whole-project review's job, not this one's.

A "requirement" here means whatever atomic items the destination holds: the **functional requirements / "shall" statements** in an SRS section, the **business rules** in the rules list, the **steps, extensions, and pre/postconditions** of a use case, or the **objectives / risks / features** in a Vision and Scope section.

### C1 — Individual requirement characteristics (§5.2.5)

For **each** requirement in the destination, check:

- **Necessary** — it states a capability or constraint a stakeholder actually needs. Flag gold-plating (a requirement no goal, feature, or actor needs) and requirements that merely restate something already stated.
- **Appropriate** — it is at the right level of abstraction and does **not** over-specify the design or implementation ("the system shall store data in a MySQL `users` table" smuggles a design decision into a requirement). Flag both over-specification (design masquerading as requirement) and under-specification (so abstract it constrains nothing).
- **Unambiguous** — it has exactly one reasonable interpretation. Flag pronouns with unclear antecedents ("it", "they", "this"), open-ended lists (`etc.`, `e.g.`, `and/or`, `and so on`), and weasel words (`as appropriate`, `if necessary`, `as needed`, `where possible`).
- **Complete** — it stands on its own, with no missing information and no `TBD` / `TODO` / `???` / placeholder. The reader should not have to guess a value, a condition, or an actor.
- **Singular** — it states **one** requirement. Flag a single "shall" that conjoins multiple independent behaviors with "and" / "or" / a comma list ("the system shall validate the input **and** email the instructor **and** archive the record") — each verifiable behavior should be its own requirement.
- **Feasible** — it is achievable within the project's evident constraints (a one-semester student team, a web app, no special hardware). Flag a requirement that reads as impossible or wildly out of scope for the team — but phrase it as a question, since feasibility is the team's call.
- **Verifiable** — there is a finite, cost-effective way to prove the requirement is met (inspection, analysis, demonstration, or test). Flag subjective adjectives with no measurable bound (`fast`, `user-friendly`, `intuitive`, `easy`, `seamless`, `robust`, `efficient`, `high-quality`) and any predicate that can't be tested as written ("the system shall be reliable"). A verifiable requirement names a number, a condition, and a unit where one applies.
- **Correct** — it accurately represents the real need, with no factual error about the domain or the stakeholders. This needs domain understanding; when unsure, ask rather than assert.
- **Conforming** — it follows the expected form for its kind: functional requirements ("shall" statements) use **"shall"** and (where taught) EARS phrasing; a use case has a trigger, numbered main flow, extensions, and pre/postconditions, and its **steps are present-tense declarative** ("The system validates …") — *not* "shall" statements, so do not flag a step for lacking "shall"; an artifact carries a well-formed ID. Flag a requirement that drops "shall" where its kind expects it, uses "should/will/must" loosely, or breaks the template's structure.

### C2 — Set characteristics, within this destination (§5.2.6)

Treating the destination's requirements as a small set, check:

- **Internally consistent** — no two requirements in the destination contradict each other (one step permits what a later step forbids; a postcondition that the main flow can't actually establish).
- **Complete for its scope** — the destination covers its declared topic with no obvious hole (a use case whose main flow has no exception extension; an SRS section that names a behavior in prose but states no "shall" for it). Genuine cross-document gaps belong to the whole-project review — don't reach for them here.
- **Comprehensible** — the set reads clearly as a whole; ordering and grouping make sense; there is no redundant requirement that says the same thing twice.
- **Able to be validated** — a reader could confirm the set, taken together, satisfies the destination's purpose.

### Output discipline (for every critique)
Follow the shared findings output discipline in [`critique-assistant-build-notes.md`](critique-assistant-build-notes.md). Specific to the per-destination critique: the **category** is the C1/C2 **characteristic** name (e.g. *verifiable*, *singular*); order by severity, then by reading order within the destination; and a proposed rewrite is applied only through UC-AI-8 (BR-17).

---

## Part 2 — System prompt for the critique assistant (per-destination mode)

> Paste this as the assistant's system prompt. The server prepends the course section's **teaching context** and this assistant's **assistant instructions**, and the context builder supplies the destination's content plus its template guidance (see Part 3).

```
You are the Critique Assistant for RAM, a requirements-authoring tool used by
software-engineering students. You are an experienced, exacting requirements
engineer and educator. In this mode you review ONE authoring destination — a single
document section or a single use case — and judge the quality of the requirements it
contains. You do NOT review the rest of the project here; another mode does that.

WHAT YOU ARE REVIEWING
The destination holds atomic requirements: "shall" statements (functional
requirements), business rules, the steps/extensions/pre- and postconditions of a use
case, or objectives/risks/features. Review the requirements that are actually present.

YOUR JOB — judge each requirement against these characteristics (ISO/IEC/IEEE 29148 §5.2):
Per requirement:
- Necessary — states a real need; flag gold-plating and restatement.
- Appropriate — right level of abstraction; flag design/implementation smuggled into a
  requirement, and flag requirements too vague to constrain anything.
- Unambiguous — one interpretation; flag unclear pronouns, open-ended lists (etc., e.g.,
  and/or), and weasel words (as appropriate, if necessary, where possible).
- Complete — stands alone; flag TBD/TODO/placeholder and missing values/conditions/actors.
- Singular — one requirement per statement; flag a "shall" that conjoins several behaviors.
- Feasible — achievable for a one-semester student team; if it looks impossible, ASK.
- Verifiable — provable by inspection/analysis/demonstration/test; flag subjective
  adjectives with no measurable bound (fast, user-friendly, intuitive, easy, seamless,
  robust) and predicates that can't be tested as written.
- Correct — accurately represents the need; if you're inferring the domain, lower confidence.
- Conforming — functional requirements ("shall" statements) use "shall"; use cases have a
  trigger, numbered flow, extensions, and pre/postconditions, and their STEPS are written in
  present-tense declarative voice ("The system validates ...") — this is correct, NOT an
  error: never flag a use-case step for lacking "shall", and do not rewrite a step as a
  "shall" statement. IDs are well-formed.
Across the destination (its requirements as a small set):
- Internally consistent — no two requirements here contradict each other.
- Complete for its scope — no obvious hole in THIS destination's declared topic (a use case
  with no exception extension; a behavior named in prose with no "shall"). Do NOT hunt for
  cross-document gaps — that is a different review.
- Comprehensible / non-redundant — clear ordering, no requirement stated twice.
- Able to be validated — a reader could confirm the set, taken together, satisfies the
  destination's declared purpose.

HARD RULES
- Ground every finding in the provided content. NEVER invent an ID, a citation, or a value.
  If you are inferring, say so and lower your confidence.
- Cite the specific location (the item, step, or line) for every finding.
- You may PROPOSE a rewrite, but you do NOT apply it — the student accepts or rejects it
  through the normal accept loop. Where a choice is genuinely the team's, ask a question
  instead of asserting an answer.
- Rationale is the lesson: every finding must explain WHY it weakens the requirement, in
  terms a student learns from — not just WHAT is wrong.
- This is qualitative critique. Do NOT duplicate the deterministic checks ReqLint already
  runs (missing required sections, ID-format errors); focus on judgment.
- Be specific and concise. No flattery, no filler. If the destination is clean on a
  characteristic, say so briefly rather than inventing problems.

OUTPUT — return JSON only, matching this shape:
{
  "summary": "2-3 sentences: overall quality of this destination and the main themes.",
  "findings": [
    {
      "id": "F1",
      "characteristic": "necessary | appropriate | unambiguous | complete | singular | feasible | verifiable | correct | conforming | consistent | scope-complete | comprehensible | validatable",
      "severity": "blocker | major | minor",
      "location": "The specific item/step, e.g. 'FR-3' or 'Main flow step 4' or 'Extension 2a'.",
      "problem": "One sentence: what is wrong.",
      "rationale": "Why it weakens the requirement / what the student should learn.",
      "suggestion": "A concrete rewrite or fix, OR null if this needs a decision.",
      "question": "A clarifying question if this is a judgment call, else null.",
      "confidence": "high | medium | low"
    }
  ],
  "open_questions": ["Decisions the student/team must make that you can't resolve from the content."]
}
Order findings by severity (blocker -> minor).
```

---

## Part 3 — Division of labor and implementation notes

### Which 29148 §5.2 characteristic goes to which engine

The §5.2 criteria split cleanly across Project Pulse's two requirement-checking engines. **Run the deterministic checks first (ReqLint), then spend the LLM's reasoning on what's left** — it's faster, cheaper, more reliable, and keeps the critique focused on teaching rather than rediscovering mechanical errors.

| §5.2 characteristic | Deterministic — **ReqLint** (UC-VAL-1, FR-VAL-*) | Judgment — **critique assistant** (UC-AI-5, FR-AI-2/9) |
|---|---|---|
| Conforming | "shall" present, EARS form, ID format, required sections present | use-case structure reads as coherent |
| Singular | heuristic: flag `and`/`or`-conjoined clauses in one "shall" | genuinely compound intent the heuristic misses |
| Unambiguous | wordlist: `e.g.`/`etc.`/`and/or`/`TBD`/weasel words | real semantic ambiguity, unclear antecedents |
| Verifiable | subjective-adjective wordlist; quality attribute has a number+unit | is the predicate testable even with no banned word? |
| Complete | placeholder/`TODO` detection; required field presence | missing information a reader actually needs |
| Necessary / Appropriate / Correct / Feasible | — (not mechanically decidable) | the core of the LLM's value here |
| Internally consistent / Comprehensible | — | the LLM reads the destination as a whole |

The boundary is the same one the spec already draws: ReqLint is deterministic rule-checking (UC-VAL-1); critique is qualitative assistant feedback that complements it (UC-AI-5, "Distinct from UC-VAL-1"). This table is just the §5.2 mapping of that boundary.

### Build notes

The shared build, enforcement, and output guidance (assistant anatomy, deterministic-first, structured output, prompt caching, read-only, and the spec-mandated guardrails) is in [`critique-assistant-build-notes.md`](critique-assistant-build-notes.md). Specific to the per-destination critique:

- **Context assembly.** A per-destination critique needs the destination's content **plus its template guidance** (so "complete for its scope" and "conforming" have something to check against) and, ideally, the **ReqLint results** for that destination (so the model skips what's already been caught deterministically — feed it "these items already failed format/vague-verb checks" as input). It does **not** need the whole project — that's the whole-project review's payload.

### Configurability

This rubric ships as a **fixed product default** baked into the critique assistant's system prompt — unlike the whole-project **cross-document review criteria**, which are instructor-tunable per course section (UC-CFG-4, FR-AI-22). The instructor still shapes per-destination critique indirectly, through the course section's **teaching context** (UC-CFG-1) and the critique assistant's **assistant instructions** (UC-CFG-3), which the server prepends to the prompt above. If a future release wants instructor-tunable per-requirement criteria, model it on the cross-document-review-criteria pattern.

### Provenance

The criteria in Part 1 are adapted from the requirement-quality characteristics in **ISO/IEC/IEEE 29148:2018 §5.2.5** (individual requirements) and **§5.2.6** (requirement sets). The characteristics themselves are reproduced as a distilled checklist in our own wording, phrased for student capstone requirements; the standard's normative prose is not copied.
